package main.java.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import main.java.model.Inpatient;
import main.java.model.TimePeriod;
import main.java.util.AlertUtil;
import main.java.util.DBUtil;
import main.java.util.DateUtil;
import main.java.util.TimePicker;

import java.time.LocalDate;

public class DischargePatientController {
    Inpatient inpatient = ViewInpatientController.patient;
    private TimePicker timePicker;
    @FXML
    private Label description;
    @FXML
    private ChoiceBox<String> hourPicker, minutePicker;
    @FXML
    private ChoiceBox<TimePeriod> timePeriodChoiceBox;
    @FXML
    private DatePicker datePicker;
    private Stage stage;
    private ViewInpatientController context;

    @FXML
    private void initialize() {
        datePicker.setConverter(DateUtil.getDatePickerConverter());
        datePicker.setValue(LocalDate.now());

        timePicker = new TimePicker(hourPicker, minutePicker, timePeriodChoiceBox);
        timePicker.configureTime();

        description.setText(description.getText() + " '" + inpatient.getFirstName() + " " + inpatient.getLastName() + "' ( INPATIENT NO. " + inpatient.getInpatientNumber() + " )");
    }

    @FXML
    private void onDischarge() {
        String sql = "update inpatients set status = '" + Inpatient.Status.DISCHARGED + "', " +
                "date_discharged = '" + datePicker.getValue() + "', time_discharged = '" + timePicker.getSelectedTime() + "' " +
                "where admission_num = " + inpatient.getAdmissionNumber();
        if (DBUtil.executeStatement(sql)) {
            AlertUtil.showAlert("Discharge Patient", inpatient.getFirstName() + " " + inpatient.getLastName() + "( INPATIENT NO. " + inpatient.getInpatientNumber() + " ) has been successfully discharged", Alert.AlertType.INFORMATION);
            stage.close();
            context.removePatient();
        } else {
            AlertUtil.showAlert("Discharge Patient", "An error occurred while attempting to discharge patient.", Alert.AlertType.ERROR);
        }
    }

    public Stage getStage() {
        return stage;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
        System.out.print("This is yet another example");
    }

    public void setContext(ViewInpatientController context) {
        this.context = context;
    }
}
