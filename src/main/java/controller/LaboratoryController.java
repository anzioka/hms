package main.java.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import main.java.dao.SettingsDao;
import main.java.model.*;
import main.java.util.DBUtil;
import main.java.util.DateUtil;
import main.java.util.StringUtil;

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

/**
 * Created by alfonce on 23/04/2017.
 */
public class LaboratoryController {
    static final String REQUEST_ID = "request_id";
    static final String TEST_ID = "test_id";
    static final String CATEGORY = "category";
    static final String SAMPLE = "sample";
    static final String TEST_NAME = "test";
    static final String PATIENT_ID = "patient_id";
    static final String TIME = "time";
    private final String PATIENT_NAME = "name";
    private final String PAYMENT_MODE = "payment_mode";
    private final String PAYMENT_STATUS = "payment_status";
    //scheduler service
    private final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(2);
    @FXML
    private VBox container;
    @FXML
    private TableView<Map<String, String>> labRequestTableView;
    @FXML
    private TabPane tabPane;
    @FXML
    private TableColumn<Map<String, String>, String> patientId, patientName, timeRequested, paymentMode, paymentStatus,
            options, test, category;
    private Setting setting;

    @FXML
    public void initialize() {
        getData();

        configureLabRequestsTable();
        tabPane.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> {
            loadTab(newValue.intValue());
        });

    }

    private void getData() {
        Task<Setting> settingsTask = new Task<Setting>() {
            @Override
            protected Setting call() {
                return SettingsDao.getSettings();
            }
        };
        settingsTask.setOnSucceeded(event -> {
            setting = settingsTask.getValue();
            //scheduler to read data
            executorService.scheduleAtFixedRate(() -> {
                Task<ObservableList<Map<String, String>>> task = new Task<ObservableList<Map<String, String>>>() {
                    @Override
                    protected ObservableList<Map<String, String>> call() {
                        return getLabRequests();
                    }
                };
                task.setOnSucceeded(e -> {
                    labRequestTableView.setItems(task.getValue());
                });
                new Thread(task).start();
            }, 0, 1, TimeUnit.SECONDS);

        });
        new Thread(settingsTask).start();
    }

    private void loadTab(int tabIndex) {
        if (tabIndex != 0) {
            String[] resourceFiles = new String[]{"lab_tests", "all_lab_results"};
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource
                        ("main/resources/view/" + resourceFiles[tabIndex - 1] + ".fxml"));
                Node node = loader.load();

                Tab tab = tabPane.getTabs().get(tabIndex);
                tab.setContent(node);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void configureLabRequestsTable() {
        //no lab requests
        Label label = new Label("No lab requests have been posted");
        label.getStyleClass().add("text-danger");
        labRequestTableView.setPlaceholder(label);

        //table columns
        category.setCellValueFactory(param -> StringUtil.getStringProperty(param.getValue().get(CATEGORY)));
        patientId.setCellValueFactory(param -> StringUtil.getStringProperty(param.getValue().get(PATIENT_ID)));
        patientName.setCellValueFactory(param -> StringUtil.getStringProperty(param.getValue().get(PATIENT_NAME)));
        timeRequested.setCellValueFactory(param -> StringUtil.getStringProperty(param.getValue().get(TIME)));
        paymentMode.setCellValueFactory(param -> StringUtil.getStringProperty(param.getValue().get(PAYMENT_MODE)));
        test.setCellValueFactory(param -> StringUtil.getStringProperty(param.getValue().get(TEST_NAME)));
        paymentStatus.setCellValueFactory(param -> StringUtil.getStringProperty(param.getValue().get(PAYMENT_STATUS)));
        options.setCellFactory(param -> new TableCell<Map<String, String>, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                int index = getIndex();
                if (index >= 0 && index < labRequestTableView.getItems().size()) {
                    Map<String, String> entry = labRequestTableView.getItems().get(index);
                    Button button = new Button("View");
                    button.getStyleClass().add("btn-info-outline");

                    if (entry.get(PAYMENT_MODE).equals(PaymentMode.CASH.name()) && entry.get(PAYMENT_STATUS).equals(Bill.Status.PENDING.name()) &&
                            !entry.get(CATEGORY).equals(PatientCategory.INPATIENT.toString())) {
                        if (setting.isLabPrepay()) {
                            button.setDisable(true);
                        }
                    }
                    button.setOnAction(event -> viewCurrentRequest(entry));
                    setGraphic(button);
                } else {
                    setGraphic(null);
                }
            }
        });
    }

    private void viewCurrentRequest(Map<String, String> entry) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource
                    ("main/resources/view/edit-test-result.fxml"));
            Parent node = loader.load();

            //stage
            Stage stage = new Stage();
            stage.setScene(new Scene(node));
            stage.setResizable(false);
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(container.getScene().getWindow());

            //controller
            EditTestResultController controller = loader.getController();
            controller.setStage(stage);
            controller.setLabRequestEntry(entry);

            //show
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private ObservableList<Map<String, String>> getLabRequests() {
        ObservableList<Map<String, String>> list = FXCollections.observableArrayList();
        String sql = "select distinct lab_requests.*, patients.PatientID, labtests.testName, billing.status as PaymentStatus," +
                "\n" +
                "  patients.FirstName, patients.LastName, queues.payment_mode, queues.DoctorId\n" +
                "from lab_requests\n" +
                "inner join queues on lab_requests.QueueId = queues.VisitId\n" +
                "inner join labtests on labtests.TestId = lab_requests.TestId\n" +
                "inner join patients on queues.PatientID = patients.PatientID\n" +
                "inner join billing on billing.Id = lab_requests.Id\n" +
                "where lab_requests.Status = '" + LabRequest.Status.PENDING + "' " +
                "and billing.category = '" + Bill.Category.LAB.name() + "' " +
                "union\n" +
                "select distinct lab_requests.*, inpatient_num, labtests.testName, billing.status, patients.FirstName, " +
                "patients\n" +
                ".LastName, inpatients.payment_mode, inpatients.doctor_id\n" +
                "from lab_requests\n" +
                "  inner join inpatients on lab_requests.AdmissionNum = inpatients.admission_num\n" +
                "  inner join labtests on labtests.TestId = lab_requests.TestId\n" +
                "  inner join patients on inpatients.patient_id = patients.PatientID\n" +
                "  inner join billing on billing.Id = lab_requests.Id\n" +
                "where lab_requests.Status = '" + LabRequest.Status.PENDING + "' " +
                "and billing.category = '" + Bill.Category.LAB.name() + "' " +
                "order by TimeCreated";
        try {
            ResultSet resultSet = DBUtil.executeQuery(sql);
            if (resultSet != null) {
                while (resultSet.next()) {
                    Map<String, String> map = new HashMap<>();
                    map.put(REQUEST_ID, resultSet.getString("Id")); //0
                    map.put(PATIENT_NAME, resultSet.getString("FirstName") + " " + resultSet.getString("LastName"))
                    ; //2
                    LocalDate date = resultSet.getObject("DateCreated", LocalDate.class);
                    LocalTime time = resultSet.getObject("TimeCreated", LocalTime.class);
                    map.put(TIME, DateUtil.formatDateTime(LocalDateTime.of(date, time))); //3
                    map.put(PAYMENT_MODE, resultSet.getString("payment_mode")); //4
                    map.put(TEST_NAME, resultSet.getString("TestName")); //8
                    map.put(TEST_ID, resultSet.getString("TestId"));
                    if (Integer.parseInt(resultSet.getString("AdmissionNum")) != -1) {
                        map.put(CATEGORY, PatientCategory.INPATIENT.toString());
                    } else if (Integer.parseInt(resultSet.getString("DoctorId")) == 0) {
                        map.put(CATEGORY, PatientCategory.WALK_IN.toString());
                    } else {
                        map.put(CATEGORY, PatientCategory.OUTPATIENT.toString());
                    }
                    map.put(SAMPLE, resultSet.getString("Specimen"));
                    map.put(PATIENT_ID, resultSet.getString("PatientId"));
                    map.put(PAYMENT_STATUS, resultSet.getString("PaymentStatus"));

                    list.add(map);
                }

            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    @FXML
    private void onNewRequest() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("main/resources/view/list-walk-in-patients.fxml"));
            Stage stage = new Stage();
            stage.setScene(new Scene(loader.load()));
            stage.setResizable(false);
            stage.initOwner(container.getScene().getWindow());
            stage.initModality(Modality.WINDOW_MODAL);
            stage.setTitle("Walk-in Patients");

            ListWalkInPatientsController controller = loader.getController();
            controller.setStage(stage);
            controller.setRequestType(ServiceType.LAB_TEST);
            stage.showAndWait();

            if (controller.isPatientSelected()) {
                createRequest(controller.getParameters());
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createRequest(String[] parameters) {
        //1 : visit id
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource
                    ("main/resources/view/lab_requests.fxml"));
            Parent node = loader.load();

            //set stage
            Stage labRequestStage = new Stage();
            labRequestStage.initModality(labRequestStage.getModality());
            labRequestStage.setResizable(false);
            labRequestStage.setScene(new Scene(node));
            labRequestStage.initOwner(container.getScene().getWindow());

            LabRequestController controller = loader.getController();
            controller.setParameters(Integer.parseInt(parameters[1]), parameters[0], false);
            controller.setStage(labRequestStage);

            labRequestStage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
