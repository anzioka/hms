package main.java.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Modality;
import javafx.stage.Stage;
import main.Main;
import main.java.dao.RadiologyDao;
import main.java.model.Bill;
import main.java.model.PaymentMode;
import main.java.model.RadiologyItem;
import main.java.model.RadiologyRequest;
import main.java.util.AlertUtil;
import main.java.util.CurrencyUtil;
import main.java.util.DBUtil;
import main.java.util.DateUtil;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

public class CreateRadiologyRequestController {
    private final String REQUEST_ID = "request_id";
    private final String RESULT = "result";
    private final String COST = "cost";
    private final String DATE = "date";
    private final String DOCTOR = "doctor";
    private final String DOCTOR_ID = "doctor_id";
    private final String TEST_CATEGORY = "test_category";
    private final String DESCRIPTION = "description";
    private final String STATUS = "status";

    private String patientId, insurer, insuranceId;
    private int queueNumber, billNumber;
    private boolean outpatientMode;
    private PaymentMode paymentMode;
    @FXML
    private VBox container;
    @FXML
    private ChoiceBox<String> radiologyCategoryChoiceBox, descriptionChoiceBox;
    @FXML
    private TableView<Map<String, String>> tableView;
    @FXML
    private TableColumn<Map<String, String>, String> cost, test, date, option, doctor;
    @FXML
    private TextField costField;
    private ObservableList<RadiologyItem> radiologyItems = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        setUpTable();
        getRadiologyTests();
        radiologyCategoryChoiceBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue,
                                                                                           newValue) -> {
            filterResults(newValue);
            descriptionChoiceBox.requestFocus();
        });
        descriptionChoiceBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue)
                -> {
            costField.setText(getCost(newValue));
        });
    }

    private String getCost(String newValue) {
        if (radiologyCategoryChoiceBox.getValue() != null) {
            for (RadiologyItem item : radiologyItems) {
                if (item.getCategory().equals(radiologyCategoryChoiceBox.getValue()) && item.getDescription().equals
                        (newValue)) {
                    return CurrencyUtil.formatCurrency(item.getCost());
                }
            }
        }
        return null;
    }

    private void filterResults(String category) {
        ObservableList<String> descriptions = FXCollections.observableArrayList();
        for (RadiologyItem item : radiologyItems) {
            if (item.getCategory().equals(category)) {
                descriptions.add(item.getDescription());
            }
        }
        descriptionChoiceBox.setItems(descriptions);
    }

    private void getRadiologyTests() {
        Task<ObservableList<RadiologyItem>> task = new Task<ObservableList<RadiologyItem>>() {
            @Override
            protected ObservableList<RadiologyItem> call() {
                return RadiologyDao.getAllRadiologyItems();
            }
        };
        task.setOnSucceeded(event -> {
            radiologyItems = task.getValue();
            Set<String> set = new HashSet<>();
            for (RadiologyItem item : radiologyItems) {
                set.add(item.getCategory());
                descriptionChoiceBox.getItems().add(item.getDescription());
            }
            radiologyCategoryChoiceBox.setItems(FXCollections.observableArrayList(set));
        });
        new Thread(task).start();
    }

    private void setUpTable() {
        Label label = new Label("No requests sent!");
        label.getStyleClass().add("text-danger");
        tableView.setPlaceholder(label);
        cost.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().get(COST)));
        date.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().get(DATE)));
        doctor.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().get(DOCTOR)));
        option.setCellFactory(param -> new TableCell<Map<String, String>, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                int index = getIndex();
                if (index >= 0 && index < tableView.getItems().size()) {
                    //cancel request, view result;
                    Map<String, String> entry = tableView.getItems().get(index);
                    Button cancel = new Button("Cancel");
                    cancel.setDisable(Main.currentUser.getUserId() != Integer.parseInt(entry.get(DOCTOR_ID)) || entry
                            .get(STATUS).equals(RadiologyRequest.Status.COMPLETED.toString()));
                    cancel.getStyleClass().add("btn-danger-outline");
                    cancel.setOnAction(event -> {
                        cancelRequest(tableView.getItems().get(index));
                    });

                    Button result = new Button("Result");
                    result.setOnAction(event -> {
                        viewResult(tableView.getItems().get(index));
                    });
                    result.setDisable(entry.get(STATUS).equals(RadiologyRequest.Status.PENDING.toString()));
                    result.getStyleClass().add("btn-info-outline");
                    VBox vBox = new VBox(5.0, result, cancel);
                    vBox.setAlignment(Pos.CENTER);
                    setGraphic(vBox);
                } else {
                    setGraphic(null);
                }
            }
        });
        test.setCellFactory(param -> new TableCell<Map<String, String>, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                int index = getIndex();
                if (index >= 0 && index < tableView.getItems().size()) {
                    Map<String, String> entry = tableView.getItems().get(index);
                    Text text = new Text(entry.get(TEST_CATEGORY) + " (" + entry.get(DESCRIPTION) + ")");
                    text.wrappingWidthProperty().bind(test.widthProperty());
                    text.setTextAlignment(TextAlignment.CENTER);
                    setPrefHeight(Control.USE_COMPUTED_SIZE);
                    setGraphic(text);
                } else {
                    setGraphic(null);
                }
            }
        });
    }

    private void cancelRequest(Map<String, String> data) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to cancel request?", ButtonType
                .OK, ButtonType.CANCEL);
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (DBUtil.executeStatement("delete from radiology_requests where request_id = " + data.get(REQUEST_ID))) {
                if (DBUtil.executeStatement("delete from billing where id = " + data.get(REQUEST_ID))) {
                    AlertUtil.showAlert("", "Request successfully cancelled and patient bill updated!", Alert.AlertType.INFORMATION);
                } else{
                    AlertUtil.showGenericError();
                }
            } else {
                AlertUtil.showGenericError();
            }
        }
    }

    private void viewResult(Map<String, String> data) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource
                    ("main/resources/view/radiology_result.fxml"));
            Stage stage = new Stage();
            stage.setScene(new Scene(loader.load()));
            stage.setResizable(false);
            stage.initModality(Modality.WINDOW_MODAL);
            RadiologyResultController controller = loader.getController();
            stage.initOwner(container.getScene().getWindow());
            controller.setParameters(data.get(DATE), data.get(DOCTOR), data.get(RESULT), data.get(TEST_CATEGORY),
                    data.get(DESCRIPTION));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void setPatientInfo(int queueNumber, String patientId, boolean outpatientMode) {
        this.queueNumber = queueNumber;
        this.patientId = patientId;
        this.outpatientMode = outpatientMode;
        if (outpatientMode) {
            tableView.getColumns().remove(doctor);
        }
        getPatientDetails();
        getRequests();
    }

    private void getRequests() {
        Task<ObservableList<Map<String, String>>> task = new Task<ObservableList<Map<String, String>>>() {
            @Override
            protected ObservableList<Map<String, String>> call() throws Exception {
                ObservableList<Map<String, String>> data = FXCollections.observableArrayList();
                String sql = "select request_id, date, time, status, result, doctor_id, users.LastName, " +
                        "category, description, cost " +
                        "from radiology_requests " +
                        "inner join users on users.id = radiology_requests.doctor_id " +
                        "inner join radiology_items on radiology_items.id = radiology_requests.item_id ";
                if (outpatientMode) {
                    sql += " where visit_id = " + queueNumber;
                } else{
                    sql += " where admission_num = " + queueNumber;
                }
                ResultSet resultSet = DBUtil.executeQuery(sql);
                if (resultSet != null) {
                    while (resultSet.next()) {
                        Map<String, String> entry = new HashMap<>();
                        LocalDateTime dateTime = LocalDateTime.of(resultSet.getObject("date", LocalDate.class),
                                resultSet.getObject("time", LocalTime.class));
                        entry.put(DATE, DateUtil.formatDateTime(dateTime));
                        entry.put(REQUEST_ID, resultSet.getString("request_id"));
                        entry.put(STATUS, RadiologyRequest.Status.valueOf(resultSet.getString("status")).toString());
                        entry.put(RESULT, resultSet.getString("result"));
                        entry.put(DOCTOR_ID, resultSet.getString("doctor_id"));
                        entry.put(DOCTOR, resultSet.getString("users.LastName"));
                        entry.put(TEST_CATEGORY, resultSet.getString("Category"));
                        entry.put(DESCRIPTION, resultSet.getString("description"));
                        entry.put(COST, CurrencyUtil.formatCurrency(resultSet.getDouble("cost")));
                        data.add(entry);
                    }
                }
                return data;
            }
        };
        task.setOnSucceeded(event -> {
            tableView.setItems(task.getValue());
        });
        new Thread(task).start();
    }

    private void getPatientDetails() {

        String sql = "SELECT  Patients.PatientId, payment_mode, bill_number, InsuranceProvider, InsuranceId " +
                "from Queues " +
                "inner join patients on patients.patientId = queues.patientId " +
                "where queues.visitId = " + queueNumber;
        if (!outpatientMode) {
            sql = "SELECT Patients.PatientId, Patients.FirstName, " +
                    "Patients.LastName, bill_number, payment_mode, InsuranceProvider, InsuranceId " +
                    "from inpatients " +
                    "inner join patients on patients.patientId = inpatients.patient_Id " +
                    "where admission_num = " + queueNumber;
        }
        try {
            ResultSet resultSet = DBUtil.executeQuery(sql);
            if (resultSet != null) {
                if (resultSet.next()) {
                    billNumber = resultSet.getInt("bill_number");
                    insurer = resultSet.getString("InsuranceProvider");
                    insuranceId = resultSet.getString("insuranceId");
                    patientId = resultSet.getString("patientId");
                    paymentMode = PaymentMode.valueOf(resultSet.getString("payment_mode"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onSendRequest() {
        if (isValidInput()) {
            int reqId = 1 + Math.max(DBUtil.getNextAutoIncrementId("radiology_requests"), DBUtil
                    .getNextAutoIncrementId("billing"));

            RadiologyRequest request = createRequest(reqId);
            if (request != null) {
                if ((DBUtil.saveRadiologyRequest(request)) && DBUtil.saveBill(createBill(reqId))) {
                    AlertUtil.showAlert("", "Radiology request has been successfully submitted",
                            Alert.AlertType.INFORMATION);
                    costField.clear();
                    radiologyCategoryChoiceBox.getSelectionModel().clearSelection();
                    descriptionChoiceBox.getSelectionModel().clearSelection();
                    getRequests();
                } else {
                    AlertUtil.showGenericError();
                }
            }

        }
    }

    private Bill createBill(Integer billId) {
        Bill bill = new Bill();
        bill.setId(billId);
        bill.setDescription(radiologyCategoryChoiceBox.getValue() + " (" + descriptionChoiceBox.getValue() + ")");
        bill.setAmount(CurrencyUtil.parseCurrency(costField.getText()));
        bill.setBillNumber(billNumber);
        bill.setCategory(Bill.Category.RADIOLOGY);
        bill.setPatientNumber(patientId);

        if (outpatientMode) {
            bill.setQueueNumber(queueNumber);
        } else {
            bill.setAdmissionNumber(queueNumber);
        }
        if (paymentMode == PaymentMode.INSURANCE) {
            bill.setInsurer(insurer);
            bill.setInsuranceId(insuranceId);
        }
        bill.setDateCreated(LocalDate.now());
        return bill;
    }

    private RadiologyRequest createRequest(Integer requestId) {
        RadiologyRequest request = new RadiologyRequest();
        request.setDateCreated(LocalDate.now());
        request.setTimeCreated(LocalTime.now());
        request.setDescription(descriptionChoiceBox.getValue());
        request.setCategory(radiologyCategoryChoiceBox.getValue());
        request.setPatientId(patientId);
        int testId = getTestId();
        if (testId == -1) {
            return null;
        } else {
            request.setId(testId);
        }
        request.setRequestId(requestId);
        if (outpatientMode) {
            request.setVisitId(queueNumber);
        } else {
            request.setAdmissionId(queueNumber);
        }

        return request;
    }

    private int getTestId() {
        ResultSet resultSet = DBUtil.executeQuery("select id from radiology_items where category = '" + radiologyCategoryChoiceBox
                .getValue() + "' and description = '" + descriptionChoiceBox.getValue() + "'");
        try {
            if (resultSet != null && resultSet.next()) {
                return resultSet.getInt("id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    private boolean isValidInput() {
        String error = "";
        if (radiologyCategoryChoiceBox.getValue() == null) {
            error += "Select category!\n";
        }
        if (descriptionChoiceBox.getValue() == null) {
            error += "Select description!\n";
        }
        if (error.isEmpty()) {
            return true;
        }
        AlertUtil.showAlert("", error, Alert.AlertType.ERROR);
        return false;
    }
}
