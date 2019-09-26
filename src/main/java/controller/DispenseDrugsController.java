package main.java.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import main.java.dao.PatientDAO;
import main.java.model.*;
import main.java.util.AlertUtil;
import main.java.util.DBUtil;
import main.java.util.DateUtil;
import main.java.util.StringUtil;

import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by alfonce on 06/07/2017.
 */
public class DispenseDrugsController {
    private static final String NAME = "name";
    private static final String DOSAGE = "dosage";
    private static final String TOTAL_QTY = "total_qty";
    private static final String QTY = "qty";
    private static final String FORMULATION = "formulation";
    @FXML
    private Label patientNo, sex, firstName, lastName, dateOfBirth, age, dateCreated, receiptNum, patientNoLabel;
    @FXML
    private TableView<Map<String, String>> tableView;
    @FXML
    private TableColumn<Map<String, String>, String> nameCol, dosageCol, qtyAvailable, qty, formulation;

    private Stage stage;
    private String prescriptionId;
    private String category;
    private String patientNum;
    private String drugId;

    @FXML
    public void initialize() {
        //table cols
        setUpTable();
    }

    private void setUpTable() {
        for (TableColumn column : tableView.getColumns()) {
            if (column == nameCol) {
                column.prefWidthProperty().bind(tableView.widthProperty().divide(3));
            } else {
                column.prefWidthProperty().bind(tableView.widthProperty().divide(6));
            }
        }
        nameCol.setCellValueFactory(param -> StringUtil.getStringProperty(param.getValue().get(NAME)));
        dosageCol.setCellValueFactory(param -> StringUtil.getStringProperty(param.getValue().get(DOSAGE)));
        qtyAvailable.setCellValueFactory(param -> StringUtil.getStringProperty(param.getValue().get(TOTAL_QTY)));
        formulation.setCellValueFactory(param -> StringUtil.getStringProperty(param.getValue().get(FORMULATION)));
        qty.setCellFactory(param -> new TableCell<Map<String, String>, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                int index = getIndex();
                if (index >= 0 && index < tableView.getItems().size()) {
                    Text text = new Text(tableView.getItems().get(index).get(QTY));
                    text.getStyleClass().add("fw-500");
                    setGraphic(text);
                } else {
                    setGraphic(null);
                }
            }
        });
    }

    private void getPatientInfo() {
        String sql = "select * from patients where patientId='" + patientNum + "'";
        if (category.equals(PatientCategory.INPATIENT.toString())) {
            sql = "select patients.* from patients " +
                    "inner join inpatients on inpatients.patient_id = patients.PatientId " +
                    "where inpatient_num = '" + patientNum + "'";
        }
        Patient patient = PatientDAO.getPatient(sql);
        if (patient != null) {
            sex.setText(patient.getSexuality());
            patientNo.setText(patient.getPatientId());
            firstName.setText(patient.getFirstName());
            lastName.setText(patient.getLastName());
            dateOfBirth.setText(DateUtil.formatDate(patient.getDateOfBirth()));
            age.setText(patient.getPatientAge());
        }
        if (category.equals(PatientCategory.INPATIENT.toString())) {
            patientNo.setText(patientNum);
        }
    }

    void setStage(Stage stage) {
        this.stage = stage;
    }

    @FXML
    private void onDispenseDrugs() {
        if (dispenseDrugHelper()) {
            AlertUtil.showAlert("Dispense Drug", "'" + tableView.getItems().get(0).get(NAME) + "' successfully dispensed!", Alert.AlertType.INFORMATION);
            stage.close();
        } else {
            AlertUtil.showAlert("Dispense Error", "An error occurred while attempting to dispense", Alert.AlertType.ERROR);
        }
    }

    private boolean dispenseDrugHelper() {
        String sql = "update prescriptions set status = '" + Prescription.Status.COMPLETED + "'" +
                " where id = " + prescriptionId;
        if (DBUtil.executeStatement(sql)) {
            sql = "update drugs set ShopQuantity = ShopQuantity - " +
                    Integer.parseInt(tableView.getItems().get(0).get(QTY)) + " where DrugCode = " + drugId;
            if (DBUtil.executeStatement(sql)) {
                if (category.equals(PatientCategory.WALK_IN.toString())) {
                    sql = "update queues set Status = '" + PatientQueue.Status.DISCHARGED + "' " +
                            "where PatientId = '" + patientNum + "' " +
                            "and DoctorId = 0";
                    return DBUtil.executeStatement(sql);
                }
                return true;
            }
        }
        return false;
    }

    public void setParameters(String category, String patientNum, String dateTime, String prescriptionId, String drugId) {
        dateCreated.setText(dateTime);
        this.drugId = drugId;
        this.prescriptionId = prescriptionId;
        this.category = category;
        if (category.equals(PatientCategory.INPATIENT.toString())) {
            patientNoLabel.setText("Inpatient No.");
        }
        this.patientNum = patientNum;
        getPatientInfo();
        getPrescriptionDetails();
        getPaymentDetails();
    }

    private void getPaymentDetails() {
        Task<Integer> task = new Task<Integer>() {
            @Override
            protected Integer call() throws Exception {
                String sql = "select receipt_no from payments " +
                        "where bill_item_id = " + prescriptionId;
                ResultSet resultSet = DBUtil.executeQuery(sql);
                if (resultSet != null && resultSet.next()) {
                    return resultSet.getInt("receipt_no");
                }
                return null;
            }
        };
        task.setOnSucceeded(event -> {
            if (task.getValue() == null) {
                receiptNum.setVisible(false);
            } else {
                receiptNum.setText("Receipt No. " + task.getValue());
            }
        });
        new Thread(task).start();
    }

    private void getPrescriptionDetails() {
        Task<ObservableList<Map<String, String>>> task = new Task<ObservableList<Map<String, String>>>() {
            @Override
            protected ObservableList<Map<String, String>> call() throws Exception {
                ObservableList<Map<String, String>> list = FXCollections.observableArrayList();
                String sql = "select quantity, dosage, formulation, name, shopQuantity " +
                        "from prescriptions inner join drugs on drugs.drugCode = prescriptions.drug_code " +
                        "where prescriptions.id = " + prescriptionId;
                ResultSet resultSet = DBUtil.executeQuery(sql);
                if (resultSet != null && resultSet.next()) {
                    Map<String, String> map = new HashMap<>();
                    map.put(FORMULATION, Formulation.valueOf(resultSet.getString("formulation")).toString());
                    map.put(QTY, resultSet.getString("quantity"));
                    map.put(DOSAGE, Prescription.Dosage.valueOf(resultSet.getString("dosage")).toString());
                    map.put(NAME, resultSet.getString("name"));
                    map.put(TOTAL_QTY, resultSet.getString("shopQuantity"));
                    list.add(map);
                }
                return list;
            }
        };
        task.setOnSucceeded(event -> {
            tableView.setItems(task.getValue());
        });
        new Thread(task).start();
    }
}
