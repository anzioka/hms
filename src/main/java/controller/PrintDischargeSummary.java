package main.java.controller;

import javafx.concurrent.Task;
import main.java.dao.HospitalDAO;
import main.java.dao.PatientDAO;
import main.java.model.*;
import main.java.util.*;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.swing.JRViewer;

import javax.swing.*;
import java.io.InputStream;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class PrintDischargeSummary extends JFrame {
    private int admissionNumber;
    void showSummary(int admissionNumber, String inpatientNumber) {
        this.admissionNumber = admissionNumber;
        //parameters
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("inpatient_no", inpatientNumber);
        //procedures
        //doctors
        getHospitalInfo(parameters);

    }

    private void getHospitalInfo(Map<String, Object> parameters) {
        Task<Hospital> task = new Task<Hospital>() {
            @Override
            protected Hospital call() throws Exception {
                return HospitalDAO.getHospital();
            }
        };
        task.setOnSucceeded(event -> {
            parameters.put("hospital_name", task.getValue().getName());
            parameters.put("hospital_address", task.getValue().getAddress());
            parameters.put("hospital_phone", "Tel: " + task.getValue().getPhoneNumber());

            getPatientDetails(parameters);
        });

        new Thread(task).start();
    }

    private void getPatientDetails(Map<String, Object> parameters) {
        Task<Patient> task = new Task<Patient>() {
            @Override
            protected Patient call() throws Exception {
                String sql = "select patients.* from patients " +
                        "inner join inpatients on inpatients.patient_id = patients.PatientID " +
                        "where admission_num = " + admissionNumber;
                return PatientDAO.getPatient(sql);
            }
        };
        task.setOnSucceeded(event -> {
            Patient patient = task.getValue();
            if (patient != null) {
                parameters.put("patient_name", patient.getFirstName() + " " + patient.getLastName());
                parameters.put("date_of_birth", DateUtil.formatDateLong(patient.getDateOfBirth()));
                parameters.put("age", AgeUtil.getAge(patient.getDateOfBirth()));
                parameters.put("sex", patient.getSexuality());
            }
            getAdmissionDetails(parameters);
        });
        new Thread(task).start();
    }

    private void getAdmissionDetails(Map<String, Object> parameters) {
        Task task = new Task() {
            @Override
            protected Object call() throws Exception {
                String sql = "select users.LastName, date_admitted, time_admitted, date_discharged, time_discharged, " +
                        "bed_id, wards.ward_name " +
                        "from inpatients " +
                        "inner join wards on wards.ward_id = inpatients.ward_id " +
                        "inner join users on users.Id = inpatients.doctor_id " +
                        "where admission_num = " + admissionNumber;
                ResultSet resultSet = DBUtil.executeQuery(sql);
                if (resultSet != null && resultSet.next()) {
                    LocalDateTime dateAdmitted = LocalDateTime.of(resultSet.getObject("date_admitted", LocalDate
                            .class),resultSet.getObject("time_admitted", LocalTime.class));
                    parameters.put("date_admitted", DateUtil.formatDateTime(dateAdmitted));
                    if (resultSet.getObject("date_discharged") != null) {
                        LocalDateTime dateDischarged = LocalDateTime.of(resultSet.getObject("date_discharged",
                                LocalDate.class),resultSet.getObject("time_discharged", LocalTime.class));
                        parameters.put("date_discharged", DateUtil.formatDateTime(dateDischarged));
                    } else {
                        parameters.put("date_discharged", "-");
                    }
                    parameters.put("ward", resultSet.getString("wards.ward_name"));
                    parameters.put("bed",resultSet.getString("bed_id"));
                    parameters.put("admitted_by", "Dr. " + resultSet.getString("lastName"));
                }

                return null;
            }
        };
        task.setOnSucceeded(event -> {
            getClinicalSummary(parameters);
        });

        new Thread(task).start();
    }

    private void getClinicalSummary(Map<String, Object> parameters) {
        Task task = new Task() {
            @Override
            protected Object call() throws Exception {
                ResultSet resultSet = DBUtil.executeQuery("select summary from clinical_summary where admission_num = " + admissionNumber);
                if (resultSet != null && resultSet.next()) {
                    parameters.put("summary", resultSet.getString("summary"));
                }
                return null;
            }
        };
        task.setOnSucceeded(event -> {
            getMedication(parameters);
        });
        new Thread(task).start();
    }

    private void getMedication(Map<String, Object> parameters) {
        Task<List<String>> task = new Task<List<String>>() {
            @Override
            protected List<String> call() throws Exception {
                ResultSet resultSet = DBUtil.executeQuery("select distinct drugs.name from prescriptions " +
                        "inner join drugs on drugs.DrugCode = prescriptions.drug_code " +
                        "where admission_num = " + admissionNumber);
                List<String> list = new ArrayList<>();
                if (resultSet != null) {
                    while (resultSet.next()) {
                        list.add(resultSet.getString("name"));
                    }
                }
                return list;
            }
        };
        task.setOnSucceeded(event -> {
            parameters.put("medication", getNumberedListString(task.getValue()));
            getDiagnosis(parameters);
        });
        new Thread(task).start();

    }

    private void getDiagnosis(Map<String, Object> parameters) {
        Task<List<String>> task = new Task<List<String>>() {
            @Override
            protected List<String> call() throws Exception {
                ResultSet resultSet = DBUtil.executeQuery("select distinct name from diagnosis " +
                        "inner join icd10_diagnoses on icd10_diagnoses.code = diagnosis.code " +
                        "where admission_num = " + admissionNumber);
                List<String> list = new ArrayList<>();
                if (resultSet != null) {
                    while (resultSet.next()) {
                        list.add(resultSet.getString("name"));
                    }
                }
                return list;
            }
        };
        task.setOnSucceeded(event -> {
            parameters.put("diagnosis", getNumberedListString(task.getValue()));
            getProcedures(parameters);
        });
        new Thread(task).start();
    }

    private void getProcedures(Map<String, Object> parameters) {
        Task<List<String>> task = new Task<List<String>>() {
            @Override
            protected List<String> call() throws Exception {
                ResultSet resultSet = DBUtil.executeQuery("select distinct procedure_name from patient_procedures " +
                        "where admission_num = " + admissionNumber);
                List<String> list = new ArrayList<>();
                if (resultSet != null) {
                    while (resultSet.next()) {
                        list.add(resultSet.getString("procedure_name"));
                    }
                }
                return list;
            }
        };
        task.setOnSucceeded(event -> {
            parameters.put("procedures", getNumberedListString(task.getValue()));
            getDoctors(parameters);
        });
        new Thread(task).start();
    }

    private void getDoctors(Map<String, Object> parameters) {
        Task<List<String>> task = new Task<List<String>>() {
            @Override
            protected List<String> call() throws Exception {
                String sql = "select distinct LastName from users " +
                        "inner join inpatient_visits on inpatient_visits.user_id = users.Id " +
                        "where admission_num = " + admissionNumber + " " +
                        "and category = '" + InpatientVisit.Category.DOCTOR + "'";
                ResultSet resultSet = DBUtil.executeQuery(sql);
                List<String> list = new ArrayList<>();
                if (resultSet != null) {
                    while (resultSet.next()) {
                        list.add("Dr. " + resultSet.getString("LastName"));
                    }
                }
                return list;
            }
        };
        task.setOnSucceeded(event -> {
            parameters.put("doctors", getNumberedListString(task.getValue()));
            viewReport(parameters);
        });
        new Thread(task).start();
    }

    private String getNumberedListString(List<String> strings) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < strings.size(); i++) {
            stringBuilder.append(i + 1).append(") ");
            stringBuilder.append(strings.get(i)).append(" ").append(" ");
        }
        return stringBuilder.toString();
    }

    private void viewReport(Map<String, Object> parameters) {
        try {
            InputStream inputStream = getClass().getClassLoader().getResourceAsStream
                    ("main/resources/view/discharge_summary.jrxml");
            JasperReport report = JasperCompileManager.compileReport(inputStream);
            JasperPrint print = JasperFillManager.fillReport(report, parameters, new JREmptyDataSource());

            JRViewer viewer = new JRViewer(print);
            viewer.setOpaque(true);
            viewer.setVisible(true);
            this.add(viewer);
            this.setSize(800, 800);
            this.setVisible(true);
        } catch (JRException e) {
            e.printStackTrace();
        }
    }
}
