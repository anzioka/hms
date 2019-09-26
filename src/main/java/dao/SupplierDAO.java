package main.java.dao;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import main.java.model.Supplier;
import main.java.util.DBUtil;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by alfonce on 08/07/2017.
 */
public class SupplierDAO {
    public static int getSupplierID(String name) {
        int result = -1;
        String sql = "select supplierId from Suppliers where name= '" + name + "'";
        try {
            ResultSet resultSet = DBUtil.executeQuery(sql);
            if (resultSet != null) {
                while (resultSet.next()) {
                    result = resultSet.getInt("SupplierID");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static ObservableList<Supplier> getSuppliers() {
        ObservableList<Supplier> suppliers = FXCollections.observableArrayList();
        String sql = "select * from suppliers";
        ResultSet resultSet = DBUtil.executeQuery(sql);
        try {
            if (resultSet != null) {
                while (resultSet.next()) {
                    Supplier supplier = new Supplier(resultSet.getString("name"), resultSet.getString("address"),
                            resultSet.getString("phonenumber"), resultSet.getString("email"), resultSet.getString
                            ("contact"));
                    supplier.setSupplierId(resultSet.getInt("supplier_id"));
                    suppliers.add(supplier);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return suppliers;

    }

    public static Supplier getSupplier(int supplierId) {
        ResultSet resultSet = DBUtil.executeQuery("select * from suppliers where supplier_id = " + supplierId);
        if (resultSet != null) {
            try {
                if (resultSet.next()) {
                    return new Supplier(
                            resultSet.getString("name"),
                            resultSet.getString("address"),
                            resultSet.getString("PhoneNumber"),
                            resultSet.getString("email"),
                            resultSet.getString("contact"));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
