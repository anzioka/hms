package main.java.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import main.java.dao.PatientDAO;
import main.java.model.LabRequest;
import main.java.model.MedicalRecord;
import main.java.model.Patient;
import main.java.model.Prescription;
import main.java.util.AgeUtil;
import main.java.util.DBUtil;
import main.java.util.DateUtil;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

/**
 * Created by alfonce on 18/07/2017.
 */
public class PatientMedicalRecordsController {

    @FXML
    private HBox pageNavigatorHolder;

    @FXML
    private Label currentPage;

    @FXML
    private Button numberOfPagesLabel, goNext, goPrevious;

    @FXML
    private VBox detailsHolder;

    @FXML
    private ListView<String> procedures;

    @FXML
    private TableView<LabRequest> labRequests;

    @FXML
    private TableColumn<LabRequest, String> testName, testResult;

    @FXML
    private TableView<Prescription> medication;

    @FXML
    private TableColumn<Prescription, String> medicineName, duration, dosage;

    //notes
    @FXML
    private Label chiefComplaints, medicalHistory, physicalExamination, investigation, diagnosis, treatment;

    //vitals
    @FXML
    private Label height, weight, bmi, systolicBp, diastolicBp, pulseRate, bodyTemp, respRate, spO2;

    //visit details
    @FXML
    private Label doctor, visitDate;

    @FXML
    private Label patientNo, firstName, lastName, sex, dateOfBirth, age;

    private ObservableList<MedicalRecord> records = FXCollections.observableArrayList();

    private String patientId;

    private int currentPageIndex = 0;

    @FXML
    public void initialize() {
        setUpLabTestsTable();
        setUpPrescriptionTable();
        configureProceduresListView();
    }

    @FXML
    private void goToNextPage() {

        if (currentPageIndex + 1 < records.size()) {
            currentPageIndex++;
            getCurrentRecord(currentPageIndex);
            if (currentPageIndex == records.size() - 1) { //last record
                goNext.setDisable(true);
                goPrevious.setDisable(false);
            }
        }
    }

    @FXML
    private void goToPrevious() {
        if (currentPageIndex - 1 >= 0) {
            currentPageIndex--;
            getCurrentRecord(currentPageIndex);
            if (currentPageIndex == 0) {
                goPrevious.setDisable(true);
                goNext.setDisable(false);
            }
        }
    }

    @FXML
    private void updateCurrentPageLabel() {
        currentPage.setText((currentPageIndex + 1) + "/" + records.size());
    }

    private void configureProceduresListView() {
        Label label = new Label("No procedures found!");
        label.getStyleClass().add("text-danger");
        procedures.setPlaceholder(label);
    }

    private void setUpPrescriptionTable() {
        Label label = new Label("No Prescription!");
        label.getStyleClass().add("text-danger");
        medication.setPlaceholder(label);

        //medicineName.setCellValueFactory(param -> param.getValue().medicineNameProperty());
//        duration.setCellValueFactory(param -> param.getValue().durationProperty());
//        dosage.setCellValueFactory(param -> param.getValue().dosageProperty());
    }

    private void getCurrentRecord(int index) {

        visitDate.setText(DateUtil.formatDate(records.get(index).getDate()));
        doctor.setText(records.get(index).getDoctorName());
        updateCurrentPageLabel();

        getDoctorNotes(index);
        getVitals(index);
        getPrescription(index);
        getProcedures(index);
        getLabRequests(index);

    }

