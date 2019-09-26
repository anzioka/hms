package main.java.controller;

import javafx.collections.ObservableList;
import main.java.dao.HospitalDAO;
import main.java.model.Hospital;
import main.java.util.CurrencyUtil;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.swing.JRViewer;

import javax.swing.*;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static main.java.controller.BillingController.*;

class GroupInvoices extends JFrame {
    void showInvoices(ObservableList<Map<String, String>> items) {
        //parameters
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("insurer", items.get(0).get(INSURER));
        parameters.put("total", getTotal(items));
        List<Map<String, String>> dataList = new ArrayList<>();
        for (Map<String, String> item : items) {
            Map<String, String> data = new HashMap<>();
            data.put("patient", item.get(PATIENT_NAME));
            data.put("insurance_num", item.get(INSURANCE_ID));
            data.put("amount", item.get(OUTSTANDING_AMOUNT));
            dataList.add(data);
        }
        parameters.put("items", dataList);
        Hospital hospital = HospitalDAO.getHospital();
        parameters.put("hospital_name", hospital.getName());
        parameters.put("hospital_address", hospital.getAddress());
        parameters.put("hospital_phone", "Tel:" + hospital.getPhoneNumber());
        showWindow(parameters);
    }

    private void showWindow(Map<String, Object> parameters) {
        try {
            InputStream inputStream = getClass().getClassLoader().getResourceAsStream("main/resources/view/group-invoice.jrxml");
            JasperReport report = JasperCompileManager.compileReport(inputStream);
            JasperPrint print = JasperFillManager.fillReport(report, parameters, new JREmptyDataSource());
            JRViewer viewer = new JRViewer(print);
            viewer.setOpaque(true);
            viewer.setVisible(true);
            this.add(viewer);
            this.setSize(700, 800);
            this.setVisible(true);

        } catch (JRException e) {
            e.printStackTrace();
        }
    }

    private String getTotal(ObservableList<Map<String, String>> items) {
        double total = 0;
        for (Map<String, String> entry : items) {
            total += CurrencyUtil.parseCurrency(entry.get(OUTSTANDING_AMOUNT));
        }
        return CurrencyUtil.formatCurrency(total);
    }
}
