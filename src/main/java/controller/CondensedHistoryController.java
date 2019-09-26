package main.java.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import main.java.model.Patient;
import main.java.model.PatientCategory;
import main.java.util.DBUtil;
import main.java.util.DateUtil;

import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

public class CondensedHistoryController {
    private final String VISIT_DATE = "visit_date";
    private final String VISIT_TIME = "visit_time";
    private final String OUT_DOC = "out_doctor";
    private final String ADMISSION_DATE = "admission_date";
    private final String ADMISSION_TIME = "admission_time";
    private final String DISCHARGE_DATE = "discharge_date";
    private final String DISCHARGE_TIME = "discharge_time";
    private final String IN_DOC = "in_doctor";
    @FXML
    private ChoiceBox<PatientCategory> patientCategoryChoiceBox;
    @FXML
    private TableView<Map<String, String>> visitsTableView, admissionsTableView;
    @FXML
    private TableColumn<Map<String, String>, String> visitDate, visitTime, inDoctor, admissionDate, admissionTime, dischargeDate, dischargeTime, outDoctor;
    @FXML
    private DatePicker startDate, endDate;
    @FXML
    private Label title;
    private Patient patient;

    @FXML
    private void initialize() {
        setUpTables();
        endDate.setConverter(DateUtil.getDatePickerConverter());
        endDate.setValue(LocalDate.now());

        startDate.setConverter(DateUtil.getDatePickerConverter());
        startDate.setValue(LocalDate.now().minusDays(60));

        patientCategoryChoiceBox.setItems(FXCollections.observableArrayList(PatientCategory.INPATIENT, PatientCategory.OUTPATIENT));
        patientCategoryChoiceBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            visitsTableView.setVisible(newValue == PatientCategory.OUTPATIENT);
            visitsTableView.setManaged(newValue == PatientCategory.OUTPATIENT);

            admissionsTableView.setVisible(newValue == PatientCategory.INPATIENT);
            admissionsTableView.setManaged(newValue == PatientCategory.INPATIENT);
            onSearchRecords();
        });
        patientCategoryChoiceBox.setValue(PatientCategory.OUTPATIENT);

    }

    private void setUpTables() {
        Label label = new Label("No visit history found!");
        label.getStyleClass().add("text-danger");
        visitsTableView.setPlaceholder(label);

        label = new Label("No admission history found!");
        label.getStyleClass().add("text-danger");
        admissionsTableView.setPlaceholder(label);

        visitDate.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().get(VISIT_DATE)));
        visitTime.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().get(VISIT_TIME)));
        outDoctor.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().get(OUT_DOC)));

        admissionDate.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().get(ADMISSION_DATE)));
        admissionTime.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().get(ADMISSION_TIME)));
        dischargeDate.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().get(DISCHARGE_DATE)));
        dischargeTime.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().get(DISCHARGE_TIME)));
        inDoctor.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().get(IN_DOC)));
    }

    @FXML
    private void onSearchRecords() {
        PatientCategory patientCategory = patientCategoryChoiceBox.getValue();
        LocalDate start = startDate.getValue() != null ? startDate.getValue() : LocalDate.now().minusDays(60);
        LocalDate end = endDate.getValue() != null ? endDate.getValue() : LocalDate.now();
        if (patientCategory == PatientCategory.OUTPATIENT) {
            getVisitHistory(start, end);
        } else {
            getAdmissionHistory(start, end);
        }
    }

    private void getAdmissionHistory(LocalDate start, LocalDate end) {
        Task<ObservableList<Map<String, String>>> task = new Task<ObservableList<Map<String, String>>>() {
            @Override
            protected ObservableList<Map<String, String>> call() throws Exception {
                ObservableList<Map<String, String>> items = FXCollections.observableArrayList();
                ResultSet resultSet = DBUtil.executeQuery("select date_admitted, date_discharged, time_admitted, time_discharged, Users.LastName " +
                        "from inpatients " +
                        "inner join users on users.Id = inpatients.doctor_id " +
                        "where ( date_admitted between '" + start + "' and '" + end + "' " +
                        "or date_discharged between '" + start + "' and '" + end + "') " +
                        "and patient_id = '" + patient.getPatientId() + "'");
                if (resultSet != null) {
                    while (resultSet.next()) {
                        Map<String, String> entry = new HashMap<>();
                        entry.put(ADMISSION_DATE, DateUtil.formatDateLong(resultSet.getObject("date_admitted", LocalDate.class)));
                        entry.put(ADMISSION_TIME, DateUtil.formatTime(resultSet.getObject("time_admitted", LocalTime.class)));
                        if (resultSet.getObject("date_discharged") != null) {
                            entry.put(DISCHARGE_DATE, DateUtil.formatDateLong(resultSet.getObject("date_discharged", LocalDate.class)));
                            entry.put(DISCHARGE_TIME, DateUtil.formatTime(resultSet.getObject("time_discharged", LocalTime.class)));
                        } else {
                            entry.put(DISCHARGE_TIME, null);
                            entry.put(DISCHARGE_DATE, null);
                        }
                        entry.put(IN_DOC, resultSet.getString("LastName"));
                        items.add(entry);
                    }
                }
                return items;
            }
        };
        task.setOnSucceeded(event -> {
            admissionsTableView.setItems(task.getValue());
        });
        new Thread(task).start();
    }

    private void getVisitHistory(LocalDate start, LocalDate end) {
        Task<ObservableList<Map<String, String>>> task = new Task<ObservableList<Map<String, String>>>() {
            @Override
            protected ObservableList<Map<String, String>> call() throws Exception {
                ObservableList<Map<String, String>> items = FXCollections.observableArrayList();
                ResultSet resultSet = DBUtil.executeQuery("select queues.DateCreated, TimeCreated, LastName from queues " +
                        "inner join users on users.Id = queues.DoctorId " +
                        "where queues.dateCreated between '" + start + "' and '" + end + "' " +
                        "and patientId = '" + patient.getPatientId() + "' " +
                        "and doctorId != 0");
                if (resultSet != null) {
                    while (resultSet.next()) {
                        Map<String, String> entry = new HashMap<>();
                        entry.put(VISIT_DATE, DateUtil.formatDateLong(resultSet.getObject("DateCreated", LocalDate.class)));
                        entry.put(VISIT_TIME, DateUtil.formatTime(resultSet.getObject("TimeCreated", LocalTime.class)));
                        entry.put(OUT_DOC, resultSet.getString("LastName"));
                        items.add(entry);
                    }
                }
                return items;
            }
        };
        task.setOnSucceeded(event -> {
            visitsTableView.setItems(task.getValue());
        });
        new Thread(task).start();
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
        title.setText(patient.getFirstName() + "'s History");
        onSearchRecords();
    }
}
