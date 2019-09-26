package main.java.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import main.java.util.DBUtil;
import main.java.util.DateUtil;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;

public class ViewAdmissionDetailsController {
    @FXML
    private Label patientName, dateAdmitted, timeAdmitted, ward, bed, paymentMode, doctor, dateDischarged, timeDischarged;

    @FXML
    private void initialize() {
        setAdmissionDetails();
    }

    private void setAdmissionDetails() {
        String sql = "select patients.FirstName, patients.LastName ,inpatients.*, Users.LastName, wards.ward_name " +
                "from inpatients " +
                "inner join patients on patients.PatientId = inpatients.patient_id " +
                "inner join users on users.Id = inpatients.doctor_id " +
                "inner join wards on inpatients.ward_id = wards.ward_id " +
                "where admission_num = " + ViewInpatientController.patient.getAdmissionNumber();

        ResultSet resultSet = DBUtil.executeQuery(sql);
        if (resultSet != null) {
            try {
                if (resultSet.next()) {
                    patientName.setText(resultSet.getString("patients.FirstName") + " " + resultSet.getString("patients.LastName"));
                    dateAdmitted.setText(DateUtil.formatDateLong(resultSet.getObject("date_admitted", LocalDate.class)));
                    timeAdmitted.setText(DateUtil.formatTime(resultSet.getObject("time_admitted", LocalTime.class)));
                    ward.setText(resultSet.getString("ward_name"));
                    bed.setText(resultSet.getInt("bed_id") + 1 + "");
                    paymentMode.setText(resultSet.getString("payment_mode"));
                    doctor.setText("Dr." + resultSet.getString("Users.LastName"));
                    if (resultSet.getObject("date_discharged") != null) {
                        dateDischarged.setText(DateUtil.formatDateLong(resultSet.getObject("date_discharged", LocalDate.class)));
                    }
                    if (resultSet.getObject("time_discharged") != null) {
                        timeDischarged.setText(DateUtil.formatTime(resultSet.getObject("time_discharged", LocalTime.class)));
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

}
