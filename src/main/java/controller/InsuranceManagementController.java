package main.java.controller;

/*
 * Created by alfonce on 21/07/2017.
 */

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
import main.Main;
import main.java.dao.InsuranceDAO;
import main.java.model.Insurance;
import main.java.util.AlertUtil;
import main.java.util.DBUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class InsuranceManagementController {

    private static Stage stage;
    private final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(2);

    @FXML
    private TextField searchField;
    @FXML
    private TableView<Insurance> insuranceTableView;

    @FXML
    private TableColumn<Insurance, String> name, options, group;

    private ObservableList<Insurance> insurances;

    static void setStage(Stage stage) {
        InsuranceManagementController.stage = stage;
    }

    @FXML
    public void initialize() {
        //set up table
        setUpTable();
        executorService.schedule(this::getData, 0, TimeUnit.SECONDS);
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            configureSearch(newValue);
        });

    }

    private void configureSearch(String value) {
        if (value == null || value.isEmpty()) {
            insuranceTableView.setItems(insurances);
        } else {
            ObservableList<Insurance> searchResults = FXCollections.observableArrayList();
            for (Insurance insurance : insuranceTableView.getItems()) {
                if (insurance.getName().toLowerCase().contains(value.toLowerCase())) {
                    searchResults.add(insurance);
                }
            }
            insuranceTableView.setItems(searchResults);
        }

    }

    void getData() {
        String sql = "SELECT * from Insurance";
        insurances = InsuranceDAO.getList(sql);
        insuranceTableView.setItems(insurances);
    }

    private void setUpTable() {
        Label label = new Label("No insurance companies found!");
        label.getStyleClass().add("text-danger");
        insuranceTableView.setPlaceholder(label);

        //column widths
        name.prefWidthProperty().bind(insuranceTableView.widthProperty().divide(2));
        group.prefWidthProperty().bind(insuranceTableView.widthProperty().divide(3));
        options.prefWidthProperty().bind(insuranceTableView.widthProperty().divide(6));

        name.setCellValueFactory(param -> param.getValue().nameProperty());
        group.setCellValueFactory(param -> param.getValue().groupProperty());
        options.setCellFactory(param -> new TableCell<Insurance, String>() {
            @Override
            public void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                int index = getIndex();
                if (index >= 0 && index < insuranceTableView.getItems().size()) {

                    Button button = new Button("Edit");
                    button.getStyleClass().add("btn-default-outline");
                    button.setMinWidth(75);
                    button.setOnAction(event -> {
                        showEditDialog(insuranceTableView.getItems().get(index));
                    });

                    setGraphic(button);

                } else {
                    setGraphic(null);
                }
            }
        });
    }

    @FXML
    public void uploadList() {

        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter extensionFilter = new FileChooser.ExtensionFilter("Excel Workbook", "*.xlsx");

        fileChooser.getExtensionFilters().add(extensionFilter);

        File file = fileChooser.showOpenDialog(Main.stage);
        if (file != null) {
            chooseFile(file);
        }
    }

    private void chooseFile(File file) {
        ObservableList<Insurance> list = FXCollections.observableArrayList();
        try {
            FileInputStream inputStream = new FileInputStream(file);

            Workbook workbook = new XSSFWorkbook(inputStream);
            Sheet sheet = workbook.getSheetAt(0);
            Row row;

            for (int i = 0; i < sheet.getLastRowNum(); i++) {
                row = sheet.getRow(i);
                Insurance insurance = new Insurance();
                insurance.setName(row.getCell(0).getStringCellValue());
                list.add(insurance);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        if (DBUtil.addInsurances(list)) {
            AlertUtil.showAlert("Insurance List", "List has been successfully uploaded.", Alert.AlertType.INFORMATION);
            insuranceTableView.getItems().addAll(list);
        } else {
            AlertUtil.showAlert("Error", "An error occurred while trying to upload the document", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void onClickInsuranceGroups() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource
                    ("main/resources/view/insurance_groups.fxml"));
            Parent node = loader.load();

            //stage
            Stage stage = new Stage();
            stage.setScene(new Scene(node));
            stage.initModality(Modality.WINDOW_MODAL);
            stage.setResizable(false);
            stage.setTitle("Manage Insurance Groups");

            InsuranceGroupsController controller = loader.getController();
            controller.setContext(this);

            //show
            stage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onAddNewCompany() {
        showEditDialog(null);

    }

    private void showEditDialog(Insurance insurance) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource
                    ("main/resources/view/edit_insurance.fxml"));
            Parent node = loader.load();

            //stage
            Stage stage = new Stage();
            stage.setResizable(false);
            stage.setScene(new Scene(node));
            stage.initModality(Modality.WINDOW_MODAL);

            if (insurance == null) {
                stage.setTitle("New Insurance Company");
            } else {
                stage.setTitle("Edit Insurance Company");
            }
            //controller
            EditInsuranceController controller = loader.getController();
            if (insurance != null) {
                controller.setInsuranceCompany(insurance);
            }
            controller.setStage(stage);
            controller.setContext(this);

            //show
            stage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void handleNewInsurance(Insurance insurance) {
        insuranceTableView.getItems().add(insurance);
    }

    void refreshData() {
        insuranceTableView.refresh();
    }

    void handleDelete(Insurance insurance) {
        insuranceTableView.getItems().remove(insurance);
    }
}

