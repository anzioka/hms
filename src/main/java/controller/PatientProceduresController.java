package main.java.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import main.Main;
import main.java.dao.HospitalProcedureDAO;
import main.java.model.*;
import main.java.util.AlertUtil;
import main.java.util.CurrencyUtil;
import main.java.util.DBUtil;
import org.controlsfx.control.textfield.AutoCompletionBinding;
import org.controlsfx.control.textfield.TextFields;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Optional;

/**
 * Created by alfonce on 20/05/2017.
 */
public class PatientProceduresController {
    @FXML
    private TextField procedureName, cost;
    @FXML
    private TableView<PatientProcedure> procedureTableView;
    @FXML
    private TableColumn<PatientProcedure, String> nameCol, costCol, options;

    private Stage stage;

    private int visitId;
    private String insurer, insuranceNum;
    private HospitalProcedure hospitalProcedure = null;
    private PaymentMode paymentMode;
    private int billNumber;
    private boolean outpatientMode = false;
    private int admissionNum;
    private String patientId, patientName;

    void setStage(Stage stage) {
        this.stage = stage;
    }

    @FXML
    public void initialize() {
        setUpAutocomplete();
        setUpTable();
    }

    private void setUpAutocomplete() {
        Task<ObservableList<HospitalProcedure>> task = new Task<ObservableList<HospitalProcedure>>() {
            @Override
            protected ObservableList<HospitalProcedure> call() throws Exception {
                return HospitalProcedureDAO.getAllProcedures();
            }
        };
        task.setOnSucceeded(event -> {
            AutoCompletionBinding<HospitalProcedure> binding = TextFields.bindAutoCompletion(procedureName, task.getValue());
            binding.setOnAutoCompleted(autoCompletionEvent -> {
                hospitalProcedure = autoCompletionEvent.getCompletion();
                cost.setText(hospitalProcedure.getCost() + "");
            });
        });
        new Thread(task).start();
    }

