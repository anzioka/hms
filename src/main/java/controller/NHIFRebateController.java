package main.java.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import main.java.model.NHIFApplicability;
import main.java.util.AlertUtil;
import main.java.util.DBUtil;

public class NHIFRebateController {
    @FXML
    private ChoiceBox<NHIFApplicability> choiceBox;
    @FXML
    private TextField textField;
    @FXML
    private VBox container;

    @FXML
    private void initialize() {
        textField.setText(ViewInpatientController.patient.getNHIFNumber());
        choiceBox.setItems(FXCollections.observableArrayList(NHIFApplicability.values()));
        choiceBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            textField.setDisable(newValue == NHIFApplicability.NON_APPLICABLE);
        });
        if (ViewInpatientController.patient.isNhifApplicable()) {
            choiceBox.getSelectionModel().select(NHIFApplicability.APPLICABLE);
        } else {
            choiceBox.getSelectionModel().select(NHIFApplicability.NON_APPLICABLE);
        }

    }

    @FXML
    private void onSave() {
        boolean error = false;
        NHIFApplicability applicability = choiceBox.getValue();
        if (applicability == NHIFApplicability.APPLICABLE) {
            if (textField.getText() == null || textField.getText().isEmpty()) {
                AlertUtil.showAlert("NHIF Number Required", "Please enter the patients NHIF number.", Alert.AlertType.ERROR);
                return;
            } else {
                String sql = "update patients set NHIFNumber = '" + textField.getText() + "' " +
                        "where patientId = '" + ViewInpatientController.patient.getPatientId() + "'";
                if (!DBUtil.executeStatement(sql)) {
                    error = true;
                }
            }

            String sql = "update inpatients set nhif_applicable = 1 " +
                    "where admission_num = " + ViewInpatientController.patient.getAdmissionNumber();
            if (!DBUtil.executeStatement(sql)) {
                error = true;
            }

        } else {
            String sql = "update inpatients set nhif_applicable = 0 " +
                    "where admission_num = " + ViewInpatientController.patient.getAdmissionNumber();
            if (!DBUtil.executeStatement(sql)) {
                error = true;
            }
        }
        if (error) {
            AlertUtil.showAlert("Error", "An error occurred while attempting to update patient's information", Alert.AlertType.ERROR);
        } else {
            AlertUtil.showAlert("NHIF Rebate Status", "Patient NHIF rebate applicability successfully updated!", Alert.AlertType.INFORMATION);
        }
        ViewInpatientController.patient.setNhifApplicable(applicability == NHIFApplicability.APPLICABLE);
        ViewInpatientController.patient.setNHIFNumber(textField.getText());

    }
}
