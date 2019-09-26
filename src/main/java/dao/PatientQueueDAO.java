package main.java.dao;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import main.java.model.PatientQueue;
import main.java.model.PaymentMode;
import main.java.util.DBUtil;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Created by alfonce on 26/04/2017.
 */
public class PatientQueueDAO {

    public static ObservableList<PatientQueue> getPatientsForTriage() {
        String sql = "select * from queues where visitId != ALL (select visitId from vitals) " +
                "AND NOT Queues.Status = 'Discharged' " +
                "AND NOT Queues.DoctorId = 0 ";
        ResultSet resultSet = DBUtil.executeQuery(sql);
        return getQueueListFromResultSet(resultSet);
    }

    private static ObservableList<PatientQueue> getQueueListFromResultSet(ResultSet resultSet) {
        ObservableList<PatientQueue> patientQueues = FXCollections.observableArrayList();
        try {
            if (resultSet != null) {
                while (resultSet.next()) {
                    PatientQueue queue = new PatientQueue();
                    queue.setQueueId(resultSet.getInt("VisitID"));
                    queue.setDateCreated(resultSet.getObject("DateCreated", LocalDate.class));
                    queue.setTimeCreated(resultSet.getObject("TimeCreated", LocalTime.class));
                    queue.setPatientId(resultSet.getString("PatientID"));
                    queue.setPaymentMode(PaymentMode.valueOf(resultSet.getString("payment_mode")));
                    queue.setBillNumber(resultSet.getInt("bill_number"));
                    queue.setDoctorId(resultSet.getInt("DoctorId"));
                    queue.setStatus(PatientQueue.Status.valueOf(resultSet.getString("Status")));
                    patientQueues.add(queue);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return patientQueues;
    }

    public static ObservableList<PatientQueue> getQueuedPatients(String sql) {
        ResultSet resultSet = DBUtil.executeQuery(sql);
        return getQueueListFromResultSet(resultSet);
    }
}
