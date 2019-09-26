package main.java.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.print.Paper;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import main.java.model.User;
import main.java.model.UserCategory;
import main.java.util.AlertUtil;
import main.java.util.DBUtil;
import main.java.util.DateUtil;
import main.java.util.PrinterUtil;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by alfonce on 13/07/2017.
 */
public class LabRequestsSummaryController {

    @FXML
    private Label tableTitle;

    @FXML
    private TableView<List<String>> tableView;

    @FXML
    private TableColumn<List<String>, String> doctorCol, patientName, testName, timeRequested;
    @FXML
    private VBox container;
    @FXML
    private HBox buttonBar;

    @FXML
    public void initialize() {
        configureTable();
    }

    private void configureTable() {
        doctorCol.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().get(0)));
        patientName.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().get(1) + " " + param
                .getValue().get(2)));
        testName.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().get(3)));
        timeRequested.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().get(4)));

    }

    void setParameters(String labTest, int doctorId, String date) {
        LocalDate localDate = DateUtil.parseDate(date);
        String sql;

        if (labTest == null && doctorId == -1) {
            sql = "SELECT DISTINCT Users.UserCategory, Users.LastName, Patients.FirstName, Patients.LastName, " +
                    "LabRequests.TestName, LabRequests.TimeCreated " +
                    "from((( LabRequests \n" +
                    "INNER JOIN Queues on Queues.VisitId = LabRequests.VisitId)" +
                    "INNER JOIN Users on Users.ID = Queues.DoctorID)\n" +
                    "INNER JOIN Patients on Patients.PatientId = Queues.PatientId)\n" +
                    "WHERE Queues.DateCreated = '" + localDate + "'";

            tableTitle.setText("Lab Requests Summary on " + DateUtil.formatDateLong(localDate));

        } else if (labTest == null) {
            sql = "SELECT DISTINCT Patients.FirstName, Patients.LastName, LabRequests.TestName, " +
                    "LabRequests.TimeCreated " +
                    "FROM ((LabRequests " +
                    "INNER JOIN Queues on Queues.VisitId = LabRequests.VisitId) " +
                    "INNER JOIN Patients on Patients.PatientId = Queues.PatientId) " +
                    "WHERE Queues.DateCreated = '" + localDate + "' " +
                    "AND Queues.DoctorID = " + doctorId;
            doctorCol.setVisible(false);
            tableTitle.setText("Lab Requests by " + getDoctorName(doctorId) + " on " + DateUtil.formatDateLong
                    (localDate));

        } else if (doctorId != -1) {
            doctorCol.setVisible(false);
            testName.setVisible(false);
            sql = "SELECT DISTINCT Patients.FirstName, Patients.LastName, LabRequests.TimeCreated " +
                    "FROM(( LabRequests " +
                    "INNER JOIN Queues on Queues.VisitId = LabRequests.VisitId) " +
                    "INNER JOIN Patients on Patients.PatientId = Queues.PatientID) " +
                    "WHERE Queues.DateCreated = '" + localDate + "' " +
                    "AND Queues.DoctorId = " + doctorId + " " +
                    "AND LabRequests.TestName = '" + labTest + "'";
            tableTitle.setText("'" + labTest + "' Lab Requests by " + getDoctorName(doctorId) + " on " + DateUtil
                    .formatDateLong(localDate));

        } else {
            testName.setVisible(false);
            sql = "SELECT DISTINCT Users.UserCategory, Users.LastName, Patients.FirstName, Patients.LastName, " +
                    "LabRequests.TimeCreated " +
                    "FROM (((LabRequests " +
                    "INNER JOIN Queues on Queues.VisitID = LabRequests.VisitID)" +
                    "INNER JOIN Patients on Patients.PatientId = Queues.PatientID)" +
                    "INNER JOIN Users on Users.ID = Queues.DoctorID) " +
                    "WHERE Queues.DateCreated = '" + localDate + "' " +
                    "AND LabRequests.TestName = '" + labTest + "'";
            tableTitle.setText("'" + labTest + "' Lab Requests on " + DateUtil.formatDateLong(localDate));
        }

        tableView.setItems(searchDatabase(sql));

    }

    private String getDoctorName(int doctorId) {
        String sql = "select LastName from users where id = " + doctorId;
        String result = "Dr. X";
        try {
            ResultSet resultSet = DBUtil.executeQuery(sql);
            if (resultSet != null) {
                resultSet.next();
                result = "Dr. " + resultSet.getString("LastName");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return result;
    }

    private ObservableList<List<String>> searchDatabase(String sql) {
        ObservableList<List<String>> results = FXCollections.observableArrayList();
        try {
            ResultSet resultSet = DBUtil.executeQuery(sql);
            if (resultSet != null) {
                while (resultSet.next()) {
                    List<String> entry = new ArrayList<>();
                    if (doctorCol.isVisible()) {
                        User user = new User();
                        user.setCategory(UserCategory.valueOf(resultSet.getString("UserCategory")));
                        user.setLastName(resultSet.getString("Users.LastName"));
                        entry.add(user.toString());
                    } else {
                        entry.add(null);
                    }
                    entry.add(resultSet.getString("Patients.FirstName"));
                    entry.add(resultSet.getString("Patients.LastName"));

                    if (testName.isVisible()) {
                        entry.add(resultSet.getString("TestName"));
                    } else {
                        entry.add(null);
                    }

                    entry.add(DateUtil.getTimeFromLocalDateTime
                            (resultSet.getObject("TimeCreated", LocalDateTime.class)));

                    results.add(entry);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return results;
    }

    @FXML
    private void onPrint() {
        buttonBar.setVisible(false);
        buttonBar.setManaged(false);

        boolean print = PrinterUtil.printNode(container, Paper.A4);
        if (!print) {
            AlertUtil.showAlert("Printer Error", "Could not connect to printer", Alert.AlertType.ERROR);
        }
        buttonBar.setVisible(true);
        buttonBar.setManaged(true);
    }
}
