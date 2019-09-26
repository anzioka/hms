package main.java.controller;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import main.Main;
import main.java.dao.PatientDAO;
import main.java.model.*;
import main.java.util.*;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;
import static main.java.controller.RadiologyController.*;

public class CompleteRadiologyRequestController {
    private Stage stage;
    private Map<String, String> data;
    @FXML
    private Label patientNumber, sex, firstName, lastName, dateOfBirth, age, testName, date, requester, currentUser;
    @FXML
    private TextArea notes;

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void setData(Map<String, String> data) {
        this.data = data;
        getPatientInfo();
        currentUser.setText(Main.currentUser.getFirstName() + " " + Main.currentUser.getLastName());
        testName.setText(data.get(CATEGORY) + " (" + data.get(DESCRIPTION) + ")");
        date.setText(data.get(DATE));
        requester.setText(data.get(DOCTOR));
    }

    private void getPatientInfo() {
        Task<Patient> task = new Task<Patient>() {
            @Override
            protected Patient call() {
                return PatientDAO.getPatient("select * from patients where PatientId = " + data.get(PATIENT_ID));
            }
        };
        if (task.getValue() != null) {
            setPatientDetails(task.getValue());
        }

        //TODO change this such that if a patient has been admitted before, patient num SHOULD be the assigned IN
        if (data.get(PATIENT_CATEGORY).equals(PatientCategory.OUTPATIENT.toString())) {
            patientNumber.setText(data.get(PATIENT_ID));
        } else{
            ResultSet resultSet = DBUtil.executeQuery("select inpatient_num from inpatients " +
                    "where patient_id = '" + data.get(PATIENT_ID) + "' limit 1");
            try {
                if (resultSet != null && resultSet.next()) {
                    patientNumber.setText(resultSet.getString("inpatient_num"));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        new Thread(task).start();
    }

    private void setPatientDetails(Patient patient) {
        sex.setText(patient.getSexuality());
        firstName.setText(patient.getFirstName());
        lastName.setText(patient.getLastName());
        dateOfBirth.setText(DateUtil.formatDate(patient.getDateOfBirth()));
        age.setText(AgeUtil.getAge(patient.getDateOfBirth()));
    }

    @FXML
    private void onSubmitResults() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to submit result? You will not " +
                "be able to edit result after submitting.", ButtonType.OK, ButtonType.CANCEL);
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (DBUtil.executeStatement("update radiology_requests set status = '" + RadiologyRequest.Status
                    .COMPLETED.name() + "', result = '" + notes.getText() + "' where request_id = " + data.get(REQUEST_ID)
            )) {
                AlertUtil.showAlert("", "Test result successfully submitted!", Alert.AlertType.INFORMATION);
                stage.close();
            } else {
                AlertUtil.showGenericError();
            }
        }
    }
}
