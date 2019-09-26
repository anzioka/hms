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
public class PharmacyController {
    private static final String CATEGORY = "category";
    private static final String PATIENT_NUMBER = "patient_number";
    private static final String PATIENT_NAME = "patient_name";
    private static final String PAYMENT_MODE = "payment_mode";
    private static final String PAYMENT_STATUS = "payment_status";
    private static final String MEDICINE = "medicine";
    private static final String PRESCRIPTION_ID = "prescription_id";
    private static final String TIME = "time";
    private static final String DRUG_ID = "drug_id";
    private final ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(2);
    private Setting setting;
    @FXML
    private TableView<Map<String, String>> tableView;
    @FXML
    private TabPane tabPane;
    @FXML
    private VBox container;
    @FXML
    private TableColumn<Map<String, String>, String> patientNum, patientName, paymentMode,
            paymentStatus, options, time, medicine, category;

    @FXML
    public void initialize() {
        initializePrescriptionTable();
        tabPane.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.intValue() != 0) {
                loadTab(newValue.intValue());
            }
        });

        getData();
    }

    private void loadTab(int index) {
        String[] resourceFileNames = new String[]{"medicine-list", "view-dispensed-drugs"};
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("main/resources/view/" +
                    resourceFileNames[index - 1] + ".fxml"));
            Node node = loader.load();

            Tab tab = tabPane.getTabs().get(index);
            tab.setContent(node);
            tab.setClosable(false);

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void getData() {
        Task settingsTask = new Task() {
            @Override
            protected Object call() {
                setting = SettingsDao.getSettings();
                return null;
            }
        };
        settingsTask.setOnSucceeded(event -> {
            scheduledExecutorService.scheduleAtFixedRate(() -> {
                Task<ObservableList<Map<String, String>>> task = new Task<ObservableList<Map<String, String>>>() {
                    @Override
                    protected ObservableList<Map<String, String>> call() throws Exception {
                        return getPrescriptions();
                    }
                };
                task.setOnSucceeded(e -> {
                    tableView.setItems(task.getValue());

                });

                new Thread(task).start();

            }, 0, 2, TimeUnit.SECONDS);
        });

        new Thread(settingsTask).start();
    }

    private ObservableList<Map<String, String>> getPrescriptions() throws SQLException {
        ObservableList<Map<String, String>> list = FXCollections.observableArrayList();
        String sql = "select prescriptions.id, DrugCode, drugs.name, prescriptions.admission_num, " +
                "prescriptions.time_created, prescriptions.date_created, inpatients.inpatient_num, " +
                "billing.status, inpatients.payment_mode, " +
                "Patients.FirstName, Patients.LastName, inpatients.doctor_id " +
                "from prescriptions " +
                "inner join drugs on drugs.drugCode = prescriptions.drug_code " +
                "inner join inpatients on inpatients.admission_num = prescriptions.admission_num " +
                "inner join patients on patients.PatientId = inpatients.patient_id " +
                "inner join billing on billing.id = prescriptions.id " +
                "where prescriptions.status =  '" + Prescription.Status.PENDING + "' " +
                "union " +
                "select prescriptions.id, DrugCode, drugs.name, prescriptions.admission_num, " +
                "prescriptions.time_created, prescriptions.date_created, patients.PatientId, " +
                "billing.status, Queues.payment_mode, " +
                "Patients.FirstName, Patients.LastName, queues.DoctorId " +
                "from prescriptions " +
                "inner join drugs on drugs.drugCode = prescriptions.drug_code " +
                "inner join queues on queues.VisitId = prescriptions.visit_id " +
                "inner join patients on patients.PatientId = queues.PatientId " +
                "left join billing on billing.id = prescriptions.id " +
                "where prescriptions.status =  '" + Prescription.Status.PENDING + "' " +
                "order by date_created, time_created";
        ResultSet resultSet = DBUtil.executeQuery(sql);
        if (resultSet != null) {
            while (resultSet.next()) {
                Map<String, String> map = new HashMap<>();
                map.put(DRUG_ID, resultSet.getString("DrugCode"));
                map.put(PRESCRIPTION_ID, resultSet.getString("ID"));
                map.put(MEDICINE, resultSet.getString("name"));
                if (Integer.parseInt(resultSet.getString("admission_num")) != -1) {
                    map.put(CATEGORY, PatientCategory.INPATIENT.toString());
                } else if (Integer.parseInt(resultSet.getString("doctor_id")) == 0) {
                    map.put(CATEGORY, PatientCategory.WALK_IN.toString());
                } else {
                    map.put(CATEGORY, PatientCategory.OUTPATIENT.toString());
                }
                map.put(PATIENT_NUMBER, resultSet.getString("inpatient_num"));
                map.put(PAYMENT_STATUS, resultSet.getString("status"));
                map.put(PAYMENT_MODE, PaymentMode.valueOf(resultSet.getString("payment_mode")).toString());
                map.put(PATIENT_NAME, resultSet.getString("FirstName") + " " + resultSet.getString("LastName"));
                LocalDate date = resultSet.getObject("date_created", LocalDate.class);
                LocalTime time = resultSet.getObject("time_created", LocalTime.class);
                map.put(TIME, DateUtil.formatDateTime(LocalDateTime.of(date, time)));
                list.add(map);

            }
        }
        return list;
    }

    private void initializePrescriptionTable() {
        //no prescription
        Label label = new Label("No prescription requests found!");
        label.getStyleClass().add("text-danger");
        tableView.setPlaceholder(label);
        //columns
        category.setCellValueFactory(param -> StringUtil.getStringProperty(param.getValue().get(CATEGORY)));
        patientNum.setCellValueFactory(param -> StringUtil.getStringProperty(param.getValue().get(PATIENT_NUMBER)));
        patientName.setCellValueFactory(param -> StringUtil.getStringProperty(param.getValue().get(PATIENT_NAME)));
        time.setCellValueFactory(param -> StringUtil.getStringProperty(param.getValue().get(TIME)));
        medicine.setCellValueFactory(param -> StringUtil.getStringProperty(param.getValue().get(MEDICINE)));
        paymentMode.setCellValueFactory(param -> StringUtil.getStringProperty(param.getValue().get(PAYMENT_MODE)));
        paymentStatus.setCellValueFactory(param -> StringUtil.getStringProperty(param.getValue().get(PAYMENT_STATUS)));
        options.setCellFactory(param -> new TableCell<Map<String, String>, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                int index = getIndex();
                if (index >= 0 && index < tableView.getItems().size()) {
                    Map<String, String> entry = tableView.getItems().get(index);
                    Button button = new Button("View");
                    button.getStyleClass().add("btn-info-outline");
                    button.setOnAction(event -> {
                        showPrescriptionDetails(entry);
                    });
                    if (entry.get(PAYMENT_MODE).equals(PaymentMode.CASH.toString())
                            && (entry.get(CATEGORY).equals(PatientCategory.OUTPATIENT.toString()) || entry.get(CATEGORY).equals(PatientCategory.WALK_IN.toString()))
                            && entry.get(PAYMENT_STATUS).equals(Bill.Status.PENDING.name())
                            && setting.isPharmacyPrepay()) {
                        button.setDisable(true);
                    }
                    setGraphic(button);
                } else {
                    setGraphic(null);
                }
            }
        });
    }

    @FXML
    private void onViewPointOfSale() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("main/resources/view/pos.fxml"));

            Stage stage = new Stage();
            stage.setScene(new Scene(loader.load()));
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(container.getScene().getWindow());
            stage.setResizable(false);

            PharmacyPosController controller = loader.getController();
            controller.setStage(stage);

            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onCreateNewRequest() {
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
            controller.setRequestType(ServiceType.PHARMACY);
            stage.showAndWait();

            if (controller.isPatientSelected()) {
                createPrescription(controller.getParameters());
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createPrescription(String[] parameters) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource
                    ("main/resources/view/prescription.fxml"));
            Parent node = loader.load();

            //stage
            Stage stage = new Stage();
            stage.setResizable(false);
            stage.setScene(new Scene(node));
            stage.initModality(Modality.WINDOW_MODAL);
            stage.setTitle("Prescription");
            stage.initOwner(container.getScene().getWindow());

            //controller
            PrescriptionController controller = loader.getController();
            controller.setOutpatientMode();
            controller.setPatientId(parameters[0]);
            controller.setVisitId(Integer.parseInt(parameters[1]));

            //show window
            stage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void showPrescriptionDetails(Map<String, String> entry) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource
                    ("main/resources/view/dispense_drugs.fxml"));
            Parent node = loader.load();

            //stage
            Stage stage = new Stage();
            stage.setScene(new Scene(node));
            stage.setTitle("Dispense Drugs");
            stage.setResizable(false);
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(container.getScene().getWindow());

            //controller
            DispenseDrugsController controller = loader.getController();
            controller.setStage(stage);
            controller.setParameters(entry.get(CATEGORY), entry.get(PATIENT_NUMBER), entry.get(TIME), entry.get(PRESCRIPTION_ID), entry.get(DRUG_ID));

            //show window
            stage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
