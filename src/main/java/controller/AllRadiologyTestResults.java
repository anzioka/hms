package main.java.controller;

import com.sun.javafx.scene.control.skin.TableCellSkin;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import main.java.model.*;
import main.java.util.*;
import javafx.fxml.FXML;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

public class AllRadiologyTestResults {
    private final String RESULT = "request_id";
    private  final String PATIENT = "patient";
    private  final String DOCTOR = "doctor";
    private  final String TEST_CATEGORY =  "test_category";
    private final String  DESCRIPTION = "description";
    private final   String DATE = "date";
   private final String PATIENT_CATEGORY = "patient_category";
    @FXML
    private VBox container;
    @FXML
    private DatePicker startDate, endDate;
    @FXML

    private TableView<Map<String, String>> tableView;
    @FXML
    private TableColumn<Map<String, String>, String> patientName, doctorName, testCategory, testDescription, date,
            result, patientCategory;
    @FXML
    private TextField patientSearchField;
    private ObservableList<Map<String, String>> data = FXCollections.observableArrayList();
    @FXML
    private void initialize() {
        //by default display results for the last 1 month
        startDate.setConverter(DateUtil.getDatePickerConverter());
        startDate.setValue(LocalDate.now().minusMonths(1));
        endDate.setConverter(DateUtil.getDatePickerConverter());
        endDate.setValue(LocalDate.now());

        onSearch();
        setUpTable();
        patientSearchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterResults(newValue);
        });
    }

    private void filterResults(String searchString) {
        if (searchString == null || searchString.isEmpty()) {
            tableView.setItems(data);
        } else{
            ObservableList<Map<String, String>> filtered = FXCollections.observableArrayList();
            for (Map<String, String> item : data)
                if (item.get(PATIENT).toLowerCase().contains(searchString.toLowerCase())) {
                    filtered.add(item);
                }
            tableView.setItems(filtered);
        }
    }

    private void setUpTable() {
        Label label = new Label("No records found!");
        label.getStyleClass().add("text-danger");
        tableView.setPlaceholder(label);

        patientCategory.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().get(PATIENT_CATEGORY)));
        patientName.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().get(PATIENT)));
        doctorName.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().get(DOCTOR)));
        testCategory.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().get(TEST_CATEGORY)));
        testDescription.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().get(DESCRIPTION)));
        date.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().get(DATE)));
        result.setCellFactory(param -> new TableCell<Map<String, String>, String>(){
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                int index = getIndex();
                if (index >= 0 && index < tableView.getItems().size()) {
                    Button view = new Button("View");
                    view.getStyleClass().add("btn-info-outline");
                    view.setOnAction(event -> {
                        viewResult(tableView.getItems().get(index));
                    });
                    setGraphic(view);
                } else{
                    setGraphic(null);
                }
            }
        });
    }

    private void viewResult(Map<String, String> data) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource
                    ("main/resources/view/radiology_result.fxml"));
            Stage stage = new Stage();
            stage.setScene(new Scene(loader.load()));
            stage.setResizable(false);
            stage.initOwner(container.getScene().getWindow());
            stage.initModality(Modality.WINDOW_MODAL);
            RadiologyResultController controller = loader.getController();
            controller.setParameters(data.get(DATE), data.get(DOCTOR), data.get(RESULT), data.get(TEST_CATEGORY),
                    data.get(DESCRIPTION));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onSearch() {
        LocalDate start = startDate.getValue() != null ? startDate.getValue() :LocalDate.now().minusMonths(1);
        LocalDate end = endDate.getValue() != null ? endDate.getValue() : LocalDate.now();
        Task<ObservableList<Map<String, String>>> task = new Task<ObservableList<Map<String,String>>>() {
            @Override
            protected ObservableList<Map<String, String>> call() throws SQLException {
                return getData(start, end);
            }
        };
        task.setOnSucceeded(event -> {
            data = task.getValue();
            tableView.setItems(task.getValue());
        });
        new Thread(task).start();
    }

    private ObservableList<Map<String, String>> getData(LocalDate start, LocalDate end) throws SQLException {
        ObservableList<Map<String, String>> data = FXCollections.observableArrayList();
        String searchSql = "select admission_num, result, date, time, patients.FirstName, Patients.LastName, " +
                "Users.LastName, category, description " +
                "from radiology_requests " +
                "inner join patients on patients.patientId = radiology_requests.patient_id " +
                "inner join users on users.Id = radiology_requests.doctor_id " +
                "inner join radiology_items on radiology_items.id = radiology_requests.item_id " +
                "where date between '" + start + "' and '" + end + "' " +
                "and status = '" + RadiologyRequest.Status.COMPLETED + "'";
        ResultSet resultSet = DBUtil.executeQuery(searchSql);
        if (resultSet != null) {
            while (resultSet.next()) {
                Map<String, String> item = new HashMap<>();
                item.put(RESULT, resultSet.getString("result"));
                item.put(PATIENT, resultSet.getString("patients.FirstName") + " " + resultSet.getString("patients" +
                        ".LastName"));
                item.put(DOCTOR, resultSet.getString("users.LastName"));
                LocalDateTime dateTime = LocalDateTime.of(resultSet.getObject("date", LocalDate.class), resultSet
                        .getObject("time", LocalTime.class));
                item.put(DATE, DateUtil.formatDateTime(dateTime));
                item.put(DESCRIPTION, resultSet.getString("description"));
                item.put(TEST_CATEGORY, resultSet.getString("category"));
                if (resultSet.getInt("admission_num") == -1) {
                    item.put(PATIENT_CATEGORY, PatientCategory.OUTPATIENT.toString());
                } else{
                    item.put(PATIENT_CATEGORY, PatientCategory.INPATIENT.toString());
                }
                data.add(item);
            }
        }
        return data;
    }
}

