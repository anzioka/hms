package main.java.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import main.java.dao.DiagnosisDAO;
import main.java.model.ICD10_Diagnosis;
import main.java.util.AlertUtil;
import main.java.util.DBUtil;
import main.java.util.FileUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;

public class DiagnosisListController {
    @FXML
    private TextField searchField;
    @FXML
    private Button uploadButton;
    @FXML
    private TableView<ICD10_Diagnosis> tableView;
    @FXML
    private TableColumn<ICD10_Diagnosis, String> name, code;
    private ObservableList<ICD10_Diagnosis> allICD10Diagnoses = FXCollections.observableArrayList();
    @FXML
    private void initialize() {
        ImageView imageView = new ImageView(new Image(getClass().getClassLoader().getResourceAsStream("main/resources/images/upload.png")));
        imageView.setFitWidth(15);
        imageView.setFitHeight(15);
        uploadButton.setGraphic(imageView);
        initTable();
        getData();

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterResults(newValue);
        });
    }

    private void filterResults(String newValue) {
        if (newValue == null || newValue.isEmpty()) {
            tableView.setItems(allICD10Diagnoses);
        } else{
            ObservableList<ICD10_Diagnosis> filtered = FXCollections.observableArrayList();
            for (ICD10_Diagnosis ICD10Diagnosis : allICD10Diagnoses) {
                if (ICD10Diagnosis.getName().toLowerCase().contains(newValue.toLowerCase())) {
                    filtered.add(ICD10Diagnosis);
                }
            }
            tableView.setItems(filtered);
        }
    }

    private void getData() {
        Task<ObservableList<ICD10_Diagnosis>> task = new Task<ObservableList<ICD10_Diagnosis>>() {
            @Override
            protected ObservableList<ICD10_Diagnosis> call() {
                return DiagnosisDAO.getDiagnosisList();
            }
        };

        task.setOnSucceeded(event -> {
            allICD10Diagnoses = task.getValue();
            tableView.setItems(task.getValue());
        });
        new Thread(task).start();
    }

    private void initTable() {

        Label label = new Label("No diagnosis found!");
        label.getStyleClass().add("text-danger");
        tableView.setPlaceholder(label);

        name.setCellFactory(param -> new TableCell<ICD10_Diagnosis, String>(){
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                int index = getIndex();
                if (index >= 0 && index < tableView.getItems().size()) {
                   Label diagnosis = new Label(tableView.getItems().get(index).getName());
                   diagnosis.setWrapText(true);;
                   setGraphic(diagnosis);
                } else{
                    setGraphic(null);
                }
            }
        });
        code.setCellValueFactory(param -> param.getValue().codeProperty());
    }

    @FXML
    private void uploadList() {
        FileChooser.ExtensionFilter filter = new FileChooser.ExtensionFilter("Excel Workbook", "*.xlsx");
        readFile(FileUtil.getSelectedFile(filter));
    }

    private void readFile(File file) {
        if (file != null) {
            ObservableList<ICD10_Diagnosis> list = FXCollections.observableArrayList();
            try {
                FileInputStream is = new FileInputStream(file);
                Workbook workbook = new XSSFWorkbook(is);
                Sheet sheet = workbook.getSheetAt(0);
                Row row;
                for (int i = 0; i <= sheet.getLastRowNum(); i++) {
                    row = sheet.getRow(i);
                    String code = row.getCell(0).getStringCellValue(), name = row.getCell(1).getStringCellValue();
                    if (code == null || code.isEmpty() || name == null || name.isEmpty()) {
                        AlertUtil.showAlert("Invalid Data", "Each excel sheel row should have a disease code and name cell.", Alert.AlertType.ERROR);
                        return;
                    }
                    ICD10_Diagnosis ICD10Diagnosis = new ICD10_Diagnosis(code, name);
                    list.add(ICD10Diagnosis);
                }
                if (!list.isEmpty()  && DBUtil.saveDiseases(list)) {
                    AlertUtil.showAlert("ICD-10 Diagnosis Upload", list.size() + " diagnoses successfully uploaded!", Alert.AlertType.INFORMATION);
                    tableView.setItems(list);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}