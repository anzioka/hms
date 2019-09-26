package main.java.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import main.Main;
import main.java.dao.LabTestDAO;
import main.java.dao.PatientDAO;
import main.java.model.*;
import main.java.util.AlertUtil;
import main.java.util.DBUtil;
import main.java.util.DateUtil;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import static main.java.controller.LaboratoryController.*;

/**
 * Created by alfonce on 05/07/2017.
 */
public class EditTestResultController {

    //patient details
    @FXML
    private Label patientNumber, sex, firstName, lastName, dateOfBirth, age, testName;
    @FXML
    private ChoiceBox<Specimen> specimenChoiceBox;
    @FXML
    private Label currentUserName, date, requester, receiptNo;
    @FXML
    private TextArea remarks;
    //table
    @FXML
    private TableView<LabTestResult> tableView;

    @FXML
    private TableColumn<LabTestResult, String> testResultsCol, refRange, flag;

    private Stage stage;
    private Map<String, String> entry;

    @FXML
    public void initialize() {
        specimenChoiceBox.setItems(FXCollections.observableArrayList(Specimen.values()));
        setUpTable();
        currentUserName.setText(Main.currentUser.toString());

    }

    private void setUpTable() {
        testResultsCol.setCellFactory(param -> new TableCell<LabTestResult, String>() {
            @Override
            public void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                int index = getIndex();
                if (index >= 0 && index < tableView.getItems().size()) {
                    TextField textField = new TextField();
                    textField.setText(tableView.getItems().get(index).getResult());
                    textField.textProperty().addListener((observable, oldValue, newValue) -> {
                        tableView.getItems().get(index).setResult(newValue);
                    });
                    textField.requestFocus();
                    setGraphic(textField);
                } else {
                    setGraphic(null);
                }
            }
        });

