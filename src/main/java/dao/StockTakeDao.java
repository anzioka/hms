package main.java.dao;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import main.java.model.MedicineLocation;
import main.java.model.StockTake;
import main.java.util.DBUtil;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

public class StockTakeDao {
    public static int getNextStockTakeId() {
        String sql = "select stock_take_id from stock_take order by stock_take_id desc limit 1";
        ResultSet resultSet = DBUtil.executeQuery(sql);
        try {
            if (resultSet != null && resultSet.next()) {
                return resultSet.getInt("stock_take_id") + 1;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 1001;
    }

    public static ObservableList<StockTake> getStockTakes(LocalDate start, LocalDate end) {
        ObservableList<StockTake> list = FXCollections.observableArrayList();
        String sql = "select stock_take_id , location, stock_take.date_created, sum(actual_quantity - " +
                "recorded_quantity) as qty, sum(value_change) as value_change, location, users.FirstName, Users.LastName " +
                "from stock_take " +
                "inner join users on users.Id = stock_take.user_id " +
                "where stock_take.date_created between '" + start + "' and '" + end + " '" +
                "group by stock_take_id, location, date_created, location, FirstName, LastName";

        ResultSet resultSet = DBUtil.executeQuery(sql);
        if (resultSet != null) {
            try {
                while (resultSet.next()) {
                    StockTake stockTake = new StockTake();
                    stockTake.setMedicineLocation(MedicineLocation.valueOf(resultSet.getString("location")));
                    stockTake.setQtyChange(resultSet.getInt("qty"));
                    stockTake.setValueChange(resultSet.getDouble("value_change"));
                    stockTake.setUserName(resultSet.getString("FirstName") + " " + resultSet.getString("LastName"));
                    stockTake.setDateCreated(resultSet.getObject("date_created", LocalDate.class));
                    stockTake.setStockTakeId(resultSet.getInt("stock_take_id"));
                    list.add(stockTake);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return list;
    }

    public static ObservableList<StockTake> getStockTakesById(int stockTakeId) {
        ObservableList<StockTake> list = FXCollections.observableArrayList();
        String sql = "select actual_quantity, recorded_quantity, value_change, stock_take.drug_id, " +
                "drugs.name " +
                "from stock_take " +
                "inner join drugs on drugs.drugCode = stock_take.drug_id " +
                "where stock_take_id = " + stockTakeId;
        ResultSet resultSet = DBUtil.executeQuery(sql);
        if (resultSet != null) {
            try {
                while (resultSet.next()) {
                    StockTake stockTake = new StockTake();
                    stockTake.setQtyOnHand(resultSet.getInt("recorded_quantity"));
                    stockTake.setCountedQty(resultSet.getInt("actual_quantity"));
                    stockTake.setValueChange(resultSet.getDouble("value_change"));
                    stockTake.setMedicineName(resultSet.getString("name"));
                    stockTake.setDrugId(resultSet.getInt("drug_id"));
                    stockTake.setQtyChange(stockTake.getCountedQty() - stockTake.getQtyOnHand());
                    list.add(stockTake);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return list;
    }
}
