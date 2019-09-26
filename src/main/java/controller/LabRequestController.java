package main.java.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import main.java.dao.LabTestDAO;
import main.java.model.*;
import main.java.util.AlertUtil;
import main.java.util.CurrencyUtil;
import main.java.util.DBUtil;
import main.java.util.DateUtil;
import org.controlsfx.control.textfield.AutoCompletionBinding;
import org.controlsfx.control.textfield.TextFields;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Created by alfonce on 03/06/2017.
 */
public class LabRequestController {
    @FXML
    private VBox container;
    @FXML
    private TextField searchField;
    @FXML
    private TableView<LabRequest> tableView;
    @FXML
    private ChoiceBox<Specimen> specimenChoiceBox;
    @FXML
    private TableColumn<LabRequest, String> date, sample, testName, cost, status, options;
    private LabTest autoCompleteResult = null;
    private Stage stage;
    private int queueId, requestId;
    private boolean inpatient;
    private int billNumber;
    private String patientId;

    void setStage(Stage stage) {
        this.stage = stage;
    }

    @FXML
    public void initialize() {
        setUpCurrentTestsTable();
        setUpAutoComplete();
        specimenChoiceBox.setItems(FXCollections.observableArrayList(Specimen.values()));
        specimenChoiceBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            searchField.requestFocus();
        });
    }

    private void setUpAutoComplete() {
        Task<ObservableList<LabTest>> task = new Task<ObservableList<LabTest>>() {
            @Override
            protected ObservableList<LabTest> call() {
                return LabTestDAO.getAllLabTests();
            }
        };
        task.setOnSucceeded(event -> {
            AutoCompletionBinding<LabTest> binding = TextFields.bindAutoCompletion(searchField, task.getValue());
            binding.setOnAutoCompleted(autoCompletionEvent -> {
                autoCompleteResult = autoCompletionEvent.getCompletion();
            });
        });

        new Thread(task).start();
    }

    void setParameters(int queueId, String patientId, boolean inpatient) {
        this.queueId = queueId;
        this.inpatient = inpatient;
        this.patientId = patientId;
        getExistingRequests();
    }

    private void getExistingRequests() {
        Task<ObservableList<LabRequest>> task = new Task<ObservableList<LabRequest>>() {
            @Override
            protected ObservableList<LabRequest> call() {
                String sql = "select lab_requests.*, TestName, Cost from lab_requests " +
                        "inner join labtests on labtests.TestId = lab_requests.TestId ";
                if (inpatient) {
                    sql += " where AdmissionNum = " + queueId;
                } else {
                    sql += " where QueueId = " + queueId;
                }
                return LabTestDAO.getLabRequests(sql);
            }
        };
        task.setOnSucceeded(event -> {
            tableView.setItems(task.getValue());
        });
        new Thread(task).start();
    }

    private LabRequest createNewRequest(int requestId) {
        LabRequest labRequest = new LabRequest();
        labRequest.setId(requestId);
        labRequest.setPatientId(patientId);
        labRequest.setSpecimen(specimenChoiceBox.getValue());
        labRequest.setTimeCreated(LocalTime.now());
        labRequest.setDateCreated(LocalDate.now());
        if (inpatient) {
            labRequest.setAdmissionNum(queueId);
        } else {
            labRequest.setQueueNum(queueId);
        }
        labRequest.setName(autoCompleteResult.getName());
        labRequest.setTestId(autoCompleteResult.getTestId());
        labRequest.setCost(autoCompleteResult.getCost());
        return labRequest;
    }

    private void setUpCurrentTestsTable() {
        //col widths
        for (TableColumn tableColumn : tableView.getColumns()) {
            if (tableColumn == testName || tableColumn == date) {
                tableColumn.prefWidthProperty().bind(tableView.widthProperty().divide(4));
            } else {
                tableColumn.prefWidthProperty().bind(tableView.widthProperty().divide(8));
            }
        }

        //selected tests columns
        Label label = new Label("No tests have been requested!");
        label.getStyleClass().add("text-danger");
        tableView.setPlaceholder(label);

        testName.setCellValueFactory(param -> param.getValue().nameProperty());
        cost.setCellValueFactory(param -> CurrencyUtil.getStringProperty(param.getValue().getCost()));
        sample.setCellValueFactory(param -> param.getValue().specimenStringProperty());
        date.setCellValueFactory(param -> new SimpleStringProperty(DateUtil.formatDateTime(LocalDateTime.of(param.getValue().getDateCreated(), param.getValue().getTimeCreated()))));
        status.setCellValueFactory(param -> param.getValue().statusProperty());

        options.setCellFactory(param -> new TableCell<LabRequest, String>() {
            @Override
            public void updateItem(String value, boolean empty) {
                super.updateItem(value, empty);
                int index = getIndex();
                if (index >= 0 && index < tableView.getItems().size()) {
                    Button button = new Button("Results");
                    button.getStyleClass().add("btn-success-outline");
                    button.setOnAction(event -> {
                        LabRequest request = tableView.getItems().get(index);
                        viewResults(request);
                    });
                    if (tableView.getItems().get(index).getStatus() == LabRequest.Status.PENDING) {
                        button.setDisable(true);
                    }
                    setGraphic(button);
                } else {
                    setGraphic(null);
                }
            }
        });
    }

    private void viewResults(LabRequest request) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource
                    ("main/resources/view/lab-test-result.fxml"));
            Stage stage = new Stage();
            stage.setScene(new Scene(loader.load()));
            stage.initModality(Modality.WINDOW_MODAL);
            stage.setResizable(false);
            stage.setTitle("Lab Test Results");
            stage.initOwner(container.getScene().getWindow());
            LabTestResultController controller = loader.getController();
            controller.setStage(stage);
            controller.setParameters(request.getId(), request.getTestId());

            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean updatePatientBill(int requestId) {
        Patient patient = getPatient();
        Bill bill = new Bill();
        bill.setId(requestId);
        bill.setInsurer(patient.getInsurer());
        bill.setInsuranceId(patient.getInsuranceID());
        bill.setDateCreated(LocalDate.now());
        bill.setCategory(Bill.Category.LAB);
        bill.setPatientNumber(patient.getPatientId());
        bill.setDescription(autoCompleteResult.getName() + " (Lab Test)");
        if (inpatient) {
            bill.setAdmissionNumber(queueId);
        } else {
            bill.setQueueNumber(queueId);
        }
        bill.setBillNumber(billNumber);
        bill.setAmount(autoCompleteResult.getCost());
        return DBUtil.saveBill(bill);
    }

    private Patient getPatient() {
        Patient patient = new Patient();
        String sql;
        if (inpatient) {
            sql = "select Patients.PatientId, InsuranceProvider, InsuranceId, bill_number from patients " +
                    "inner" +
                    " join " +
                    "inpatients " +
                    "on inpatients.patient_id = patients.PatientId " +
                    "where admission_num = " + queueId;
        } else {
            sql = "select Patients.PatientId, InsuranceProvider, InsuranceId, bill_number from patients " +
                    "inner join queues on queues.patientId = patients.patientId " +
                    "where VisitId = " + queueId;
        }
        ResultSet resultSet = DBUtil.executeQuery(sql);
        try {
            if (resultSet != null && resultSet.next()) {
                patient.setInsurer(resultSet.getString("InsuranceProvider"));
                patient.setInsuranceID(resultSet.getString("InsuranceId"));
                patient.setPatientId(resultSet.getString("PatientId"));
                billNumber = resultSet.getInt("bill_number");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return patient;
    }

    @FXML
    private void onSendRequest() {
        Task<Integer> task = new Task<Integer>() {
            @Override
            protected Integer call() throws Exception {
                return 1 + Math.max(DBUtil.getNextAutoIncrementId("billing"), DBUtil.getNextAutoIncrementId("lab_requests"));
            }
        };

        task.setOnSucceeded(event -> {
            if (autoCompleteResult != null) {
                boolean error;
                LabRequest request = createNewRequest(task.getValue());
                error = !DBUtil.addLabRequest(request) || !updatePatientBill(task.getValue());
                if (!error) {
                    AlertUtil.showAlert("Lab Request", "Request has been successfully submitted", Alert.AlertType
                            .INFORMATION);
                    tableView.getItems().add(request);
                    autoCompleteResult = null;
                    searchField.clear();
                    specimenChoiceBox.setValue(null);
                    specimenChoiceBox.requestFocus();
                } else {
                    AlertUtil.showAlert("Error", "An error occurred", Alert.AlertType.ERROR);
                }

            } else {
                AlertUtil.showAlert("Lab Request", "Please enter name of test", Alert.AlertType.ERROR);
            }
        });

        new Thread(task).start();
    }
}
