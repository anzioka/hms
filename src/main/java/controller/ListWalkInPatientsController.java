package main.java.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import main.java.model.ServiceType;
import main.java.util.DBUtil;
import main.java.util.DateUtil;
import main.java.util.StringUtil;

import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

public class ListWalkInPatientsController {
    private static final String NAME = "name";
    private static final String BIRTH_DATE = "birth";
    private static final String SEX = "sex";
    private static final String TIME = "time";
    private static final String VISIT_ID = "visit_id";
    private static final String PATIENT_ID = "patient_id";
    @FXML
    private TableView<Map<String, String>> tableView;
    @FXML
    private TableColumn<Map<String, String>, String> name, birthDate, sex, timeRegistered, option;
    private boolean patientSelected;
    private Stage stage;
    private String[] parameters;
    private ServiceType requestType;

    @FXML
    private void initialize() {
        setUpTable();
    }

    private void getPatientsList() {
        Task<ObservableList<Map<String, String>>> task = new Task<ObservableList<Map<String, String>>>() {
            @Override
            protected ObservableList<Map<String, String>> call() throws Exception {
                ObservableList<Map<String, String>> list = FXCollections.observableArrayList();
                String sql = "select Patients.PatientId, firstName, lastName, DateOfBirth, sex, queues.DateCreated, queues.TimeCreated, visitId " +
                        "from patients " +
                        "inner join queues on patients.PatientId = queues.PatientId " +
                        "where Queues.ServiceType = '" + requestType.name() + "' and DoctorId = 0 and Queues.DateCreated =" +
                        " '" + LocalDate.now() + "'";
                ResultSet resultSet = DBUtil.executeQuery(sql);
                if (resultSet != null) {
                    while (resultSet.next()) {
                        Map<String, String> map = new HashMap<>();
                        map.put(NAME, resultSet.getString("FirstName") + " " + resultSet.getString("LastName"));
                        map.put(BIRTH_DATE, DateUtil.formatDateLong(resultSet.getObject("DateOfBirth", LocalDate.class)));
                        map.put(SEX, resultSet.getString("Sex"));
                        LocalDate date = resultSet.getObject("DateCreated", LocalDate.class);
                        LocalTime time = resultSet.getObject("TimeCreated", LocalTime.class);
                        map.put(TIME, DateUtil.formatDateTime(LocalDateTime.of(date, time)));
                        map.put(VISIT_ID, resultSet.getString("VisitId"));
                        map.put(PATIENT_ID, resultSet.getString("PatientId"));
                        list.add(map);
                    }
                }
                return list;
            }
        };
        task.setOnSucceeded(event -> {
            tableView.setItems(task.getValue());
        });
        new Thread(task).start();
    }

    private void setUpTable() {
        Label label = new Label("No walk-in patients found!");
        label.getStyleClass().add("text-danger");
        tableView.setPlaceholder(label);

        name.setCellValueFactory(param -> StringUtil.getStringProperty(param.getValue().get(NAME)));
        birthDate.setCellValueFactory(param -> StringUtil.getStringProperty(param.getValue().get(BIRTH_DATE)));
        sex.setCellValueFactory(param -> StringUtil.getStringProperty(param.getValue().get(SEX)));
        timeRegistered.setCellValueFactory(param -> StringUtil.getStringProperty(param.getValue().get(TIME)));
        option.setCellFactory(param -> new TableCell<Map<String, String>, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                int index = getIndex();
                if (index >= 0 && index < tableView.getItems().size()) {
                    Button button = new Button("Select");
                    button.getStyleClass().add("btn-info-outline");
                    button.setOnAction(event -> {
                        selectEntry(index);
                    });
                    setGraphic(button);
                } else {
                    setGraphic(null);
                }
            }
        });
    }

    private void selectEntry(int index) {
        patientSelected = true;
        Map<String, String> entry = tableView.getItems().get(index);
        setParameters(new String[]{entry.get(PATIENT_ID), entry.get(VISIT_ID)});
        stage.close();
    }

    boolean isPatientSelected() {
        return patientSelected;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    String[] getParameters() {
        return parameters;
    }

    public void setParameters(String[] parameters) {
        this.parameters = parameters;
    }

    void setRequestType(ServiceType requestType) {
        this.requestType = requestType;
        getPatientsList();
    }

}
