package main.java.controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import main.java.dao.InpatientDao;
import main.java.model.*;
import main.java.util.DBUtil;
import main.java.util.DateUtil;
import main.java.util.StringUtil;

import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InpatientRecordsController {
    @FXML
    private Label dateAdmitted, dateDischarged, doctor, clinicalSummary, diagnosis, procedures, medication;
    @FXML
    private ScrollPane scrollPane;
    @FXML
    private VBox container, doctorNotes;
    @FXML
    private TableView<LabRequest> labTestsTableView;
    @FXML
    private TableView<RadiologyRequest> radiologyTableView;
    @FXML
    private TableColumn<LabRequest, String> labTest, labResult;
    @FXML
    private TableColumn<RadiologyRequest, String> radiologyTest, radiologyResult;
    @FXML
    private VBox pagination;
    private String patientId;
    private int currentIndex = 0, numRecords = 0;
    private Map<Integer, Integer> pageNumberAdmissionNumMap = new HashMap<>();
    private Map<Integer, List<String>> pageNumberAdmissionDetailsMap = new HashMap<>(); //0 -> date_admitted, 1-> date_discharged, 2-> doctor
    private MedicalRecordsController context;

    @FXML
    private void initialize() {
        togglePaginationVisibility(false);
        for (Node node : ((HBox) pagination.getChildren().get(0)).getChildren()) {
            node.setOnMouseClicked(event -> {
                handleClicked(node.getId());
            });
        }
        Platform.runLater(() -> {
            scrollPane.setVvalue(0);
        });
    }


    private void togglePaginationVisibility(boolean visible) {
        pagination.setVisible(visible);
        pagination.setManaged(visible);
    }

    private void handleClicked(String id) {
        currentIndex = context.getCurrentIndex(id, currentIndex, numRecords);

        getRecords(pageNumberAdmissionNumMap.get(currentIndex));
        setAdmissionDetails(pageNumberAdmissionDetailsMap.get(currentIndex));
        ((Label) pagination.getChildren().get(1)).setText(Integer.toString(currentIndex + 1) + "/" + numRecords);
    }

    private void setAdmissionDetails(List<String> list) {
        dateAdmitted.setText("Date Admitted: " + list.get(0));
        dateDischarged.setText("Date Discharged : " + list.get(1));
        doctor.setText(list.get(2));
    }

    private void getRecords(Integer admissionNum) {
        getClinicalSummary(admissionNum);
        getDiagnosis(admissionNum);
        getProcedures(admissionNum);
        getMedication(admissionNum);
        getLabTests(admissionNum);
        getRadiologyTests(admissionNum);
        getDoctorNotes(admissionNum);
        scrollPane.setVvalue(0);
    }

    private void getDoctorNotes(Integer admissionNum) {
        Task<ObservableList<InpatientVisit>> task = new Task<ObservableList<InpatientVisit>>() {
            @Override
            protected ObservableList<InpatientVisit> call() {
                return InpatientDao.getVisitNotes(InpatientVisit.Category.DOCTOR, admissionNum);
            }
        };
        task.setOnSucceeded(event -> {
            for (int i = 1 ; i < doctorNotes.getChildren().size(); i++) {
                doctorNotes.getChildren().remove(i);
            }
            if (!task.getValue().isEmpty()) {
                setDoctorNotes(task.getValue());
            }
        });
        new Thread(task).start();
    }

    private void setDoctorNotes(ObservableList<InpatientVisit> inpatientVisits) {

        Accordion accordion = new Accordion();
        for (InpatientVisit inpatientVisit : inpatientVisits) {
            TitledPane titledPane = new TitledPane();
            titledPane.setText(inpatientVisit.getUserName() + " : " + DateUtil.formatDateLong(inpatientVisit.getDateCreated()));
            Label label = new Label(inpatientVisit.getNotes());
            label.setWrapText(true);
            label.setLineSpacing(2);
            VBox vBox = new VBox(label);
            titledPane.setContent(vBox);
            accordion.getPanes().add(titledPane);
        }
        VBox.setVgrow(accordion, Priority.ALWAYS);
        doctorNotes.getChildren().add(accordion);
    }

    private void getRadiologyTests(Integer admissionNum) {

    }

    private void getLabTests(Integer admissionNum) {
        Task<ObservableList<LabRequest>> task = new Task<ObservableList<LabRequest>>() {
            @Override
            protected ObservableList<LabRequest> call() throws Exception {
                ObservableList<LabRequest> labRequests = FXCollections.observableArrayList();
                ResultSet resultSet = DBUtil.executeQuery("select Id, lab_requests.TestId, TestName from lab_requests " +
                        "inner join labtests on labTests.TestId = lab_requests.TestId " +
                        "where Status = '" + LabRequest.Status.COMPLETED + "' and " +
                        "AdmissionNum = " + admissionNum);
                if (resultSet != null) {
                    while (resultSet.next()) {
                        LabRequest request = new LabRequest();
                        request.setId(resultSet.getInt("id"));
                        request.setName(resultSet.getString("TestName"));
                        request.setTestId(resultSet.getInt("TestId"));
                        labRequests.add(request);
                    }
                }
                return labRequests;
            }
        };
        task.setOnSucceeded(event -> {
            labTestsTableView.setItems(task.getValue());
        });
        new Thread(task).start();
    }

    private void getMedication(Integer admissionNum) {
        Task<List<String>> task = new Task<List<String>>() {
            @Override
            protected List<String> call() throws Exception {
                List<String> list = new ArrayList<>();
                ResultSet resultSet = DBUtil.executeQuery("select distinct drugs.name from prescriptions " +
                        "inner join drugs on drugs.DrugCode = prescriptions.drug_code " +
                        "where admission_num = " + admissionNum);
                if (resultSet != null) {
                    while (resultSet.next()) {
                        list.add(resultSet.getString("name"));
                    }
                }
                return list;
            }
        };
        task.setOnSucceeded(event -> {
            medication.setText(StringUtil.getUnnumberedList(task.getValue()));
        });
        new Thread(task).start();

    }

    private void getProcedures(Integer admissionNum) {
        Task<List<String>> task = new Task<List<String>>() {
            @Override
            protected List<String> call() throws Exception {
                List<String> list = new ArrayList<>();
                ResultSet resultSet = DBUtil.executeQuery("select distinct procedure_name from patient_procedures where admission_num = " + admissionNum);
                if (resultSet != null) {
                    while (resultSet.next()) {
                        list.add(resultSet.getString("procedure_name"));
                    }
                }
                return list;
            }
        };
        task.setOnSucceeded(event -> {
            procedures.setText(StringUtil.getUnnumberedList(task.getValue()));
        });
        new Thread(task).start();
    }

    private void getDiagnosis(Integer admissionNum) {
        Task<List<String>> task = new Task<List<String>>() {
            @Override
            protected List<String> call() throws Exception {
                List<String> list = new ArrayList<>();
                ResultSet resultSet = DBUtil.executeQuery("select distinct icd10_diagnoses.name from diagnosis " +
                        "inner join icd10_diagnoses on icd10_diagnoses.code = diagnosis.code " +
                        "where admission_num = " + admissionNum);
                if (resultSet != null) {
                    while (resultSet.next()) {
                        list.add(resultSet.getString("name"));
                    }
                }
                return list;
            }
        };
        task.setOnSucceeded(event -> {
            diagnosis.setText(StringUtil.getUnnumberedList(task.getValue()));
        });
        new Thread(task).start();
    }

    private void getClinicalSummary(Integer admissionNum) {
        Task<ClinicalSummary> task = new Task<ClinicalSummary>() {
            @Override
            protected ClinicalSummary call() throws Exception {
                return InpatientDao.getClinicalSummary(admissionNum);
            }
        };
        task.setOnSucceeded(event -> {
            if (task.getValue() != null) {
                clinicalSummary.setText(task.getValue().getSummary());
            }
        });
        new Thread(task).start();
    }

    private void configurePagination(List<List<String>> admissions) {
        numRecords = admissions.size();
        pageNumberAdmissionNumMap = new HashMap<>();

        for (int i = 0; i < numRecords; i++) {
            pageNumberAdmissionNumMap.put(i, Integer.parseInt(admissions.get(i).get(0)));
            pageNumberAdmissionDetailsMap.put(i, admissions.get(i).subList(1, admissions.get(i).size()));
        }
        handleClicked("prev");
        togglePaginationVisibility(true);
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
        getPrevAdmissions();
    }

    private void getPrevAdmissions() {
        Task<List<List<String>>> task = new Task<List<List<String>>>() {
            @Override
            protected List<List<String>> call() throws Exception {
                List<List<String>> list=  new ArrayList<>();
                ResultSet resultSet = DBUtil.executeQuery("select admission_num, date_admitted, date_discharged, users.LastName from inpatients " +
                        "inner join users on users.Id = inpatients.doctor_id " +
                        "where patient_id = '" + patientId + "' " +
                        "and status = '" + Inpatient.Status.DISCHARGED + "'");
                if (resultSet != null) {
                    while (resultSet.next()) {
                        List<String> items = new ArrayList<>();
                        items.add(resultSet.getString("admission_num"));
                        items.add(DateUtil.formatDateLong(resultSet.getObject("date_admitted", LocalDate.class)));
                        items.add(DateUtil.formatDateLong(resultSet.getObject("date_discharged", LocalDate.class)));
                        items.add("Dr. "+ resultSet.getString("lastName"));
                        list.add(items);
                    }
                }
                return list;
            }
        };
        task.setOnSucceeded(event -> {
            configurePagination(task.getValue());
        });
        new Thread(task).start();

    }

    public void setContext(MedicalRecordsController context) {
        this.context = context;
        context.setUpTables(labTestsTableView, labTest, labResult, radiologyTableView, radiologyTest,radiologyResult, container);
    }
}
