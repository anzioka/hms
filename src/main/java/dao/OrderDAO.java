package main.java.dao;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import main.java.model.OrderStatus;
import main.java.model.PurchaseOrder;
import main.java.util.DBUtil;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

/**
 * Created by alfonce on 08/07/2017.
 */
public class OrderDAO {
//    public static ObservableList<PurchaseOrder> getOrders(String sql) {
//        ObservableList<PurchaseOrder> list = FXCollections.observableArrayList();
//        try {
//            ResultSet resultSet = DBUtil.executeQuery(sql);
//        } catch (SQLException e) {
//
//        }
//    }

    public static int getNextOrderNumber() {
        String sql = "select * from Orders order by OrderID desc limit 1";
        try {
            ResultSet resultSet = DBUtil.executeQuery(sql);
            if (resultSet != null) {
                if (resultSet.next()) {
                    return resultSet.getInt("OrderID") + 1;
                }
                return 1;
            } else {
                return 1;
            }
        } catch (SQLException e) {
            return 1;
        }
    }

    public static int getNextId() {
        int resultId = 1;
        String sql = "select * from Orders order by ID desc limit 1";
        try {
            ResultSet resultSet = DBUtil.executeQuery(sql);
            if (resultSet != null) {
                if (resultSet.next()) {
                    resultId = resultSet.getInt("ID") + 1;
                }
            }
        } catch (SQLException e) {
            return 1;
        }
        return resultId;
    }

    public static ObservableList<PurchaseOrder> getOrders(LocalDate start, LocalDate end) {
        ObservableList<PurchaseOrder> orders = FXCollections.observableArrayList();
        String sql = "SELECT distinct order_id, orders.supplier_id, Orders.date_created, Orders.date_delivered, " +
                "order_status, Suppliers.Name from " +
                "Orders " +
                "INNER JOIN Suppliers ON orders.supplier_id = suppliers.supplier_id " +
                "where date_created between '" + start + "' and '" + end + "' " +
                "order by date_created desc";
        try {
            ResultSet resultSet = DBUtil.executeQuery(sql);
            if (resultSet != null) {
                while (resultSet.next()) {
                    PurchaseOrder purchaseOrder = new PurchaseOrder();
                    purchaseOrder.setOrderId(resultSet.getInt("order_id"));
                    purchaseOrder.setOrderDate(resultSet.getObject("date_created", LocalDate.class));
                    if (resultSet.getObject("date_delivered") != null) {
                        purchaseOrder.setDateReceived(resultSet.getObject("date_delivered", LocalDate.class));
                    }
                    purchaseOrder.setOrderStatus(OrderStatus.valueOf(resultSet.getString("order_status")));
                    purchaseOrder.setSupplierName(resultSet.getString("name"));
                    purchaseOrder.setSupplierId(resultSet.getInt("supplier_id"));
                    orders.add(purchaseOrder);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return orders;
    }
}
