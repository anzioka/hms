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
import main.java.util.*;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by alfonce on 12/07/2017.
 */
public class PrescriptionReportController {

    @FXML
    private Label reportTitle;

    @FXML
    private TableView<List<String>> tableView;

    @FXML
    private VBox container;

    @FXML
    private HBox buttonBar;

    @FXML
    private TableColumn<List<String>, String> doctorName, patientName, nameOfDrug, quantity, cost;
    private String date;
    private String selectedMedicine;

    @FXML
    public void initialize() {

        setUpTable();
    }

    private void setUpTable() {
        doctorName.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().get(0)));
        patientName.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().get(1) + " " + param
                .getValue().get(2)));
        nameOfDrug.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().get(3)));
        quantity.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().get(4)));
        cost.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().get(5)));
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

    void setDate(String date) {
        this.date = date;
        if (selectedMedicine == null) {
            tableView.setItems(getAllPrescriptions());
            reportTitle.setText("Prescription Summary, " + DateUtil.formatDateLong(DateUtil.parseDate(date)));
        } else {
            tableView.setItems(getMedicineSpecificPrescriptions());
            reportTitle.setText("'" + selectedMedicine + "' Prescription Summary, " + DateUtil.formatDateLong
                    (DateUtil.parseDate(date)));
            nameOfDrug.setVisible(false);
        }
    }

    private ObservableList<List<String>> getMedicineSpecificPrescriptions() {
        String sql = "SELECT DISTINCT Users.LastName, Users.UserCategory, Patients.FirstName, Patients.LastName, " +
                "Prescriptions.Quantity, Drugs.SellingPrice\n" +
                "FROM((((Prescriptions\n" +
                "INNER JOIN Queues ON Prescriptions.VisitId = Queues.VisitId)\n" +
                "INNER JOIN Patients ON Patients.PatientID = Queues.PatientID)\n" +
                "INNER JOIN Users ON Users.ID = Queues.DoctorID)\n" +
                "INNER JOIN Drugs ON Drugs.Name = Prescriptions.MedicineName)\n" +
                "WHERE Queues.DateCreated = '" + DateUtil.parseDate(date) + "'\n " +
                "AND Prescriptions.MedicineName = '" + selectedMedicine + "'";
        return getResultsFromResultSet(sql);
    }

    private ObservableList<List<String>> getAllPrescriptions() {
        String sql = "SELECT DISTINCT Users.LastName, Users.UserCategory, Patients.FirstName, Patients.LastName, " +
                "Prescriptions" +
                ".MedicineName, " +
                "Prescriptions.Quantity, Drugs.SellingPrice\n" +
                "FROM((((Prescriptions\n" +
                "INNER JOIN Queues ON Prescriptions.VisitId = Queues.VisitId)\n" +
                "INNER JOIN Patients ON Patients.PatientID = Queues.PatientID)\n" +
                "INNER JOIN Users ON Users.ID = Queues.DoctorID)\n" +
                "INNER JOIN Drugs ON Drugs.Name = Prescriptions.MedicineName)\n" +
                "WHERE Queues.DateCreated = '" + DateUtil.parseDate(date) + "'";
        return getResultsFromResultSet(sql);

    }

    private ObservableList<List<String>> getResultsFromResultSet(String sql) {
        ObservableList<List<String>> prescriptions = FXCollections.observableArrayList();
        try {
            ResultSet resultSet = DBUtil.executeQuery(sql);
            if (resultSet != null) {
                while (resultSet.next()) {
                    List<String> entry = new ArrayList<>();
                    User user = new User();
                    user.setCategory(UserCategory.valueOf(resultSet.getString("UserCategory")));
                    user.setLastName(resultSet.getString("LastName"));
                    entry.add(user.toString());
                    entry.add(resultSet.getString("FirstName"));
                    entry.add(resultSet.getString("Patients.LastName"));

                    if (selectedMedicine == null) {
                        entry.add(resultSet.getString("MedicineName"));
                    } else {
                        entry.add(null);
                    }
                    entry.add(resultSet.getString("Quantity"));
                    double sp = resultSet.getDouble("SellingPrice") * resultSet.getInt("Quantity");
                    entry.add(CurrencyUtil.formatCurrency(sp));
                    prescriptions.add(entry);

                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return prescriptions;
    }

    void setSingleMedicine(String selectedMedicine) {
        this.selectedMedicine = selectedMedicine;
    }
}
