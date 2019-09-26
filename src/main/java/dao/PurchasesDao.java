package main.java.dao;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import main.java.model.MedicineLocation;
import main.java.model.OrderStatus;
import main.java.model.Purchase;
import main.java.model.PurchaseOrder;
import main.java.util.DBUtil;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

public class PurchasesDao {
    public static int getNextPurchaseId() {
        String sql = "select purchase_id from purchases order by purchase_id desc limit 1";
        ResultSet resultSet = DBUtil.executeQuery(sql);
        try {
            if (resultSet != null && resultSet.next()) {
                return resultSet.getInt("purchase_id") + 1;

            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 1;
    }

    public static int getNextPurchaseReturnId() {
        String sql = "select id from purchase_returns order by id desc limit 1";
        ResultSet resultSet = DBUtil.executeQuery(sql);
        try {
            if (resultSet != null && resultSet.next()) {
                return resultSet.getInt("id") + 1;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 1;
    }

    public static ObservableList<Purchase> getPurchases() {
        ObservableList<Purchase> list = FXCollections.observableArrayList();
        String sql = "select purchases.invoice, purchases.purchase_id, purchases.date_created, users.firstname, " +
                "suppliers.name, " +
                "purchases" +
                ".destination,\n" +
                "  sum(purchases.quantity * (100 - purchases.discount) * purchases.unit_price / 100) as total\n" +
                "from  (((purchases\n" +
                "  inner join users on users.user_id = purchases.user_id)\n" +
                "  inner join suppliers on suppliers.supplier_id = purchases.supplier_id)\n" +
                "  inner join drugs on drugs.drug_id = purchases.drug_id)\n" +
                "group by purchase_id, purchases.invoice, date_created, firstname, suppliers.name, purchases" +
                ".destination";
        ResultSet resultSet = DBUtil.executeQuery(sql);
        if (resultSet != null) {
            try {
                while (resultSet.next()) {
                    Purchase purchase = new Purchase();
                    purchase.setPurchaseId(resultSet.getInt("purchase_id"));
                    purchase.setSupplier(resultSet.getString("name"));
                    purchase.setReceivedBy(resultSet.getString("firstname"));
                    purchase.setInvoiceNumber(resultSet.getString("invoice"));
                    purchase.setDateDelivered(resultSet.getObject("date_created", LocalDate.class));
                    purchase.setLocation(MedicineLocation.valueOf(resultSet.getString("destination")));
                    purchase.setTotalPrice(resultSet.getDouble("total"));
                    list.add(purchase);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return list;
    }

    public static ObservableList<PurchaseOrder> getPurchaseOrders() {
        ObservableList<PurchaseOrder> list = FXCollections.observableArrayList();
        String sql = "select orders.order_id, orders.supplier_id, orders.date_created, orders.date_delivered, orders" +
                ".order_status, s" +
                ".name, sum(orders\n" +
                ".quantity * orders.unit_price) as total \n" +
                "from orders \n" +
                "  inner join suppliers s on orders.supplier_id = s.supplier_id\n" +
                "group by order_id, date_delivered, date_created, order_status, name, supplier_id";

        ResultSet resultSet = DBUtil.executeQuery(sql);
        if (resultSet != null) {
            try {
                while (resultSet.next()) {
                    PurchaseOrder purchaseOrder = new PurchaseOrder();
                    purchaseOrder.setSupplierId(resultSet.getInt("supplier_id"));
                    purchaseOrder.setOrderId(resultSet.getInt("order_id"));
                    purchaseOrder.setOrderDate(resultSet.getObject("date_created", LocalDate.class));
                    if (resultSet.getObject("date_delivered") != null) {
                        purchaseOrder.setDateReceived(resultSet.getObject("date_delivered", LocalDate.class));
                    } else {
                        purchaseOrder.setDateReceived(null);
                    }
                    purchaseOrder.setTotalPrice(resultSet.getDouble("total"));
                    purchaseOrder.setOrderStatus(OrderStatus.valueOf(resultSet.getString
                            ("order_status")));
                    purchaseOrder.setSupplierName(resultSet.getString("name"));
                    list.add(purchaseOrder);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return list;
    }

    public static int getNextPurchaseOrderId() {
        String sql = "select order_id from orders order by order_id desc limit 1";
        ResultSet resultSet = DBUtil.executeQuery(sql);
        try {
            if (resultSet != null && resultSet.next()) {
                return resultSet.getInt("order_id") + 1;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 1001;
    }

    public static ObservableList<PurchaseOrder> getOrderByID(int orderId) {
        ObservableList<PurchaseOrder> list = FXCollections.observableArrayList();
        String sql = "select quantity, orders.drug_id, unit_price, drugs.name " +
                "from orders " +
                " inner join " +
                " drugs on drugs.DrugCode = orders.drug_id " +
                " where order_id = " + orderId;
        ResultSet resultSet = DBUtil.executeQuery(sql);
        if (resultSet != null) {
            try {
                while (resultSet.next()) {
                    PurchaseOrder purchaseOrder = new PurchaseOrder();
                    purchaseOrder.setQuantity(resultSet.getInt("quantity"));
                    purchaseOrder.setDrugId(resultSet.getInt("drug_id"));
                    purchaseOrder.setDescription(resultSet.getString("name"));
                    purchaseOrder.setUnitPrice(resultSet.getDouble("unit_price"));
                    list.add(purchaseOrder);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return list;
    }

    public static ObservableList<Purchase> getPurchasesById(int purchaseId) {
        ObservableList<Purchase> list = FXCollections.observableArrayList();
        String sql = "select purchases.drug_id, batch_number, quantity, unit_price, discount, expiry_date, drugs" +
                ".name " +
                "from purchases " +
                "inner join drugs on drugs.DrugCode = purchases.drug_id " +
                "where purchase_id = " + purchaseId;
        ResultSet resultSet = DBUtil.executeQuery(sql);
        if (resultSet != null) {
            try {
                while (resultSet.next()) {
                    Purchase purchase = new Purchase();
                    purchase.setDrugName(resultSet.getString("name"));
                    purchase.setDrugId(resultSet.getInt("drug_id"));
                    purchase.setBatchNo(resultSet.getString("batch_number"));
                    if (resultSet.getObject("expiry_date") != null) {
                        purchase.setExpiryDate(resultSet.getObject("expiry_date", LocalDate.class));
                    }
                    purchase.setQuantity(resultSet.getInt("quantity"));
                    purchase.setUnitPrice(resultSet.getDouble("unit_price"));
                    purchase.setDiscount(resultSet.getDouble("discount"));
                    list.add(purchase);
                }

            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return list;
    }

    public static ObservableList<Purchase> getPurchasesByInvoice(String invoiceNumber) {
        ObservableList<Purchase> list = FXCollections.observableArrayList();
        String sql = "select purchases.drug_id, purchases.supplier_id, drugs.name as drug_name, quantity, discount, unit_price, purchases" +
                ".date_created, batch_number," +
                " suppliers.name as supplier " +
                "from ((purchases inner join suppliers on purchases.supplier_id  = suppliers.supplier_id) " +
                "inner join drugs on drugs.drugCode = purchases.drug_id) " +
                "where invoice = '" + invoiceNumber + "'";
        ResultSet resultSet = DBUtil.executeQuery(sql);
        if (resultSet != null) {
            try {
                while (resultSet.next()) {
                    Purchase purchase = new Purchase();
                    purchase.setDrugId(resultSet.getInt("drug_id"));
                    purchase.setQuantity(resultSet.getInt("quantity"));
                    purchase.setDiscount(resultSet.getDouble("discount"));
                    purchase.setUnitPrice(resultSet.getDouble("unit_price"));
                    purchase.setDateDelivered(resultSet.getObject("date_created", LocalDate.class));
                    purchase.setBatchNo(resultSet.getString("batch_number"));
                    purchase.setSupplier(resultSet.getString("supplier"));
                    purchase.setDrugName(resultSet.getString("drug_name"));
                    purchase.setSupplierId(resultSet.getInt("supplier_id"));
                    list.add(purchase);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return list;
    }

    public static ObservableList<Purchase> getPurchasesByDate(LocalDate start, LocalDate end) throws SQLException {
        ObservableList<Purchase> purchases = FXCollections.observableArrayList();
        String sql = "select purchase_id, sum((100 - discount) / 100 * quantity * unit_price) as total, " +
                "date_created, invoice, suppliers.name, users.FirstName, users.LastName " +
                "from purchases " +
                "inner join suppliers on suppliers.supplier_id = purchases.supplier_id " +
                "inner join users on users.Id = purchases.user_id " +
                "where date_created between '" + start + "' and '" + end + "' " +
                "group by date_created, invoice, name, firstName, LastName, purchase_id " +
                "order by date_created desc";
        ResultSet resultSet = DBUtil.executeQuery(sql);
        if (resultSet != null) {
            while (resultSet.next()) {
                Purchase purchase = new Purchase();
                purchase.setTotalPrice(resultSet.getDouble("total"));
                purchase.setPurchaseId(resultSet.getInt("purchase_id"));
                purchase.setSupplier(resultSet.getString("name"));
                purchase.setReceivedBy(resultSet.getString("FirstName") + " " + resultSet.getString("LastName"));
                purchase.setDateDelivered(resultSet.getObject("date_created", LocalDate.class));
                purchase.setInvoiceNumber(resultSet.getString("invoice"));
                purchases.add(purchase);
            }
        }
        return purchases;
    }

}