    private void getLabRequests(int index) {
        String sql = "SELECT TestName, Result from LabRequests " +
                "INNER JOIN Queues on Queues.VisitId = LabRequests.VisitId " +
                "WHERE Queues.PatientId = '" + patientId + "' " +
                "AND LabRequests.VisitId = " + records.get(index).getVisitId();
        ObservableList<LabRequest> list = FXCollections.observableArrayList();
        try {
            ResultSet resultSet = DBUtil.executeQuery(sql);
            if (resultSet != null) {
                while (resultSet.next()) {
                    LabRequest request = new LabRequest();
                    request.setName(resultSet.getString("Testname"));
                    request.setResult(resultSet.getString("Result"));
                    list.add(request);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void getProcedures(int index) {
        String sql = "SELECT Name from VisitProcedures " +
                "INNER JOIN Queues on Queues.VisitId = VisitProcedures.VisitId " +
                "WHERE Queues.PatientId ='" + patientId + "' " +
                "AND VisitProcedures.VisitID = " + records.get(index).getVisitId();
        ObservableList<String> list = FXCollections.observableArrayList();
        try {
            ResultSet resultSet = DBUtil.executeQuery(sql);
            if (resultSet != null) {
                while (resultSet.next()) {
                    list.add(resultSet.getString("Name"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        procedures.setItems(list);

    }

    private void getPrescription(int index) {
        String sql = "SELECT MedicineName, Duration, Dosage " +
                "FROM Prescriptions " +
                "INNER JOIN Queues on Queues.VisitId = Prescriptions.VisitId " +
                "WHERE Queues.PatientID = '" + patientId + "' " +
                "AND Prescriptions.VisitID = " + records.get(index).getVisitId();
        ObservableList<Prescription> prescriptions = FXCollections.observableArrayList();
        try {
            ResultSet resultSet = DBUtil.executeQuery(sql);
            if (resultSet != null) {
                while (resultSet.next()) {
                    Prescription prescription = new Prescription();
                    //prescription.setMedicineName(resultSet.getString("MedicineName"));
//                    prescription.setDuration(resultSet.getString("Duration"));
//                    prescription.setDosage(resultSet.getString("Dosage"));
                    prescriptions.add(prescription);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        medication.setItems(prescriptions);
    }

    private void getVitals(int index) {
        String sql = "SELECT * FROM Vitals " +
                "INNER JOIN Queues on Queues.VisitId = Vitals.VisitId " +
                "WHERE Queues.PatientId = '" + patientId + "' " +
                "AND Vitals.VisitId = " + records.get(index).getVisitId();
        try {
            ResultSet resultSet = DBUtil.executeQuery(sql);
            if (resultSet != null) {
                if (resultSet.next()) {
                    height.setText(resultSet.getString("Height"));
                    weight.setText(resultSet.getString("Weight"));
                    bmi.setText(resultSet.getString("BMI"));
                    diastolicBp.setText(resultSet.getString("DiastolicBp"));
                    systolicBp.setText(resultSet.getString("SystolicBp"));
                    bodyTemp.setText(resultSet.getString("BodyTemperature"));
                    respRate.setText(resultSet.getString("RespiratoryRate"));
                    pulseRate.setText(resultSet.getString("PulseRate"));
                    spO2.setText(resultSet.getString("SPO2"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void getDoctorNotes(int index) {
        String sql = "SELECT * From VisitNotes " +
                "INNER JOIN Queues on Queues.VisitId = VisitNotes.VisitId " +
                "WHERE Queues.PatientId = '" + patientId + "' " +
                "AND Queues.VisitId = " + records.get(index).getVisitId();
        try {
            ResultSet resultSet = DBUtil.executeQuery(sql);
            if (resultSet != null) {
                if (resultSet.next()) {
                    medicalHistory.setText(resultSet.getString("History"));
                    chiefComplaints.setText(resultSet.getString("Complains"));
                    physicalExamination.setText(resultSet.getString("PhysicalExam"));
                    investigation.setText(resultSet.getString("Investigation"));
                    treatment.setText(resultSet.getString("Treatment"));
                    diagnosis.setText(resultSet.getString("Diagnosis"));

                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void setUpLabTestsTable() {
        Label label = new Label("No Lab Requests found!");
        label.getStyleClass().add("text-danger");
        labRequests.setPlaceholder(label);

        testName.setCellValueFactory(param -> param.getValue().nameProperty());
        testResult.setCellValueFactory(param -> param.getValue().resultProperty());

    }

    private void displayNoRecordsStatus() {
        detailsHolder.getChildren().clear();
        pageNavigatorHolder.setVisible(false);
        currentPage.setVisible(false);

        Label label = new Label("No medical records found!");
        label.getStyleClass().add("text-danger");
        detailsHolder.getChildren().add(label);

    }

    void setPatientId(String patientId) {
        this.patientId = patientId;
        setPatientInfo();
        getAllRecords();

        if (records.isEmpty()) {
            displayNoRecordsStatus();
        } else {
            numberOfPagesLabel.setText(records.size() + " records");
            getCurrentRecord(currentPageIndex);
        }
    }

    private void getAllRecords() {
        String sql = "SELECT Queues.DateCreated, VisitID, LastName " +
                "FROM Queues " +
                "INNER JOIN Users on Users.Id = Queues.DoctorId " +
                "WHERE Queues.Status = 'Discharged' " +
                "AND Queues.PatientId = '" + patientId + "' " +
                "ORDER By DateCreated";
        try {
            ResultSet resultSet = DBUtil.executeQuery(sql);
            if (resultSet != null) {
                while (resultSet.next()) {
                    MedicalRecord record = new MedicalRecord();
                    record.setDate(resultSet.getObject("DateCreated", LocalDate.class));
                    record.setVisitId(resultSet.getInt("VisitID"));
                    record.setDoctorName("Dr. " + resultSet.getString("LastName"));
                    records.add(record);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    private void setPatientInfo() {
        String sql = "SELECT * " +
                "FROM Patients " +
                "WHERE PatientId = '" + patientId + "'";
        Patient patient = PatientDAO.getPatient(sql);
        if (patient != null) {
            age.setText(AgeUtil.getAge(patient.getDateOfBirth()));
            dateOfBirth.setText(DateUtil.formatDate(patient.getDateOfBirth()));
            lastName.setText(patient.getLastName());
            firstName.setText(patient.getFirstName());
            sex.setText(patient.getSexuality());
            patientNo.setText(patient.getPatientId());
        }
    }

}
