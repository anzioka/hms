package main.java.dao;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import main.java.model.CashSale;
import main.java.util.DBUtil;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

/**
 * Created by alfonce on 08/05/2017.
 */
public class CashSaleDAO {

    public static String getNextInvoiceNumber() {
        return null;
    }

    public static ObservableList<CashSale> getAllInvoices() {
        //TODO
        return null;
    }

    public static CashSale getBillFromQueueId(int id) {
        String sql = "select * from CashSales where queueid=" + id;
        ResultSet resultSet = DBUtil.executeQuery(sql);
        return getBillFromResultSet(resultSet);
    }

    private static CashSale getBillFromResultSet(ResultSet resultSet) {
        CashSale cashSale = new CashSale();
        try {
            if (resultSet.next()) {
                cashSale.setDateCreated(resultSet.getObject("DateCreated", LocalDate.class));
                cashSale.setId(resultSet.getInt("ID"));
                cashSale.setStatus(resultSet.getString("Status"));
                cashSale.setBillNumber(resultSet.getInt("ReceiptNumber"));
                cashSale.setCategory(resultSet.getString("Category"));
                cashSale.setDescription(resultSet.getString("Description"));
                cashSale.setAmount(resultSet.getDouble("Amount"));
                cashSale.setQueueId(resultSet.getInt("QueueId"));
                return cashSale;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
        return null;
    }

    public static ObservableList<CashSale> getBillByQueueId(int queueId) {
        ObservableList<CashSale> cashSales = FXCollections.observableArrayList();
        String sql = "select *  from CashSales where queueId=" + queueId;
        ResultSet resultSet = DBUtil.executeQuery(sql);
        if (resultSet != null) {
            getBillsFromResultSet(resultSet, cashSales);

        }
        return cashSales;
    }

    private static void getBillsFromResultSet(ResultSet resultSet, ObservableList<CashSale> cashSales) {
        try {
            while (resultSet.next()) {
                CashSale cashSale = new CashSale();
                cashSale.setDateCreated(resultSet.getObject("DateCreated", LocalDate.class));
                cashSale.setId(resultSet.getInt("ID"));
                cashSale.setBillNumber(resultSet.getInt("ReceiptNumber"));
                cashSale.setStatus(resultSet.getString("Status"));
                cashSale.setDescription(resultSet.getString("Description"));
                cashSale.setAmount(resultSet.getDouble("Amount"));
                cashSale.setQueueId(resultSet.getInt("QueueId"));
                cashSales.add(cashSale);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static ObservableList<CashSale> getPayments(String sql) {
        ObservableList<CashSale> list = FXCollections.observableArrayList();
        ResultSet resultSet = DBUtil.executeQuery(sql);
        getPaymentsFromResultSet(resultSet, list);
        return list;
    }

    private static void getPaymentsFromResultSet(ResultSet resultSet, ObservableList<CashSale> list) {
        try {
            if (resultSet != null) {
                while (resultSet.next()) {
                    CashSale sale = new CashSale();
                    sale.setQueueId(resultSet.getInt("VisitId"));
                    sale.setAmount(resultSet.getDouble("sum(amount)"));
                    sale.setBillNumber(resultSet.getInt("ReceiptNumber"));
                    sale.setDateCreated(resultSet.getObject("DateCreated", LocalDate.class));
                    list.add(sale);
                }

            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static ObservableList<CashSale> getCashSale(String getSaleSql) {
        ObservableList<CashSale> list = FXCollections.observableArrayList();
        try {
            ResultSet resultSet = DBUtil.executeQuery(getSaleSql);
            if (resultSet != null) {
                while (resultSet.next()) {
                    CashSale sale = new CashSale();
                    sale.setId(resultSet.getInt("ID"));
                    sale.setDescription(resultSet.getString("Description"));
                    sale.setAmount(resultSet.getDouble("Amount"));
                    sale.setQuantity(resultSet.getInt("Quantity"));
                    list.add(sale);
                }

            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}

