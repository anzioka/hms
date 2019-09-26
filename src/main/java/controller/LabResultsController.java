package main.java.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import main.java.model.LabRequest;
import main.java.util.DBUtil;
import main.java.util.DateUtil;
import main.java.util.StringUtil;

import java.io.IOException;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by alfonce on 01/08/2017.
 */
public class LabResultsController {
    private static final String DATE = "date";
    private static final String PATIENT_NAME = "patient_name";
    private static final String REQUESTER = "requester";
    private static final String PATIENT_NUM = "patient_no";
    private static final String TEST_NAME = "test";
    private static final String TEST_ID = "test_id";
    private static final String REQUEST_ID = "request";
    @FXML
    private AnchorPane container;
    @FXML
    private TableView<Map<String, String>> tableView;
    @FXML
    private TableColumn<Map<String, String>, String> date, patientName, requester, options, patientNo, testName;
    @FXML
    private DatePicker startDate, endDate;

    @FXML
    public void initialize() {
        startDate.setConverter(DateUtil.getDatePickerConverter());
        endDate.setConverter(DateUtil.getDatePickerConverter());
        setUpTable();
        getLabResults();
    }

    private void getLabResults() {
        LocalDate start = startDate.getValue() != null ? startDate.getValue() : LocalDate.now();
        startDate.setValue(start);
        LocalDate end = endDate.getValue() != null ? endDate.getValue() : LocalDate.now();
        endDate.setValue(end);
        Task<ObservableList<Map<String, String>>> task = new Task<ObservableList<Map<String, String>>>() {
            @Override
            protected ObservableList<Map<String, String>> call() throws Exception {
                ObservableList<Map<String, String>> list = FXCollections.observableArrayList();
                String sql = "select lab_requests.TimeCreated, lab_requests.AdmissionNum, lab_requests.DateCreated, lab_requests.Id, LabTests.TestName, Lab_requests.TestId, Patients.FirstName, Patients.LastName, Users.FirstName, Users.LastName, inpatients.inpatient_num, queues.PatientId\n" +
                        "from lab_requests\n" +
                        "inner join LabTests on LabTests.TestId = lab_requests.TestId\n" +
                        "inner join users on lab_requests.UserId = Users.Id\n" +
                        "left join inpatients on inpatients.admission_num = lab_requests.AdmissionNum\n" +
                        "inner join patients on patients.PatientID = lab_requests.PatientId\n" +
                        "left join queues on queues.VisitId = lab_requests.QueueId " +
                        "where lab_requests.Status = '" + LabRequest.Status.COMPLETED + "' " +
                        "and lab_requests.DateCreated between '" + start + "' and '" + end + "'";
                ResultSet resultSet = DBUtil.executeQuery(sql);
                if (resultSet != null) {
                    while (resultSet.next()) {
                        Map<String, String> map = new HashMap<>();
                        LocalDate date = resultSet.getObject("DateCreated", LocalDate.class);
                        LocalTime time = resultSet.getObject("TimeCreated", LocalTime.class);
                        map.put(DATE, DateUtil.formatDateTime(LocalDateTime.of(date, time)));
                        map.put(REQUEST_ID, resultSet.getString("Id"));
                        map.put(TEST_NAME, resultSet.getString("TestName"));
                        map.put(PATIENT_NAME, resultSet.getString("Patients.FirstName") + " " + resultSet.getString("Patients.LastName"));
                        map.put(REQUESTER, resultSet.getString("Users.FirstName") + " " + resultSet.getString("Users.LastName"));
                        if (resultSet.getInt("AdmissionNum") == -1) {
                            //not inpatient
                            map.put(PATIENT_NUM, resultSet.getString("PatientId"));
                        } else {
                            map.put(PATIENT_NUM, resultSet.getString("inpatient_num"));
                        }
                        map.put(TEST_ID, resultSet.getString("TestId"));
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
        //place holder
        Label label = new Label("No lab results found!");
        label.getStyleClass().add("text-danger");
        tableView.setPlaceholder(label);

        date.setCellValueFactory(param -> StringUtil.getStringProperty(param.getValue().get(DATE)));
        patientName.setCellValueFactory(param -> StringUtil.getStringProperty(param.getValue().get(PATIENT_NAME)));
        requester.setCellValueFactory(param -> StringUtil.getStringProperty(param.getValue().get(REQUESTER)));
        patientNo.setCellValueFactory(param -> StringUtil.getStringProperty(param.getValue().get(PATIENT_NUM)));
        testName.setCellValueFactory(param -> StringUtil.getStringProperty(param.getValue().get(TEST_NAME)));
        options.setCellFactory(param -> new TableCell<Map<String, String>, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                int index = getIndex();
                if (index >= 0 && index < tableView.getItems().size()) {
                    Button button = new Button("View Results");
                    button.getStyleClass().add("btn-info-outline");
                    button.setOnAction(event -> {
                        viewResults(tableView.getItems().get(index));
                    });
                    setGraphic(button);
                } else {
                    setGraphic(null);
                }
            }
        });
    }

    private void viewResults(Map<String, String> entry) {
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
            controller.setParameters(Integer.parseInt(entry.get(REQUEST_ID)), Integer.parseInt(entry.get(TEST_ID)));

            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onSearch() {
        getLabResults();
    }
}
