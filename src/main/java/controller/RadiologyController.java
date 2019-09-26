package main.java.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import main.java.model.Bill;
import main.java.model.PatientCategory;
import main.java.model.RadiologyRequest;
import main.java.util.DBUtil;
import main.java.util.DateUtil;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class RadiologyController {
    static final String DATE = "date";
    static final String CATEGORY = "category";
    static final String DESCRIPTION = "description";
    static final String DOCTOR = "doctor";
    static final String PATIENT_ID = "patient_id";
    static final String PATIENT_CATEGORY = "patient_category";
    static final String REQUEST_ID = "request_id";
    private final String PAYMENT_STATUS = "payment_status";
    private final String PATIENT = "patient";
    private boolean radiologyPrepay = false;

    @FXML
    private TableView<Map<String, String>> tableView;
    @FXML
    private TableColumn<Map<String, String>, String> date, doctor, patient, category, description, paymentStatus,
            view;
    @FXML
    private TabPane tabPane;

    @FXML
    private void initialize() {
        getRadiologySetting();
        setUpTable();

        tabPane.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.intValue() != 0) {
                loadTab(newValue.intValue());
            }
        });

        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.scheduleAtFixedRate(this::getRequests, 0, 2, TimeUnit.SECONDS);
    }

    private void setUpTable() {
        Label label = new Label("No requests found!");
        label.getStyleClass().add("text-danger");
        tableView.setPlaceholder(label);

        date.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().get(DATE)));
        doctor.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().get(DOCTOR)));
        patient.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().get(PATIENT)));
        paymentStatus.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().get(PAYMENT_STATUS)));
        Callback<TableColumn<Map<String, String>, String>, TableCell<Map<String, String>, String>> cellCallback =
                (param -> new
                TableCellCallback(description.getText()));
        description.setCellFactory(cellCallback);
        cellCallback = (param -> new TableCellCallback(category.getText()));
        category.setCellFactory(cellCallback);

        cellCallback = (param -> new TableCellCallback(view.getText()));
        view.setCellFactory(cellCallback);
    }

    private void viewRequest(Map<String, String> request) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource
                    ("main/resources/view/complete-radiology-request.fxml"));
            Stage stage = new Stage();
            stage.setScene(new Scene(loader.load()));
            stage.setResizable(false);
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(tableView.getScene().getWindow());
            CompleteRadiologyRequestController controller = loader.getController();
            controller.setStage(stage);
            controller.setData(request);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void getRadiologySetting() {
        ResultSet resultSet = DBUtil.executeQuery("select radiology_prepay from general_settings");
        try {
            if (resultSet != null && resultSet.next()) {
                radiologyPrepay = resultSet.getBoolean("radiology_prepay");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void getRequests() {
        Task<ObservableList<Map<String, String>>> task = new Task<ObservableList<Map<String, String>>>() {
            @Override
            protected ObservableList<Map<String, String>> call() throws Exception {
                ObservableList<Map<String, String>> data = FXCollections.observableArrayList();
                String sql = "select billing.status, Patients.FirstName, Patients.LastName, Users.LastName, " +
                        "radiology_items.category, radiology_items.description, request_id, " +
                        "radiology_requests.patient_id, " +
                        "radiology_requests.admission_num, date, time " +
                        "from radiology_requests " +
                        "inner join radiology_items on radiology_requests.item_id = radiology_items.id " +
                        "inner join Users on users.Id = radiology_requests.doctor_id " +
                        "inner join patients on patients.patientId = radiology_requests.patient_id " +
                        "inner join billing on billing.id = radiology_requests.request_id " +
                        "where radiology_requests.status = '" + RadiologyRequest.Status.PENDING + "'";
                ResultSet resultSet = DBUtil.executeQuery(sql);
                if (resultSet != null) {
                    while (resultSet.next()) {
                        Map<String, String> entry = new HashMap<>();
                        if (resultSet.getInt("admission_num") == -1) {
                            //outpatients
                            entry.put(PAYMENT_STATUS, Bill.Status.valueOf(resultSet.getString("status")).toString());
                            entry.put(PATIENT_CATEGORY, PatientCategory.OUTPATIENT.toString());
                        } else {
                            entry.put(PATIENT_CATEGORY, PatientCategory.INPATIENT.toString());
                            entry.put(PAYMENT_STATUS, Bill.Status.PAID.toString());
                        }
                        entry.put(PATIENT_ID, resultSet.getString("patient_id"));
                        entry.put(PATIENT, resultSet.getString("Patients.FirstName") + " " + resultSet.getString
                                ("LastName"));
                        entry.put(DOCTOR, resultSet.getString("Users.LastName"));
                        entry.put(CATEGORY, resultSet.getString("category"));
                        entry.put(DESCRIPTION, resultSet.getString("description"));
                        LocalDateTime dateTime = LocalDateTime.of(resultSet.getObject("date", LocalDate.class),
                                resultSet.getObject("time", LocalTime.class));
                        entry.put(DATE, DateUtil.formatDateTime(dateTime));
                        entry.put(REQUEST_ID, resultSet.getString("request_id"));
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

    private void loadTab(int index) {
        String[] resourceFiles = new String[]{"radiology-items", "all-radiology-results"};
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("main/resources/view/" + resourceFiles[index - 1] + ".fxml"));
            Node node = loader.load();

            Tab tab = tabPane.getTabs().get(index);
            tab.setClosable(false);
            tab.setContent(node);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class TableCellCallback extends TableCell<Map<String, String>, String> {
        private String columnName;

        TableCellCallback(String columnName) {
            this.columnName = columnName;
        }

        @Override
        protected void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            int index = getIndex();
            if (index >= 0 && index < tableView.getItems().size()) {
                Map<String, String> entry = tableView.getItems().get(index);
                if (!entry.get(PAYMENT_STATUS).equals(Bill.Status.PENDING.toString()) || !radiologyPrepay) {
                    if (columnName.equals(description.getText())) {
                        setText(entry.get(DESCRIPTION));
                    } else if (columnName.equals(category.getText())) {
                        setText(entry.get(CATEGORY));
                    } else {
                        Button button = new Button("Complete");
                        button.getStyleClass().add("btn-info-outline");
                        button.setOnAction(event -> {
                            viewRequest(tableView.getItems().get(index));
                        });
                        setGraphic(button);
                    }
                }
            } else {
                setGraphic(null);
                setText(null);
            }
        }
    }
}

