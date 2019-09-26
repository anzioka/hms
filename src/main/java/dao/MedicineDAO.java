package main.java.dao;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import main.java.model.Medicine;
import main.java.model.MedicineLocation;
import main.java.util.DBUtil;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by alfonce on 20/05/2017.
 */
public class MedicineDAO {
    @FXML
    public static ObservableList<Medicine> getMedicineList() {
        ObservableList<Medicine> list = FXCollections.observableArrayList();
        String sql = "select * from drugs";
        try {
            ResultSet resultSet = DBUtil.executeQuery(sql);
            if (resultSet != null) {
                while (resultSet.next()) {
                    Medicine medicine = new Medicine();
                    medicine.setShopQuantity(resultSet.getInt("ShopQuantity"));
                    medicine.setStoreQuantity(resultSet.getInt("StoreQuantity"));
                    medicine.setDrugCode(resultSet.getInt("DrugCode"));
                    medicine.setReorderLevel(resultSet.getInt("ReorderLevel"));
                    medicine.setSellingPrice(resultSet.getDouble("SellingPrice"));
                    medicine.setName(resultSet.getString("Name"));
                    medicine.setBuyingPrice(resultSet.getDouble("BuyingPrice"));
                    list.add(medicine);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    public static int getQuantity(ObservableList<Medicine> medicines, int drugId, MedicineLocation medicineLocation) {
        for (Medicine medicine : medicines) {
            if (medicine.getDrugCode() == drugId) {
                if (medicineLocation == MedicineLocation.STORE) {
                    return medicine.getStoreQuantity();
                } else {
                    return medicine.getShopQuantity();
                }
            }
        }
        return 0;
    }
}