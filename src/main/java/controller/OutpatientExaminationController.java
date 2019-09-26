package main.java.controller;

import com.sun.javafx.scene.control.behavior.TextAreaBehavior;
import com.sun.javafx.scene.control.skin.TextAreaSkin;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import main.Main;
import main.java.dao.ClinicVisitDAO;
import main.java.dao.PatientDAO;
import main.java.model.*;
import main.java.util.AlertUtil;
import main.java.util.DBUtil;
import main.java.util.DateUtil;
import main.java.util.NumberUtil;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by alfonce on 18/05/2017.
 */
public class OutpatientExaminationController {
    //patient details
    @FXML
    private Label patientId, patientGender, firstName, surname, dateOfBirth, age;
    //clinical notes
    @FXML
    private TextArea chiefComplains, medicalHistory, physicalExam, investigation, treatment;
    @FXML
    private VBox container;
    private int queueId;
    private String currentPatientId;
    private DoctorModuleController doctorModuleController;
    private Stage stage;

    @FXML
    private void initialize() {
        for (TextArea textArea : new TextArea[] {chiefComplains, medicalHistory, physicalExam, investigation, treatment}) {
            textArea.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
                if (event.getCode() == KeyCode.TAB) {
                    TextAreaSkin skin = (TextAreaSkin) textArea.getSkin();
                    TextAreaBehavior behavior = skin.getBehavior();
                    if (event.isControlDown()) {
                        behavior.callAction("InsertTab");
                    } else{
                        behavior.callAction("TraverseNext");
                    }
                    event.consume();
                }
            });
        }
    }


    private void setNotes() {
        ClinicVisitNotes notes = ClinicVisitDAO.getNotesByVisitId(queueId);
        chiefComplains.setText(notes.getPrimaryComplains());
        medicalHistory.setText(notes.getMedicalHistory());
        physicalExam.setText(notes.getPhysicalExam());
        investigation.setText(notes.getInvestigation());
        treatment.setText(notes.getTreatment());
    }

    private void setPatientDetails() {
        String sql = "select * from patients where patientId='" + currentPatientId + "'";
        Patient patient = PatientDAO.getPatient(sql);
        if (patient != null) {
            firstName.setText(patient.getFirstName());
            surname.setText(patient.getLastName());
            patientId.setText(patient.getPatientId());
            patientGender.setText(patient.getSexuality());
            dateOfBirth.setText(DateUtil.formatDate(patient.getDateOfBirth()));
            age.setText(patient.getPatientAge());
        }
    }

    @FXML
    private void onViewVitals() {
        if (!vitalsRecorded()) {
            AlertUtil.showAlert("Missing Vitals", "Vitals have not been recorded", Alert.AlertType.INFORMATION);
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("main/resources/view/view-vitals.fxml"));

            Stage stage = new Stage();
            stage.setScene(new Scene(loader.load()));
            stage.initOwner(container.getScene().getWindow());
            stage.initModality(Modality.WINDOW_MODAL);
            stage.setResizable(false);
            stage.setTitle("Vitals");

            ViewVitalsController controller = loader.getController();
            controller.setVisitId(queueId);

            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean vitalsRecorded() {
        String sql = "select visitId from vitals where visitId = " + queueId;
        ResultSet resultSet = DBUtil.executeQuery(sql);
        try {
            return resultSet != null && resultSet.next();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @FXML
    private void onProcedure() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource
                    ("main/resources/view/patient_procedures.fxml"));
            Parent pane = loader.load();

            //set stage
            Stage procedureStage = new Stage();
            procedureStage.setTitle("Procedure");
            procedureStage.initModality(procedureStage.getModality());
            procedureStage.setResizable(false);
            procedureStage.setScene(new Scene(pane));
            procedureStage.initOwner(container.getScene().getWindow());

            //controller
            PatientProceduresController controller = loader.getController();
            controller.setOutpatientMode();
            controller.setVisitId(queueId);
            controller.setStage(procedureStage);

            //show
            procedureStage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onMedication() {
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
            controller.setPatientId(currentPatientId);
            controller.setVisitId(queueId);

            //show window
            stage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @FXML
    private void onRadiologyRequest() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource
                    ("main/resources/view/create_radiology_request.fxml"));
            Parent node = loader.load();

            //set stage
            Stage stage = new Stage();
            stage.initModality(Modality.WINDOW_MODAL);
            stage.setResizable(false);
            stage.setScene(new Scene(node));
            stage.initOwner(container.getScene().getWindow());

            CreateRadiologyRequestController controller = loader.getController();
            controller.setPatientInfo(queueId, currentPatientId, true);

            stage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onLabRequest() {
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
            controller.setParameters(queueId, currentPatientId, false);
            controller.setStage(labRequestStage);

            labRequestStage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onAddDiagnosis() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("main/resources/view/diagnosis.fxml"));
            Stage stage = new Stage();
            stage.setScene(new Scene(loader.load()));
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(container.getScene().getWindow());

            DiagnosisController controller = loader.getController();
            controller.setParameters(queueId, PatientCategory.OUTPATIENT);

            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onFollowUpVisit() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("main/resources/view/appointment_scheduler.fxml"));
            Stage stage = new Stage();
            stage.setScene(new Scene(loader.load()));
            stage.initModality(Modality.WINDOW_MODAL);
            stage.setResizable(false);
            stage.initOwner(container.getScene().getWindow());

            AppointmentController controller = loader.getController();
            controller.setParameters(Main.currentUser.getUserId(), currentPatientId);
            controller.setStage(stage);
            stage.show();

            Rectangle2D rectangle2D = Screen.getPrimary().getBounds();
            stage.setY(0);
            stage.setX((rectangle2D.getWidth() - stage.getWidth()) / 2);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onEditAllergies() {

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("main/resources/view/allergies.fxml"));
            Stage stage = new Stage();
            stage.setResizable(false);
            stage.setScene(new Scene(loader.load()));
            stage.initOwner(container.getScene().getWindow());
            stage.initModality(Modality.WINDOW_MODAL);

            AllergiesController controller = loader.getController();
            controller.setPatientId(currentPatientId);
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onViewHistory() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("main/resources/view/medical_records.fxml"));
            Stage stage = new Stage();
            stage.setScene(new Scene(loader.load()));
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(container.getScene().getWindow());

            MedicalRecordsController controller = loader.getController();
            controller.setPatientId(currentPatientId);
            stage.showAndWait();
            stage.setResizable(false);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onSave() {
        ClinicVisitNotes notes = new ClinicVisitNotes();
        notes.setVisitId(queueId);
        notes.setPatientId(currentPatientId);
        notes.setPrimaryComplains(chiefComplains.getText());
        notes.setPhysicalExam(physicalExam.getText());
        notes.setInvestigation(investigation.getText());
        notes.setTreatment(treatment.getText());
        notes.setMedicalHistory(medicalHistory.getText());

        if (DBUtil.addClinicVisitNotes(notes)) {
            AlertUtil.showAlert("Changes Saved",
                    "Changes have been successfully changed", Alert.AlertType.INFORMATION);
        } else {
            AlertUtil.showAlert("Error",
                    "An error occurred while trying to save changes!", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void onDischarge() {
        //TODO : close case
        String sql = "UPDATE Queues set Status = '" + PatientQueue.Status.DISCHARGED + "' " +
                "WHERE VisitID = " + queueId;
        if (DBUtil.executeStatement(sql)) {
            AlertUtil.showAlert("Patient Discharge",
                    "Yay! Patient successfully removed from queue.", Alert.AlertType.INFORMATION);
            stage.close();
        }
    }
    void setQueueId(int queueId) {
        this.queueId = queueId;
        setNotes();
    }

    public void setPatientId(String patientId) {
        this.currentPatientId = patientId;
        setPatientDetails();
    }


    @FXML
    private void onAdmit() {
        if (!alreadyAdmitted()) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource
                        ("main/resources/view/admit-patient.fxml"));
                Stage stage = new Stage();
                stage.setScene(new Scene(loader.load()));
                stage.setTitle("Admit Patient");
                stage.initOwner(container.getScene().getWindow());

                Patient patient = PatientDAO.getPatient("select * from patients where patientId ='" + currentPatientId +
                        "'");
                Inpatient inpatient = new Inpatient();
                inpatient.setPatientId(currentPatientId);
                inpatient.setInpatientNumber(getInpatientNumber());

                AdmitPatientController controller = loader.getController();
                controller.setParameters(patient, inpatient, getPaymentMode(), getBillNumber());
                controller.setStage(stage);

                stage.showAndWait();
                if (controller.isAdmissionSuccessful()) {
                    String sql = "UPDATE Queues set Status = '" + PatientQueue.Status.DISCHARGED + "' " +
                            "WHERE VisitID = " + queueId;
                    DBUtil.executeStatement(sql);
                    stage.close();
                }

            } catch (IOException e) {
                e.printStackTrace();
            }

        } else {
            AlertUtil.showAlert("Patient Admitted", "This patient is already admitted.", Alert.AlertType.ERROR);
        }
    }

    private Integer getBillNumber() {
        String sql = "select bill_number from queues where visitId = " + queueId;
        ResultSet resultSet = DBUtil.executeQuery(sql);
        try {
            if (resultSet != null && resultSet.next()) {
                return resultSet.getInt("bill_number");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private boolean alreadyAdmitted() {
        String sql = "select admission_num from inpatients where " +
                "patient_id = '" + currentPatientId + "' " +
                "and status = '" + Inpatient.Status.ADMITTED + "'";
        try {
            ResultSet resultSet = DBUtil.executeQuery(sql);
            return (resultSet != null && resultSet.next());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private PaymentMode getPaymentMode() {
        String sql = "select payment_mode from queues where visitId = " + queueId;
        ResultSet resultSet = DBUtil.executeQuery(sql);
        try {
            if (resultSet != null && resultSet.next()) {
                return PaymentMode.valueOf(resultSet.getString("payment_mode"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String getInpatientNumber() {
        int min = 1001;
        String sql = "select inpatient_num from inpatients";
        ResultSet resultSet = DBUtil.executeQuery(sql);
        if (resultSet != null) {
            try {
                while (resultSet.next()) {
                    int id = NumberUtil.stringToInt(resultSet.getString("inpatient_num"));
                    if (id != -1) {
                        if (id > 1001) {
                            min = id;
                        }
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return Integer.toString(min);
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }
}
