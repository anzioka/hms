package main.java.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import main.java.model.Formulation;
import main.java.model.Prescription;
import main.java.util.DBUtil;
import main.java.util.DateUtil;
import main.java.util.StringUtil;

import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class DispensedDrugsController {
    private static final String DATE = "date";
    private static final String CATEGORY = "category";
    private static final String PATIENT_NAME = "patient_name";
    private static final String MEDICINE = "medicine";
    private static final String QUANTITY = "quantity";
    private static final String FORMULATION = "formulation";
    private static final String DOSAGE = "dosage";

    @FXML
    private TableView<Map<String, String>> tableView;
    @FXML
    private TableColumn<Map<String, String>, String> date, category, patientName, medicine, quantity, formulation, dosage;
    @FXML
    private DatePicker startDate, endDate;

    @FXML
    private void initialize() {
        startDate.setConverter(DateUtil.getDatePickerConverter());
        endDate.setConverter(DateUtil.getDatePickerConverter());
        setUpTable();
        onSearch();
    }

    private void setUpTable() {
        Label label = new Label("No dispense history found");
        label.getStyleClass().add("text-danger");
        tableView.setPlaceholder(label);

        date.setCellValueFactory(param -> StringUtil.getStringProperty(param.getValue().get(DATE)));
        category.setCellValueFactory(param -> StringUtil.getStringProperty(param.getValue().get(CATEGORY)));
        patientName.setCellValueFactory(param -> StringUtil.getStringProperty(param.getValue().get(PATIENT_NAME)));
        medicine.setCellValueFactory(param -> StringUtil.getStringProperty(param.getValue().get(MEDICINE)));
        quantity.setCellValueFactory(param -> StringUtil.getStringProperty(param.getValue().get(QUANTITY)));
        formulation.setCellValueFactory(param -> StringUtil.getStringProperty(param.getValue().get(FORMULATION)));
        dosage.setCellValueFactory(param -> StringUtil.getStringProperty(param.getValue().get(DOSAGE)));
    }

    @FXML
    private void onSearch() {
        LocalDate start = startDate.getValue() != null ? startDate.getValue() : LocalDate.now();
        startDate.setValue(start);
        LocalDate end = endDate.getValue() != null ? endDate.getValue() : LocalDate.now();
        endDate.setValue(end);

        Task<ObservableList<Map<String, String>>> task = new Task<ObservableList<Map<String, String>>>() {
            @Override
            protected ObservableList<Map<String, String>> call() throws Exception {
                ObservableList<Map<String, String>> list = FXCollections.observableArrayList();

                String sql = "select dosage, quantity, formulation, drugs.name, patients.FirstName, Patients.LastName, " +
                        "prescriptions.Admission_num, prescriptions.date_created from prescriptions " +
                        "inner join drugs on drugs.DrugCode = prescriptions.drug_code " +
                        "inner join inpatients on inpatients.admission_num " +
                        "inner join patients on patients.patientId = inpatients.patient_id " +
                        "where prescriptions.status = '" + Prescription.Status.COMPLETED + "' " +
                        "and date_created between '" + start + "' and '" + end + "' " +
                        "union " +

                        "select dosage, quantity, formulation, drugs.name, patients.FirstName, Patients.LastName, " +
                        "prescriptions.Admission_num, prescriptions.date_created from prescriptions " +
                        "inner join drugs on drugs.DrugCode = prescriptions.drug_code " +
                        "inner join queues on queues.VisitId = prescriptions.visit_id " +
                        "inner join patients on patients.patientId = queues.patientId " +
                        "where prescriptions.status = '" + Prescription.Status.COMPLETED + "' " +
                        "and date_created between '" + start + "' and '" + end + "'";

                ResultSet resultSet = DBUtil.executeQuery(sql);
                if (resultSet != null) {
                    while (resultSet.next()) {
                        Map<String, String> map = new HashMap<>();
                        map.put(DATE, DateUtil.formatDate(resultSet.getObject("date_created", LocalDate.class)));
                        map.put(DOSAGE, Prescription.Dosage.valueOf(resultSet.getString("dosage")).toString());
                        map.put(QUANTITY, resultSet.getString("quantity"));
                        if (resultSet.getString("formulation") != null) {
                            map.put(FORMULATION, Formulation.valueOf(resultSet.getString("formulation")).toString());
                        } else {
                            map.put(FORMULATION, null);
                        }
                        map.put(MEDICINE, resultSet.getString("name"));
                        map.put(PATIENT_NAME, resultSet.getString("FirstName") + " " + resultSet.getString("LastName"));
                        if (Integer.parseInt(resultSet.getString("admission_num")) == -1) {
                            map.put(CATEGORY, "Outpatient");
                        } else {
                            map.put(CATEGORY, "Inpatient");
                        }
                        list.add(map);
                    }
                }
                return list;
            }
        };
        task.setOnSucceeded(event -> {
            tableView.setItems(task.getValue());
        });
        new Thread(task).start();
    }
}
