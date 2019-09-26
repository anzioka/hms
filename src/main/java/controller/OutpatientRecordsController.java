package main.java.controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import main.java.dao.ClinicVisitDAO;
import main.java.model.ClinicVisitNotes;
import main.java.model.LabRequest;
import main.java.model.PatientQueue;
import main.java.model.RadiologyRequest;
import main.java.util.DBUtil;
import main.java.util.DateUtil;
import main.java.util.StringUtil;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OutpatientRecordsController {
    @FXML
    private VBox pagination;
    @FXML
    private VBox container;
    @FXML
    private ScrollPane scrollPane;
    @FXML
    private Label date, doctor, chiefComplaints, history, physicalExamination, investigation, diagnosis, treatment, procedures, medication;

    @FXML
    private TableView<LabRequest> labTestsTableView;
    @FXML
    private TableView<RadiologyRequest> radiologyTableView;
    @FXML
    private TableColumn<LabRequest, String> labTest, labResult;
    @FXML
    private TableColumn<RadiologyRequest, String> radiologyTest, radiologyResult;
    private String patientId;
    private int currentIndex = 0;
    private int numRecords = 0;
    private Map<Integer, Integer> pageNumberVisitIdMap;
    private MedicalRecordsController context;

    @FXML
    private void initialize() {
        togglePaginationView(false);

        for (Node node : ((HBox) pagination.getChildren().get(0)).getChildren()) {
            node.setOnMouseClicked(event -> {
                handleClick(node.getId());
            });
        }
        Platform.runLater(() -> {
            scrollPane.setVvalue(0);
        });
    }

    private void handleClick(String id) {
        currentIndex = context.getCurrentIndex(id, currentIndex, numRecords);
        getRecords(pageNumberVisitIdMap.get(currentIndex));
        ((Label) pagination.getChildren().get(1)).setText(Integer.toString(currentIndex + 1) + "/" + numRecords);
    }

    private void togglePaginationView(boolean visible) {
        pagination.setVisible(visible);
        pagination.setManaged(visible);
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
        getVisitIds();
    }

    private void configurePagination(List<Integer> visitIds) {
        numRecords = visitIds.size();
        pageNumberVisitIdMap = new HashMap<>();

        for (int i = 0; i < numRecords; i++) {
            pageNumberVisitIdMap.put(i, visitIds.get(i));
        }
        handleClick("prev");
        togglePaginationView(true);

    }

    private void getRecords(int visitId) {
        //get date, and doctor, get notes, get procedures, get medication
        getVisitDetails(visitId);
        getNotes(visitId);
        getMedication(visitId);
        getProcedures(visitId);
        getDiagnosis(visitId);
        getLabTests(visitId);
        getRadiologyTests(visitId);
    }

    private void getDiagnosis(int visitId) {
       Task<List<String>>task = new Task<List<String>>() {
           @Override
           protected List<String> call() throws Exception {
               List<String> list = new ArrayList<>();
               ResultSet resultSet = DBUtil.executeQuery("select distinct icd10_diagnoses.name from diagnosis " +
                       "inner join icd10_diagnoses on icd10_diagnoses.code = diagnosis.code " +
                       "where visit_id = " + visitId);
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

    private void getRadiologyTests(int visitId) {

    }

    private void getLabTests(int visitId) {
        Task<ObservableList<LabRequest>> task = new Task<ObservableList<LabRequest>>() {
            @Override
            protected ObservableList<LabRequest> call() throws Exception {
                ObservableList<LabRequest> requests = FXCollections.observableArrayList();
                ResultSet resultSet = DBUtil.executeQuery("select Id, lab_requests.TestId, TestName " +
                        "from lab_requests inner join LabTests on LabTests.TestId = lab_requests.TestId " +
                        "where QueueId = " + visitId + " and status = '" + LabRequest.Status.COMPLETED + "'");
                if (resultSet != null) {
                    while (resultSet.next()) {
                        LabRequest request = new LabRequest();
                        request.setTestId(resultSet.getInt("TestId"));
                        request.setId(resultSet.getInt("id"));
                        request.setName(resultSet.getString("TestName"));
                        requests.add(request);
                    }

                }
                return requests;
            }
        };
        task.setOnSucceeded(event -> {
            labTestsTableView.setItems(task.getValue());
        });
        new Thread(task).start();
    }

    private void getVisitDetails(int visitId) {
        Task<List<String>> task = new Task<List<String>>() {
            @Override
            protected List<String> call() throws Exception {
                List<String> list = new ArrayList<>();
                String sql = "select queues.DateCreated, users.LastName from queues " +
                        "inner join users on users.Id = queues.doctorId " +
                        "where visitId = " + visitId;
                ResultSet resultSet = DBUtil.executeQuery(sql);
                if (resultSet != null && resultSet.next()) {
                    list.add(resultSet.getString("lastName"));
                    list.add(DateUtil.formatDateLong(resultSet.getObject("DateCreated", LocalDate.class)));
                }
                return list;
            }
        };
        task.setOnSucceeded(event -> {
            doctor.setText("Dr. " + task.getValue().get(0));
            date.setText(task.getValue().get(1));

        });
        new Thread(task).start();
    }

    private void getProcedures(int visitId) {
        Task<List<String>> task = new Task<List<String>>() {
            @Override
            protected List<String> call() throws Exception {
                List<String> list = new ArrayList<>();
                ResultSet resultSet = DBUtil.executeQuery("select distinct procedure_name from patient_procedures where visit_id = " + visitId);
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

    private void getMedication(int visitId) {
        Task<List<String>> task = new Task<List<String>>() {
            @Override
            protected List<String> call() throws Exception {
                List<String> list = new ArrayList<>();
                ResultSet resultSet = DBUtil.executeQuery("select distinct drugs.name from prescriptions " +
                        "inner join drugs on drugs.drugCode = prescriptions.drug_code " +
                        "where visit_id =  " + visitId);
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

    private void getNotes(int visitId) {
        Task<ClinicVisitNotes> task = new Task<ClinicVisitNotes>() {
            @Override
            protected ClinicVisitNotes call() throws Exception {
               return ClinicVisitDAO.getNotesByVisitId(visitId);
            }
        };
        task.setOnSucceeded(event -> {
            setNotes(task.getValue());
        });
        new Thread(task).start();
    }

    private void setNotes(ClinicVisitNotes visitNotes) {
        investigation.setText(visitNotes.getInvestigation());
        physicalExamination.setText(visitNotes.getPhysicalExam());
        chiefComplaints.setText(visitNotes.getPrimaryComplains());
        treatment.setText(visitNotes.getTreatment());
        history.setText(visitNotes.getMedicalHistory());
    }

    private void getVisitIds() {
        Task<List<Integer>> task = new Task<List<Integer>>() {
            @Override
            protected List<Integer> call() throws Exception {
                List<Integer> list = new ArrayList<>();
                String sql = "select visitId from queues where patientId = '" + patientId + "' and status = '" + PatientQueue.Status.DISCHARGED + "' and DoctorId != 0";

                ResultSet resultSet = DBUtil.executeQuery(sql);
                if (resultSet != null) {
                    try {
                        while (resultSet.next()) {
                            list.add(resultSet.getInt("visitId"));
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
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
        context.setUpTables(labTestsTableView, labTest, labResult, radiologyTableView, radiologyTest, radiologyResult, container);

    }
}
