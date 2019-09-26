package main.java.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import main.Main;
import main.java.model.FamilyRelation;
import main.java.model.MaritalStatus;
import main.java.model.Patient;
import main.java.model.Permission;
import main.java.util.AgeUtil;
import main.java.util.AlertUtil;
import main.java.util.DBUtil;
import main.java.util.DateUtil;

import java.time.LocalDate;

/**
 * Created by alfonce on 27/07/2017.
 */
public class PatientDetailsController {
    @FXML
    private Label ageLabel;
    @FXML
    private ChoiceBox<MaritalStatus> maritalStatusChoiceBox;
    @FXML
    private RadioButton male, female;
    @FXML
    private DatePicker datePicker;
    @FXML
    private ChoiceBox<FamilyRelation> familyRelationChoiceBox;
    @FXML
    private TextField nextOfKinFirstName, nextOfKinLastName, nextOfKinPhoneNumber, phoneNumber, residence, firstName, lastName, NHIFNumber;
    private Patient patient;
    private ToggleGroup toggleGroup;
    private Stage stage;
    private PatientRecordsController context;

    @FXML
    public void initialize() {
        familyRelationChoiceBox.setItems(FXCollections.observableArrayList(FamilyRelation.values()));
        maritalStatusChoiceBox.setItems(FXCollections.observableArrayList(MaritalStatus.values()));

        datePicker.setConverter(DateUtil.getDatePickerConverter());
        datePicker.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && newValue.isBefore(LocalDate.now())) {
                ageLabel.setText(AgeUtil.getAge(newValue));
            }
        });
        toggleGroup = new ToggleGroup();
        toggleGroup.getToggles().addAll(male, female);
        for (TextField textField : new TextField[]{nextOfKinFirstName, nextOfKinLastName, nextOfKinPhoneNumber, phoneNumber, residence, firstName, lastName, NHIFNumber}) {
            textField.setEditable(Main.userPermissions.get(Permission.EDIT_EXISTING_PATIENTS));
        }
        for (Toggle toggle : toggleGroup.getToggles()) {
            ((RadioButton)toggle).setDisable(!Main.userPermissions.get(Permission.EDIT_EXISTING_PATIENTS));
        }
    }

    void setStage(Stage stage) {
        this.stage = stage;
    }

    void setPatient(Patient patient) {
        this.patient = patient;
        setPatientDetails();
    }

    private void setPatientDetails() {
        firstName.setText(patient.getFirstName());
        lastName.setText(patient.getLastName());
        ageLabel.setText(patient.getPatientAge());
        maritalStatusChoiceBox.setValue(patient.getMaritalStatus());
        if (patient.getSexuality().toLowerCase().equals("female")) {
            toggleGroup.selectToggle(female);
        } else {
            toggleGroup.selectToggle(male);
        }

        nextOfKinFirstName.setText(patient.getContactFirstName());
        nextOfKinLastName.setText(patient.getContactLastName());
        nextOfKinPhoneNumber.setText(patient.getContactTelephone());
        familyRelationChoiceBox.setValue(patient.getContactRelationship());
        phoneNumber.setText(patient.getTelephoneNumber());

        residence.setText(patient.getResidence());
        NHIFNumber.setText(patient.getNHIFNumber());
        datePicker.setValue(patient.getDateOfBirth());
        ageLabel.setText(AgeUtil.getAge(patient.getDateOfBirth()));
    }

    @FXML
    private void onSaveChanges() {
        updatePatientDetails();
        if (DBUtil.addPatient(patient)) {
            AlertUtil.showAlert("Edit Patient", "Patient information has been updated", Alert.AlertType.INFORMATION);
            DBUtil.saveActivity("Updated patient '" + patient.getFirstName() + " " + patient.getLastName() + "' information");
            stage.close();
            if (context != null) {
                context.refreshTable();
            }
        } else {
            AlertUtil.showAlert("Error", "An error occurred while attempting to update patient information", Alert
                    .AlertType.ERROR);
        }
    }

    private void updatePatientDetails() {
        if (datePicker.getValue() != null && datePicker.getValue().isBefore(LocalDate.now())) {
            patient.setDateOfBirth(datePicker.getValue());
        }
        if (maritalStatusChoiceBox.getValue() != null) {
            patient.setMaritalStatus(maritalStatusChoiceBox.getValue());
        }
        if (phoneNumber.getText() != null && !phoneNumber.getText().isEmpty()) {
            patient.setTelephoneNumber(phoneNumber.getText());
        }

        if (NHIFNumber.getText() != null && !NHIFNumber.getText().isEmpty()) {
            patient.setNHIFNumber(NHIFNumber.getText());
        }

        if (residence.getText() != null && !residence.getText().isEmpty()) {
            patient.setResidence(residence.getText());
        }
        if (nextOfKinFirstName.getText() != null && !nextOfKinFirstName.getText().isEmpty()) {
            patient.setContactFirstName(nextOfKinFirstName.getText());
        }
        if (nextOfKinLastName.getText() != null && !nextOfKinLastName.getText().isEmpty()) {
            patient.setContactLastName(nextOfKinLastName.getText());
        }

        if (familyRelationChoiceBox.getValue() != null) {
            patient.setContactRelationship(familyRelationChoiceBox.getValue());
        }

        if (nextOfKinPhoneNumber.getText() != null && !nextOfKinPhoneNumber.getText().isEmpty()) {
            patient.setContactTelephone(nextOfKinPhoneNumber.getText());
        }

        if (firstName.getText() != null && !firstName.getText().isEmpty()) {
            patient.setFirstName(firstName.getText());
        }
        if (lastName.getText() != null && !lastName.getText().isEmpty()) {
            patient.setLastName(lastName.getText());
        }
        patient.setSexuality(((RadioButton) toggleGroup.getSelectedToggle()).getText());
    }

    void setContext(PatientRecordsController context) {
        this.context = context;
    }
}
