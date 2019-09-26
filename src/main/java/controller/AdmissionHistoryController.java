package main.java.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import main.java.model.Inpatient;
import main.java.util.DBUtil;
import main.java.util.DateUtil;
import main.java.util.StringUtil;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class AdmissionHistoryController {
    private static final String PATIENT_NO = "patient_no";
    private static final String PATIENT_NAME = "patient_name";
    private static final String DATE_ADMITTED = "date_admitted";
    private static final String DATE_DISCHARGED = "date_discharged";
    private static final String DOCTOR = "doctor";
    private static final String ADMISSION_NUM = "admission_num";
    @FXML
    private AnchorPane container;
    @FXML
    private TextField searchField;
    @FXML
    private DatePicker startDate, endDate;
    @FXML
    private TableView<Map<String, String>> tableView;
    @FXML
    private TableColumn<Map<String, String>, String> patientNo, patientName, dateAdmitted, dateDischarged, doctor, options;
    private ObservableList<Map<String, String>> admissionHistory = FXCollections.observableArrayList();

    @FXML
    private void initialize() {

        startDate.setConverter(DateUtil.getDatePickerConverter());
        endDate.setConverter(DateUtil.getDatePickerConverter());

        endDate.setValue(LocalDate.now());
        startDate.setValue(LocalDate.now().withDayOfMonth(1));

        setUpTable();
        onSearch();
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterResults(newValue);
        });
    }

    private void filterResults(String newValue) {
        if (newValue == null || newValue.isEmpty()) {
            tableView.setItems(admissionHistory);
        } else {
            ObservableList<Map<String, String>> filtered = FXCollections.observableArrayList();
            for (Map<String, String> entry : filtered) {
                if (entry.get(PATIENT_NAME).toLowerCase().contains(newValue.toLowerCase())) {
                    filtered.add(entry);
                }
            }
            tableView.setItems(filtered);
        }
    }

    private void setUpTable() {

        for (TableColumn column : tableView.getColumns()) {
            if (column == options) {
                column.prefWidthProperty().bind(tableView.widthProperty().divide(3.5));
            } else {
                column.prefWidthProperty().bind(tableView.widthProperty().divide(7));
            }
        }

        //placeholder
        Label label = new Label("No admission history found");
        label.getStyleClass().add("text-danger");
        tableView.setPlaceholder(label);

        patientNo.setCellValueFactory(param -> StringUtil.getStringProperty(param.getValue().get(PATIENT_NO)));
        patientName.setCellValueFactory(param -> StringUtil.getStringProperty(param.getValue().get(PATIENT_NAME)));
        dateAdmitted.setCellValueFactory(param -> StringUtil.getStringProperty(param.getValue().get(DATE_ADMITTED)));
        dateDischarged.setCellValueFactory(param -> StringUtil.getStringProperty(param.getValue().get(DATE_DISCHARGED)));
        doctor.setCellValueFactory(param -> StringUtil.getStringProperty(param.getValue().get(DOCTOR)));
        options.setCellFactory(param -> new TableCell<Map<String, String>, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                int index = getIndex();
                if (index >= 0 && index < tableView.getItems().size()) {
                    Map<String, String> data = tableView.getItems().get(index);
                    Button summary = new Button("Discharge Summary");
                    summary.getStyleClass().add("btn-info-outline");
                    summary.setPrefWidth(150);
                    summary.setOnAction(event -> {
                        new PrintDischargeSummary().showSummary(Integer.parseInt(data.get(ADMISSION_NUM)), data.get
                                (PATIENT_NO));
                    });

                    Button bill = new Button("Bill");
                    bill.getStyleClass().add("btn-info-outline");
                    bill.setPrefWidth(150);
                    bill.setOnAction(event -> {
                        viewPatientBill(tableView.getItems().get(index).get(ADMISSION_NUM));
                    });
                    VBox vBox = new VBox(5.0, summary, bill);
                    vBox.setAlignment(Pos.CENTER);
                    setGraphic(vBox);
                } else {
                    setGraphic(null);
                }
            }
        });
    }

    private void viewPatientBill(String admissionNum) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("main/resources/view/inpatient_bill.fxml"));
            Stage stage = new Stage();
            stage.setScene(new Scene(loader.load()));
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(container.getScene().getWindow());
            InpatientBillController controller = loader.getController();
            controller.setAdmissionNumber(Integer.parseInt(admissionNum));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @FXML
    private void onSearch() {
        LocalDate start = startDate.getValue() != null ? startDate.getValue() : LocalDate.now().withDayOfMonth(1);
        startDate.setValue(start);
        LocalDate end = endDate.getValue() != null ? endDate.getValue() : LocalDate.now();
        endDate.setValue(end);

        Task<ObservableList<Map<String, String>>> task = new Task<ObservableList<Map<String, String>>>() {
            @Override
            protected ObservableList<Map<String, String>> call() {
                ObservableList<Map<String, String>> list = FXCollections.observableArrayList();
                String sql = "select admission_num, patients.FirstName, patients.LastName, inpatient_num, date_admitted, date_discharged, Users.LastName " +
                        "from inpatients " +
                        "inner join Users on users.Id = inpatients.doctor_id " +
                        "inner join patients on patients.patientId = inpatients.patient_id " +
                        "where status = '" + Inpatient.Status.DISCHARGED + "' " +
                        "and date_admitted between '" + start + "' and '" + end + "'";
                ResultSet resultSet = DBUtil.executeQuery(sql);
                if (resultSet != null) {
                    try {
                        while (resultSet.next()) {
                            Map<String, String> map = new HashMap<>();
                            map.put(PATIENT_NO, resultSet.getString("inpatient_num"));
                            map.put(ADMISSION_NUM, resultSet.getString("admission_num"));
                            map.put(PATIENT_NAME, resultSet.getString("patients.FirstName") + " " + resultSet.getString("patients.LastName"));
                            map.put(DATE_ADMITTED, DateUtil.formatDateLong(resultSet.getObject("date_admitted", LocalDate.class)));
                            map.put(DATE_DISCHARGED, DateUtil.formatDateLong(resultSet.getObject("date_discharged", LocalDate.class)));
                            map.put(DOCTOR, "Dr. " + resultSet.getString("Users.LastName"));
                            list.add(map);
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
                return list;
            }
        };
        task.setOnSucceeded(event -> {
            admissionHistory = task.getValue();
            tableView.setItems(admissionHistory);
        });
        new Thread(task).start();
    }
}