        flag.setCellValueFactory(param -> param.getValue().flagProperty());
        refRange.setCellValueFactory(param -> param.getValue().rangeProperty());

    }

    void setStage(Stage stage) {
        this.stage = stage;
    }

    void setLabRequestEntry(Map<String, String> entry) {
        this.entry = entry;
        specimenChoiceBox.setValue(Specimen.valueOf(entry.get(SAMPLE)));
        testName.setText(entry.get(TEST_NAME));
        date.setText(entry.get(TIME));
        getTestFlags(entry.get(TEST_ID));
        getCurrentPatient();
        getRequestee();
        getReceiptNo();
    }

    private void getReceiptNo() {
        Task<Integer> task = new Task<Integer>() {
            @Override
            protected Integer call() throws Exception {
                String sql = "select receipt_no from payments where bill_item_id = " + entry.get(REQUEST_ID);
                ResultSet resultSet = DBUtil.executeQuery(sql);
                if (resultSet != null && resultSet.next()) {
                    return resultSet.getInt("receipt_no");
                }
                return null;
            }
        };
        task.setOnSucceeded(event -> {
            if (task.getValue() == null) {
                receiptNo.setVisible(false);
            } else {
                receiptNo.setText("Receipt No. " + task.getValue());
            }
        });
        new Thread(task).start();
    }

    private void getRequestee() {
        Task<String> task = new Task<String>() {
            @Override
            protected String call() throws Exception {
                String sql = "select FirstName, LastName " +
                        "from users " +
                        "inner join lab_requests on lab_requests.UserId = Users.Id " +
                        "where lab_requests.Id = " + entry.get(REQUEST_ID);
                ResultSet resultSet = DBUtil.executeQuery(sql);
                if (resultSet != null && resultSet.next()) {
                    return resultSet.getString("FirstName") + " " + resultSet.getString("LastName");
                }
                return null;
            }
        };
        task.setOnSucceeded(event -> {
            requester.setText(task.getValue());
        });
        new Thread(task).start();
    }

    private void getTestFlags(String test) {
        Task<ObservableList<LabTestFlag>> task = new Task<ObservableList<LabTestFlag>>() {
            @Override
            protected ObservableList<LabTestFlag> call() throws Exception {
                return LabTestDAO.getLabTestFlagsForTest(test);
            }
        };
        task.setOnSucceeded(event -> {
            for (LabTestFlag flag : task.getValue()) {
                LabTestResult result = new LabTestResult();
                result.setFlag(flag.getName());
                result.setRange(flag.getDefaultVal());
                tableView.getItems().add(result);
            }
        });
        new Thread(task).start();
    }

    private void getCurrentPatient() {
        String patientId = entry.get(PATIENT_ID);
        Task<Patient> task = new Task<Patient>() {
            @Override
            protected Patient call() {
                String sql;
                if (entry.get(CATEGORY).equals(PatientCategory.INPATIENT.toString())) {
                    //inpatient
                    sql = "select patients.* from patients " +
                            "inner join inpatients on inpatients.patient_id = patients.patientId " +
                            "where inpatient_num = '" + patientId + "' " +
                            "limit 1";
                } else {
                    sql = "select patients.* from patients " +
                            "where patientId = '" + patientId + "'";
                }
                return PatientDAO.getPatient(sql);
            }
        };

        task.setOnSucceeded(event -> {
            Patient patient = task.getValue();
            if (patient != null) {
                patientNumber.setText(patient.getPatientId());
                sex.setText(patient.getSexuality());
                firstName.setText(patient.getFirstName());
                lastName.setText(patient.getLastName());
                dateOfBirth.setText(DateUtil.formatDate(patient.getDateOfBirth()));
                age.setText(patient.getPatientAge());
            }
            if (entry.get(CATEGORY).equals(PatientCategory.INPATIENT.toString())) {
                patientNumber.setText(patientId);
            }
        });
        new Thread(task).start();
    }

    @FXML
    private void onSubmitResults() {
        if (validInput()) {
            LabTestResult result = createLabTestResult();

            if (DBUtil.saveLabTestResults(result)) {
                AlertUtil.showAlert("Results Saved", "Results for lab request have been successfully submitted!", Alert.AlertType.INFORMATION);
                if (entry.get(CATEGORY).equals(PatientCategory.WALK_IN.toString())) {
                    String sql = "update queues set status = '" + PatientQueue.Status.DISCHARGED + "' where DoctorId = 0 and PatientId = '" + entry.get(PATIENT_ID) + "'";
                    DBUtil.executeStatement(sql);
                }
                if (entry.get(CATEGORY).equals(PatientCategory.OUTPATIENT.toString())) {
                    DBUtil.saveLabResultNotification(getVisitId(entry.get(PATIENT_ID)));
                }
                stage.close();
            } else {
                AlertUtil.showAlert("Error", "An error occurred while attempting to save results", Alert.AlertType.ERROR);
            }
        }
    }

    private int getVisitId(String patientId) {
        ResultSet resultSet = DBUtil.executeQuery("select visitId from queues where patientID = '" + patientId + "' and status !='" + PatientQueue.Status.DISCHARGED + "'");
        try {
            if (resultSet != null && resultSet.next()) {
                return resultSet.getInt("visitId");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private LabTestResult createLabTestResult() {
        LabTestResult result = new LabTestResult();
        result.setSpecimen(specimenChoiceBox.getValue());
        result.setRequestId(Integer.parseInt(entry.get(REQUEST_ID)));
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < tableView.getItems().size() - 1; i++) {
            builder.append(tableView.getItems().get(i).getResult());
            builder.append(",");
        }
        builder.append(tableView.getItems().size() - 1);
        result.setResult(builder.toString());
        result.setComment(remarks.getText());
        return result;
    }

    private boolean validInput() {
        Specimen specimen = specimenChoiceBox.getValue();
        if (specimen == null) {
            AlertUtil.showAlert("Specimen Required", "Please select specimen from the available choices", Alert
                    .AlertType.ERROR);
            return false;
        }
        return true;
    }

    @FXML
    private void onPrintLabResults() {
        new PrintLabReport().showReport(entry.get(REQUEST_ID));
    }
}
