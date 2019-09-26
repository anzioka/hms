package main.java.controller;

import javafx.collections.ObservableList;
import main.java.dao.HospitalDAO;
import main.java.dao.PurchasesDao;
import main.java.dao.SupplierDAO;
import main.java.model.Hospital;
import main.java.model.PurchaseOrder;
import main.java.model.Supplier;
import main.java.util.CurrencyUtil;
import main.java.util.DateUtil;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.swing.JRViewer;

import javax.swing.*;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class PrintPurchaseOrder extends JFrame {
    void viewOrder(PurchaseOrder purchaseOrder) {
        //parameters
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("items", getPurchaseOrderItems(purchaseOrder));
        parameters.put("date", DateUtil.formatDateLong(purchaseOrder.getOrderDate()));
        parameters.put("order_num", purchaseOrder.getOrderId());
        Hospital hospital  = HospitalDAO.getHospital();
        parameters.put("hospital_name", hospital.getName());
        parameters.put("hospital_address", hospital.getAddress());
        parameters.put("hospital_phone", hospital.getPhoneNumber());
        Supplier supplier = SupplierDAO.getSupplier(purchaseOrder.getSupplierId());
        if (supplier != null) {
            parameters.put("supplier", supplier.getName());
            parameters.put("supplier_address", supplier.getAddress());
            parameters.put("supplier_phone", supplier.getPhoneNumber());
        }
        parameters.put("total", getOrderTotal((List<Map<String, String>>) parameters.get("items")));
        parameters.put("letter_head", HospitalDAO.getLetterHeadBufferedImage());
        showReport(parameters);
    }

    private void showReport(Map<String, Object> parameters) {
        try {
            InputStream inputStream = getClass().getClassLoader().getResourceAsStream("main/resources/view/purchase_order.jrxml");
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

    private String getOrderTotal(List<Map<String, String>> items) {
        double total = 0;
        for (Map<String, String> entry : items) {
            total += CurrencyUtil.parseCurrency(entry.get("total"));
        }
        return "Ksh. " + CurrencyUtil.formatCurrency(total);
    }

    private List<Map<String,String>> getPurchaseOrderItems(PurchaseOrder purchaseOrder) {
        List<Map<String, String>> data = new ArrayList<>();
        ObservableList<PurchaseOrder> purchaseOrders = PurchasesDao.getOrderByID(purchaseOrder.getOrderId());
        for (PurchaseOrder order : purchaseOrders) {
            Map<String, String> entry = new HashMap<>();
            entry.put("name", order.getDescription());
            entry.put("quantity", Integer.toString(order.getQuantity()));
            entry.put("buying_price", Double.toString(order.getUnitPrice()));
            entry.put("total", CurrencyUtil.formatCurrency(order.getQuantity() * order.getUnitPrice()));
            data.add(entry);
        }
        return data;
    }
}
