package main.java.controller;

import javafx.concurrent.Task;
import main.java.dao.HospitalDAO;
import main.java.model.Hospital;
import main.java.util.CurrencyUtil;
import main.java.util.DBUtil;
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

import static main.java.controller.BillingController.*;
class PrintReceipt extends JFrame {
    void viewReceipt(Map<String, String> data) {
        Task<List<Map<String, String>>> task = new Task<List<Map<String, String>>>() {
            @Override
            protected List<Map<String, String>> call() throws Exception {
                return getReceiptItems(data);
            }
        };
        task.setOnSucceeded(event -> {
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("items", task.getValue());
            parameters.put("user", data.get(SERVER));
            parameters.put("total", data.get(AMOUNT_PAID));
            parameters.put("receipt_num", "Receipt No. " + data.get(BILL_NUM));
            parameters.put("date", data.get(DATE));

            Hospital hospital = HospitalDAO.getHospital();
            parameters.put("hospital_name", hospital.getName());
            parameters.put("hospital_address", hospital.getAddress());
            parameters.put("hospital_phone", hospital.getPhoneNumber());
            showReceipt(parameters);
        });
        new Thread(task).start();
    }

    private void showReceipt(Map<String, Object> parameters) {
        try {
            InputStream inputStream = getClass().getClassLoader().getResourceAsStream("main/resources/view/receipt.jrxml");
            JasperReport report = JasperCompileManager.compileReport(inputStream);
            JasperPrint print = JasperFillManager.fillReport(report, parameters, new JREmptyDataSource());

            JRViewer viewer = new JRViewer(print);
            viewer.setOpaque(true);
            viewer.setVisible(true);
            this.add(viewer);
            this.setSize(400, 580);
            this.setVisible(true);
        } catch (JRException e) {
            e.printStackTrace();
        }
    }

    private List<Map<String, String>> getReceiptItems(Map<String, String> data) throws SQLException {
        List<Map<String, String>> items = new ArrayList<>();
        ResultSet resultSet = DBUtil.executeQuery("select amount, description from payments where bill_number = " + data.get(BILL_NUM));
        if (resultSet != null) {
            while (resultSet.next()) {
                Map<String, String> item = new HashMap<>();
                item.put("amount", CurrencyUtil.formatCurrency(resultSet.getDouble("amount")));
                item.put("description", resultSet.getString("description"));
                items.add(item);
            }
        }
        return items;
    }

}
