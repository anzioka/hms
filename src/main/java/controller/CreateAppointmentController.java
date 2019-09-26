package main.java.controller;

import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import main.java.dao.UsersDAO;
import main.java.model.Appointment;
import main.java.model.TimePeriod;
import main.java.model.User;
import main.java.model.UserCategory;
import main.java.util.AlertUtil;
import main.java.util.DBUtil;
import main.java.util.TimePicker;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;

public class CreateAppointmentController {
    @FXML
    private ChoiceBox<String> minuteChoiceBox, hourChoiceBox;
    @FXML
    private ChoiceBox<TimePeriod> timePeriodChoiceBox;
    @FXML
    private ChoiceBox<User> doctorChoiceBox;
    @FXML
    private TextField editNote;
    @FXML
    private Label patientName;
    @FXML
    private DatePicker datePicker;
    private TimePicker timePicker;

    private String patientId;
    private Stage stage;
    private int doctorId;

    @FXML
    private void initialize() {
        timePicker = new TimePicker(hourChoiceBox, minuteChoiceBox, timePeriodChoiceBox);
    }

    private void getDoctors() {
        Task<ObservableList<User>> task = new Task<ObservableList<User>>() {
            @Override
            protected ObservableList<User> call() {
                return UsersDAO.getUserObservableList("select * from users where UserCategory = '" + UserCategory.DOCTOR + "'");
            }
        };
        task.setOnSucceeded(event -> {
            doctorChoiceBox.setItems(task.getValue());

            for (User user : doctorChoiceBox.getItems()) {
                if (user.getUserId() == doctorId) {
                    doctorChoiceBox.getSelectionModel().select(user);
                }
            }
        });
        new Thread(task).start();
    }

    @FXML
    private void onBookAppointment() {
        if (validInput()) {
            if (DBUtil.saveAppointment(createAppointment())) {
                AlertUtil.showAlert("Doctor's Appointment", "Successfully booked appointment!", Alert.AlertType.INFORMATION);
            } else {
                AlertUtil.showAlert("", "An error occurred while attempting to save appointment", Alert.AlertType.ERROR);
            }
            stage.close();
        }
    }

    private Appointment createAppointment() {
        Appointment appointment = new Appointment();
        appointment.setDate(datePicker.getValue());
        appointment.setDoctorId(doctorChoiceBox.getValue().getUserId());
        appointment.setPatientId(patientId);
        appointment.setTime(timePicker.getSelectedTime());
        return appointment;
    }

    private boolean validInput() {
        String errorMsg = "";
        if (datePicker.getValue() == null) {
            errorMsg += "Date required!\n";
        } else if (datePicker.getValue().isBefore(LocalDate.now())) {
            errorMsg += "Appointment date cannot be in the past!\n";
        }
        if (timePicker.getSelectedTime() == null) {
            errorMsg += "Appointment time required!\n";
        }
        if (doctorChoiceBox.getValue() == null) {
            errorMsg += "Doctor required!\n";
        }

        if (errorMsg.isEmpty()) {
            return true;
        }
        AlertUtil.showAlert("Input Error(s)", errorMsg, Alert.AlertType.ERROR);
        return false;
    }

    public void setParameters(LocalTime time, LocalDate date, int doctorId, String patientId) {
        this.patientId = patientId;
        this.doctorId = doctorId;
        if (time != null) {
            timePicker.setTime(time);
        }
        if (date != null) {
            datePicker.setValue(date);
        }
        getDoctors();


        patientName.setText(getPatientName());
    }

    private String getPatientName() {
        ResultSet resultSet = DBUtil.executeQuery("select FirstName, LastName from patients where patientId = '" + patientId + "'");
        try {
            if (resultSet != null && resultSet.next()) {
                return resultSet.getString("FirstName") + " " + resultSet.getString("LastName");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }
}
