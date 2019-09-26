package main.java.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import main.java.model.PatientQueue;
import main.java.model.PatientVitals;
import main.java.util.AlertUtil;
import main.java.util.ColorCode;
import main.java.util.DBUtil;
import main.java.util.NumberUtil;

import java.text.DecimalFormat;

/**
 * Created by alfonce on 21/06/2017.
 */
public class PatientVitalsController {

    @FXML
    private TextField weight;

    @FXML
    private TextField height;

    @FXML
    private TextField bodyMassIndex;

    @FXML
    private TextField systolicBp;

    @FXML
    private TextField diastolicBp;

    @FXML
    private TextField respirationRate;

    @FXML
    private TextField pulseRate;

    @FXML
    private TextField spO2;

    @FXML
    private TextField bodyTemperature;

    @FXML
    private RadioButton bloodGroupUnknown;

    @FXML
    private RadioButton bloodGroupA;

    @FXML
    private RadioButton bloodGroupB;

    @FXML
    private RadioButton bloodGroupdAB;

    @FXML
    private RadioButton bloodGroupO;

    @FXML
    private RadioButton rFactorUnknown;

    @FXML
    private RadioButton rFactorPositive;

    @FXML
    private RadioButton rFactorNegative;

    @FXML
    private ChoiceBox<ColorCode> colorCode;

    private Stage stage;

    private String selectedRFactor = null;
    private String selectedBloodGroup = null;
    private int uniqueId;
    private boolean inpatient;

    @FXML
    private void initialize() {
        //disable editing BMI
        bodyMassIndex.setEditable(false);

        //add listener for text changes in height/weight -> find BMI
        height.textProperty().addListener((observable, oldValue, newValue) -> {
            calculateBMI();
        });

        weight.textProperty().addListener((observable, oldValue, newValue) -> {
            calculateBMI();
        });

        //color code
        colorCode.setItems(FXCollections.observableArrayList(ColorCode.values()));
        colorCode.getSelectionModel().select(ColorCode.YELLOW); //default is yellow

        //blood group
        ToggleGroup bloodGroup = new ToggleGroup();
        bloodGroup.getToggles().addAll(bloodGroupA, bloodGroupB, bloodGroupdAB, bloodGroupO, bloodGroupUnknown);
        bloodGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            RadioButton selected = (RadioButton) newValue;
            if (selected != null) {
                selectedBloodGroup = selected.getText();
            }
        });
        bloodGroup.selectToggle(bloodGroupUnknown);

        //rhesus factor
        ToggleGroup rhesusFactorToggleGroup = new ToggleGroup();
        rhesusFactorToggleGroup.getToggles().addAll(rFactorNegative, rFactorPositive, rFactorUnknown);
        rhesusFactorToggleGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            RadioButton selected = (RadioButton) newValue;
            if (selected != null) {
                selectedRFactor = selected.getText();
            }
        });
        rhesusFactorToggleGroup.selectToggle(rFactorUnknown);

    }

    private void calculateBMI() {
        double patientWeight = NumberUtil.stringToDouble(weight.getText());
        double patientHeight = NumberUtil.stringToDouble(height.getText()); //height in cm

        if (patientWeight != -1 && patientHeight != -1) {
            double bmi = patientWeight / Math.pow(patientHeight / 100.0, 2);

            DecimalFormat format = new DecimalFormat("0.0");
            bodyMassIndex.setText(format.format(bmi));
        }
    }

    @FXML
    private void onClose() {
        stage.close();
    }

    void setStage(Stage stage) {
        this.stage = stage;
    }

    private double stringToDouble(String string) {
        if (string == null || string.isEmpty()) {
            return 0;
        } else {
            return Double.parseDouble(string);
        }
    }

    @FXML
    private void onSave() {
        PatientVitals vitals = new PatientVitals();
        if (inpatient) {
            vitals.setAdmissionNum(uniqueId);
        } else {
            vitals.setQueueId(uniqueId);
        }
        vitals.setBmi(stringToDouble(bodyMassIndex.getText()));
        vitals.setColorCode(colorCode.getSelectionModel().getSelectedItem().getValue());
        vitals.setBloodGroup(selectedBloodGroup);
        vitals.setRhesusFactor(selectedRFactor);
        vitals.setHeight(stringToDouble(height.getText()));
        vitals.setWeight(stringToDouble(weight.getText()));
        vitals.setBodyTemp(stringToDouble(bodyTemperature.getText()));
        vitals.setDiastolicBp(stringToDouble(diastolicBp.getText()));
        vitals.setSystolicBp(stringToDouble(systolicBp.getText()));
        vitals.setSpo2(stringToDouble(spO2.getText()));
        vitals.setPulseRate(stringToDouble(pulseRate.getText()));
        vitals.setRespiratoryRate(stringToDouble(respirationRate.getText()));

        if (DBUtil.addPatientVitals(vitals)) {
            AlertUtil.showAlert("Patient Vitals", "Patient vitals have been saved", Alert.AlertType.CONFIRMATION);
            if (!inpatient) {
                String sql = "update queues set Status = '" + PatientQueue.Status.AWAITING_CONSULTATION + "' " +
                        "where VisitId = " + uniqueId;
                DBUtil.executeStatement(sql);
            }
            onClose();
        } else {
            AlertUtil.showAlert("Save Error", "An error occurred while trying to save patient vitals", Alert
                    .AlertType.ERROR);
        }
    }

    public void setParameters(int id, boolean inpatient) {
        this.uniqueId = id;
        this.inpatient = inpatient;
    }
}