    private void setUpTable() {

        //place holder
        Label label = new Label("No procedures have been specified");
        label.getStyleClass().add("text-danger");
        procedureTableView.setPlaceholder(label);

        //columns
        costCol.setCellValueFactory(param -> new SimpleStringProperty(CurrencyUtil.formatCurrency(param.getValue()
                .getCost())));
        options.setCellFactory(param -> new TableCell<PatientProcedure, String>() {
            @Override
            protected void updateItem(String value, boolean empty) {
                super.updateItem(value, empty);
                int index = getIndex();
                if (index >= 0 && index < procedureTableView.getItems().size()) {
                    PatientProcedure procedure = procedureTableView.getItems().get(index);
                    Button deleteButton = new Button("Delete");
                    deleteButton.getStyleClass().add("btn-danger-outline");
                    if (procedure.getUserId() != Main.currentUser.getUserId()) {
                        deleteButton.setDisable(true);
                    }
                    deleteButton.setOnAction(event -> {
                        removeProcedure(procedure);
                    });
                    setGraphic(deleteButton);
                } else {
                    setGraphic(null);
                }
            }
        });
        nameCol.setCellFactory(param -> new TableCell<PatientProcedure, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                int index = getIndex();
                if (index >= 0 && index < procedureTableView.getItems().size()) {
                    setText(procedureTableView.getItems().get(index).getName());
                    setWrapText(true);
                } else {
                    setText(null);
                }
            }
        });
    }

    private void removeProcedure(PatientProcedure procedure) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Procedure");
        alert.setContentText("Are you sure you want to delete procedure '" + procedure.getName() + "'?");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            String sql = "delete from patient_procedures where Id = " + procedure.getId();
            if (DBUtil.executeStatement(sql)) {
                sql = "delete from billing where category = '" + Bill.Category.PROCEDURE + "' " +
                        "and amount = " + procedure.getCost() + " and description = '" + procedure.getName() + "'";
                if (outpatientMode) {
                    sql += " and queue_num = " + visitId;
                } else {
                    sql += " and admission_num = " + admissionNum;
                }
                sql += " limit 1";
                if (DBUtil.executeStatement(sql)) {
                    AlertUtil.showAlert("Delete Procedure", "Procedure '" + procedure.getName() + "' successfully removed and patient bill updated", Alert.AlertType.INFORMATION);
                    DBUtil.saveActivity("Deleted procedure '" + procedure.getName() + "' for patient '" + patientName + "'");
                    procedureTableView.getItems().remove(procedure);
                } else {
                    AlertUtil.showAlert("Delete Procedure", "An error occurred while attempting to remove procedure '" + procedure.getName() + "'", Alert.AlertType.ERROR);
                }
            }
        }
    }

    private boolean updatePatientBill() {
        Bill bill = new Bill();
        if (paymentMode == PaymentMode.INSURANCE) {
            bill.setInsuranceId(insuranceNum);
            bill.setInsurer(insurer);
        }
        bill.setDescription(procedureName.getText());
        bill.setAmount(CurrencyUtil.parseCurrency(cost.getText()));
        bill.setDateCreated(LocalDate.now());
        if (outpatientMode) {
            bill.setQueueNumber(visitId);
        } else {
            bill.setAdmissionNumber(admissionNum);
        }
        bill.setCategory(Bill.Category.PROCEDURE);
        bill.setBillNumber(billNumber);
        bill.setPatientNumber(patientId);

        return DBUtil.saveBill(bill);
    }

    @FXML
    private void onSave() {
        if (validInput()) {
            PatientProcedure patientProcedure = createProcedure();
            if (DBUtil.savePatientProcedure(patientProcedure) && updatePatientBill()) {
                if (hospitalProcedure == null) {
                    hospitalProcedure = new HospitalProcedure();
                    hospitalProcedure.setCost(patientProcedure.getCost());
                    hospitalProcedure.setName(patientProcedure.getName());
                    DBUtil.addProcedures(FXCollections.observableArrayList(hospitalProcedure));
                }

                    DBUtil.saveActivity("Recorded procedure '" + patientProcedure.getName() + "' for patient '" + patientName + "'");

                AlertUtil.showAlert("Add Procedure", "Procedure '" + patientProcedure.getName() + "' saved and patient bill updated.", Alert.AlertType.INFORMATION);
                procedureTableView.getItems().add(patientProcedure);
                hospitalProcedure = null;
                procedureName.clear();
                cost.clear();
            } else {
                AlertUtil.showAlert("Add Procedure", "An error occurred while attempting to save procedure", Alert.AlertType.ERROR);
            }
        }
    }

    private PatientProcedure createProcedure() {
        PatientProcedure patientProcedure = new PatientProcedure();
        patientProcedure.setName(procedureName.getText());
        patientProcedure.setCost(CurrencyUtil.parseCurrency(cost.getText()));
        User currentUser = Main.currentUser;
        if (currentUser.getCategory() == UserCategory.DOCTOR) {
            patientProcedure.setUserName("Dr. " + currentUser.getLastName());
        } else {
            patientProcedure.setUserName(currentUser.getFirstName());
        }
        patientProcedure.setUserId(currentUser.getUserId());
        patientProcedure.setId(DBUtil.getNextAutoIncrementId("patient_procedures"));
        if (outpatientMode) {
            patientProcedure.setVisitId(visitId);
        } else {
            patientProcedure.setAdmissionNum(admissionNum);
        }
        return patientProcedure;
    }

    private boolean validInput() {
        String errorMsg = "";
        if (procedureName.getText() == null || procedureName.getText().isEmpty()) {
            errorMsg += "Procedure name required!\n";
        }
        if (CurrencyUtil.parseCurrency(cost.getText()) == -1) {
            errorMsg += "Invalid cost!\n";
        }
        if (errorMsg.isEmpty()) {
            return true;
        }
        AlertUtil.showAlert("Input Error(s)", errorMsg, Alert.AlertType.ERROR);
        return false;
    }

    @FXML
    private void onClose() {
        stage.close();
    }

    void setVisitId(int visitId) {
        this.visitId = visitId;
        getCurrentProcedures(visitId);
        getPatientInfo();
    }

    private void getPatientInfo() {
        String sql = "SELECT payment_mode, Patients.PatientId, Patients.FirstName, Patients.LastName, bill_number, InsuranceProvider, InsuranceId from Queues " +
                "inner join patients on patients.patientId = queues.patientId " +
                "where queues.visitId = " + visitId;
        if (!outpatientMode) {
            sql = "SELECT payment_mode, Patients.PatientId, Patients.FirstName, Patients.LastName, bill_number, InsuranceProvider, InsuranceId from inpatients " +
                    "inner join patients on patients.patientId = inpatients.patient_Id " +
                    "where admission_num = " + admissionNum;
        }
        try {
            ResultSet resultSet = DBUtil.executeQuery(sql);
            if (resultSet != null) {
                if (resultSet.next()) {
                    paymentMode = PaymentMode.valueOf(resultSet.getString("payment_mode"));
                    billNumber = resultSet.getInt("bill_number");
                    insurer = resultSet.getString("InsuranceProvider");
                    insuranceNum = resultSet.getString("insuranceId");
                    patientId = resultSet.getString("patientId");
                    patientName = resultSet.getString("FirstName") + " " + resultSet.getString("LastName");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    private void getCurrentProcedures(int visitId) {
        Task<ObservableList<PatientProcedure>> task = new Task<ObservableList<PatientProcedure>>() {
            @Override
            protected ObservableList<PatientProcedure> call() {
                return HospitalProcedureDAO.getPatientProcedures(visitId, outpatientMode);
            }
        };
        task.setOnSucceeded(event -> {
            procedureTableView.setItems(task.getValue());
        });
        new Thread(task).start();
    }

    void setOutpatientMode() {
        this.outpatientMode = true;
    }

    public void setAdmissionNum(int admissionNum) {
        this.admissionNum = admissionNum;
        getCurrentProcedures(admissionNum);
        getPatientInfo();
    }

}
