package main.java.controller;

import javafx.collections.ObservableList;
import main.Main;
import main.java.dao.BillingDao;
import main.java.dao.HospitalDAO;
import main.java.dao.PatientDAO;
import main.java.model.*;
import main.java.util.CurrencyUtil;
import main.java.util.DBUtil;
import main.java.util.DateUtil;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.swing.JRViewer;

import javax.swing.*;
import java.io.InputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import static main.java.controller.BillingController.*;

class PrintInvoice extends JFrame {
    void showInvoice(Map<String, String> data) {
        Map<String, Object> parameters = getParameters(data);
        try {
            InputStream inputStream = getClass().getClassLoader().getResourceAsStream("main/resources/view/invoice.jrxml");
            JasperReport report = JasperCompileManager.compileReport(inputStream);
            JasperPrint print = JasperFillManager.fillReport(report, parameters, new JREmptyDataSource());
            JRViewer viewer = new JRViewer(print);
            viewer.setOpaque(true);
            viewer.setVisible(true);
            this.add(viewer);
            this.setSize(800, 800);
            this.setVisible(true);

        } catch (JRException e) {
            Main.LOGGER.logp(Level.SEVERE, PrintInvoice.class.getName(), "", "Error", e);
        }
    }

    private Map<String, Object> getParameters(Map<String, String> data) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("owed", data.get(OUTSTANDING_AMOUNT));
        parameters.put("insurance_num", data.get(INSURANCE_ID));
        parameters.put("insurer", data.get(INSURER));
        parameters.put("patient", data.get(PATIENT_NAME));
        parameters.put("patient_num", data.get(PATIENT_NUM));
        List<Invoice> invoices = getInvoices(data);
        parameters.put("items", invoices);
        parameters.put("invoice_num", data.get(BILL_NUM));
        parameters.put("copay", data.get(AMOUNT_PAID));
        parameters.put("date", data.get(DATE));
        parameters.put("rebate", data.get(REBATE));
        double total = CurrencyUtil.parseCurrency(data.get(OUTSTANDING_AMOUNT)) + CurrencyUtil.parseCurrency(data.get(REBATE));
        parameters.put("total", CurrencyUtil.formatCurrency(total));
        Hospital hospital = HospitalDAO.getHospital();
        parameters.put("hospital_name", hospital.getName());
        parameters.put("hospital_address", hospital.getAddress());
        parameters.put("hospital_phone", "Tel:" + hospital.getPhoneNumber());

        //get patient details
        Patient patient;
        if (data.get(CATEGORY).equals(PatientCategory.OUTPATIENT.toString())) {
            patient = PatientDAO.getPatient("select * from patients where PatientId = '" + data.get(PATIENT_NUM) + "'");
        } else{
            patient = PatientDAO.getPatient("select patients.* from patients inner join inpatients on inpatients.patient_id = patients.PatientId " +
                    "where inpatient_num = '" + data.get(PATIENT_NUM) + "'");
        }
        if (patient != null) {
            parameters.put("sex", patient.getSexuality());
            parameters.put("age", patient.getPatientAge());
            parameters.put("date_of_birth", DateUtil.formatDateLong(patient.getDateOfBirth()));
        };
        return parameters;
    }

    private List<Invoice> getInvoices(Map<String, String> data) {
        ObservableList<Bill> bills = BillingDao.getPatientBill(data.get(BILL_NUM), data.get(CATEGORY).equals(PatientCategory.INPATIENT.toString()));

        List<Invoice> invoices = new ArrayList<>();
        for (Bill bill : bills) {
            Invoice invoice = new Invoice(bill.getDescription(), CurrencyUtil.formatCurrency(bill.getAmount()));
            invoices.add(invoice);
        }
        return invoices;
    }

}
