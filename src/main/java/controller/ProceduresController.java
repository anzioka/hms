package main.java.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import main.java.dao.HospitalProcedureDAO;
import main.java.model.HospitalProcedure;
import main.java.util.*;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Created by alfonce on 30/06/2017.
 */
public class ProceduresController {

    private static Stage stage;
    @FXML
    private TableView<HospitalProcedure> procedureTableView;
    @FXML
    private TextField name, cost, filter;
    @FXML
    private TableColumn<HospitalProcedure, String> nameCol, costCol, optionsCol;
    private ObservableList<HospitalProcedure> allProcedures = FXCollections.observableArrayList();

    static Stage getStage() {
        return stage;
    }

    static void setStage(Stage stage) {
        ProceduresController.stage = stage;
    }

    @FXML
    public void initialize() {
        setUpTable();

        allProcedures = HospitalProcedureDAO.getAllProcedures();
        procedureTableView.setItems(allProcedures);

        filter.textProperty().addListener((observable, oldValue, newValue) -> filterResults(newValue.toLowerCase()));
    }

    private void filterResults(String filterString) {
        if (filterString == null || filterString.isEmpty()) {
            procedureTableView.setItems(allProcedures);
            return;
        }

        ObservableList<HospitalProcedure> filteredList = FXCollections.observableArrayList();
        for (HospitalProcedure procedure : allProcedures) {
            if (procedure.getName().toLowerCase().contains(filterString)) {
                filteredList.add(procedure);
            }
        }
        procedureTableView.setItems(filteredList);
    }

    private void editProcedure(HospitalProcedure procedure) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource
                    ("main/resources/view/edit_procedure.fxml"));
            Parent node = loader.load();

            //stage
            Stage stage = new Stage();
            stage.setScene(new Scene(node));
            stage.setResizable(false);
            stage.initModality(Modality.WINDOW_MODAL);
            stage.setTitle("Edit Procedure");

            //controller
            EditProcedureController controller = loader.getController();
            controller.setProcedure(procedure);
            controller.setContext(this);
            controller.setStage(stage);

            //show
            stage.showAndWait();
            procedureTableView.refresh();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setUpTable() {

        Label label = new Label("No procedures found!");
        label.getStyleClass().add("text-danger");
        procedureTableView.setPlaceholder(label);

        //col widths
        nameCol.prefWidthProperty().bind(procedureTableView.widthProperty().divide(2));
        costCol.prefWidthProperty().bind(procedureTableView.widthProperty().divide(4));
        optionsCol.prefWidthProperty().bind(procedureTableView.widthProperty().divide(4));

        //table cols
        nameCol.setCellValueFactory(param -> param.getValue().nameProperty());
        costCol.setCellValueFactory(param -> new SimpleStringProperty(CurrencyUtil.formatCurrency(param.getValue().getCost())));

        optionsCol.setCellFactory(param -> new TableCell<HospitalProcedure, String>() {
            @Override
            public void updateItem(String val, boolean empty) {
                super.updateItem(val, empty);
                int index = getIndex();
                if (index >= 0 && index < procedureTableView.getItems().size()) {

                    Button button = new Button("Edit");
                    button.getStyleClass().add("btn-default-outline");

                    button.setOnAction(event -> {
                        HospitalProcedure procedure = procedureTableView.getItems().get(index);
                        editProcedure(procedure);
                        procedureTableView.getSelectionModel().select(procedure);

                    });

                    setGraphic(button);
                } else {
                    setGraphic(null);
                }
            }
        });

    }

    @FXML
    private void onUploadFile() {
        //set extension filter
        FileChooser.ExtensionFilter filter = new FileChooser.ExtensionFilter("Excel Files", "*.xlsx");
        readFile(FileUtil.getSelectedFile(filter));
    }

    private void readFile(File file) {
        ObservableList<HospitalProcedure> hospitalProcedures = FXCollections.observableArrayList();
        try {
            FileInputStream is = new FileInputStream(file);
            Workbook workbook = new XSSFWorkbook(is);
            Sheet sheet = workbook.getSheetAt(0);
            Row row;

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                HospitalProcedure procedure = new HospitalProcedure();
                row = sheet.getRow(i);
                procedure.setName(row.getCell(0).getStringCellValue());
                procedure.setCost(row.getCell(1).getNumericCellValue());
                hospitalProcedures.add(procedure);
            }

            if (DBUtil.addProcedures(hospitalProcedures)) {
                procedureTableView.setItems(HospitalProcedureDAO.getAllProcedures());
                AlertUtil.showAlert("Procedure Upload", "Procedures have been successfully uploaded and saved!",
                        Alert.AlertType.CONFIRMATION);
            } else {
                AlertUtil.showAlert("Error", "An error occurred while trying to upload procedures.", Alert.AlertType.ERROR);
            }

        } catch (IOException e) {
            AlertUtil.showAlert("Error", "An error occurred while trying to read the selected file", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void onSaveProcedure() {
        if (isValidInput()) {
            HospitalProcedure procedure = new HospitalProcedure();
            procedure.setCost(CurrencyUtil.parseCurrency(cost.getText()));
            procedure.setName(name.getText());
            if (DBUtil.addProcedures(FXCollections.observableArrayList(procedure))) {
                procedureTableView.getItems().add(procedure);
                AlertUtil.showAlert("New Procedure",
                        "Procedure has been successfully saved!", Alert.AlertType.CONFIRMATION);
                name.setText(null);
                cost.setText(null);
            } else {
                AlertUtil.showAlert("Error", "An error occurred while trying to save procedure", Alert.AlertType.ERROR);

            }
        }
    }

    private boolean isValidInput() {
        String errorMsg = "";
        if (name.getText() == null || name.getText().isEmpty()) {
            errorMsg += "Procedure name is required!\n";
        } else if (duplicateName(name.getText())) {
            errorMsg += "Procedure already listed!\n";
        }
        if (cost.getText() == null || cost.getText().isEmpty()) {
            errorMsg += "Procedure cost is required!\n";
        } else if (NumberUtil.stringToDouble(cost.getText()) == -1) {
            errorMsg += "Invalid procedure cost value! Acceptable values are whole numbers and decimals\n";
        }
        if (errorMsg.isEmpty()) {
            return true;
        }
        AlertUtil.showAlert("Input Error", errorMsg, Alert.AlertType.ERROR);
        return false;
    }

    private boolean duplicateName(String name) {
        for (HospitalProcedure procedure : procedureTableView.getItems()) {
            if (procedure.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    void handleDelete(HospitalProcedure procedure) {
        procedureTableView.getItems().remove(procedure);
    }

    void refreshData() {
        procedureTableView.refresh();
    }
}
