package main.java.controller;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import main.java.model.PatientVitals;
import main.java.util.DBUtil;
import main.java.util.DateUtil;

import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class ViewVitalsController {
    @FXML
    private Label date, weight, height, bmi, bloodGroup, rhFactor, systolicBp, diastolicBp, temperature, respiratoryRate, pulseRate, sp02, notes;

    public void setVisitId(int visitId) {
        getVitals(visitId);
    }

    private void getVitals(int visitId) {
        Task<PatientVitals> task = new Task<PatientVitals>() {
            @Override
            protected PatientVitals call() throws Exception {
                String sql = "select * from vitals where visitId = " + visitId;
                ResultSet resultSet = DBUtil.executeQuery(sql);
                if (resultSet != null && resultSet.next()) {
                    PatientVitals vitals = new PatientVitals();
                    vitals.setBmi(resultSet.getDouble("BMI"));
                    vitals.setWeight(resultSet.getDouble("Weight"));
                    vitals.setHeight(resultSet.getDouble("height"));
                    vitals.setSystolicBp(resultSet.getDouble("SystolicBp"));
                    vitals.setDiastolicBp(resultSet.getDouble("DiastolicBp"));
                    vitals.setBodyTemp(resultSet.getDouble("BodyTemperature"));
                    vitals.setRespiratoryRate(resultSet.getDouble("RespiratoryRate"));
                    vitals.setPulseRate(resultSet.getDouble("PulseRate"));
                    vitals.setSpo2(resultSet.getDouble("SpO2"));
                    vitals.setBloodGroup(resultSet.getString("BloodGroup"));
                    vitals.setRhesusFactor(resultSet.getString("RhesusFactor"));
                    vitals.setDateCreated(resultSet.getObject("DateCreated", LocalDate.class));
                    vitals.setTimeCreated(resultSet.getObject("TimeCreated", LocalTime.class));
                    return vitals;
                }
                return null;
            }
        };
        task.setOnSucceeded(event -> {
            setDetails(task.getValue());
        });
        new Thread(task).start();
    }

    private void setDetails(PatientVitals vitals) {
        if (vitals != null) {
            date.setText(DateUtil.formatDateTime(LocalDateTime.of(vitals.getDateCreated(), vitals.getTimeCreated())));
            weight.setText(vitals.getWeight() + "");
            height.setText(vitals.getHeight() + "");
            bmi.setText(vitals.getBmi() + "");
            bloodGroup.setText(vitals.getBloodGroup());
            rhFactor.setText(vitals.getRhesusFactor());
            systolicBp.setText(vitals.getSystolicBp() + "");
            diastolicBp.setText(vitals.getDiastolicBp() + "");
            temperature.setText(vitals.getBodyTemp() + "");
            respiratoryRate.setText(vitals.getRespiratoryRate() + "");
            pulseRate.setText(vitals.getPulseRate() + "");
            sp02.setText(vitals.getSpo2() + "");
            if (vitals.getTriageNotes() == null || vitals.getTriageNotes().isEmpty()) {
                notes.setVisible(false);
            } else {
                notes.setText(vitals.getTriageNotes());
            }
        }
    }

}
