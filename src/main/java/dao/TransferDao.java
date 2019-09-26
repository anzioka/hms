package main.java.dao;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import main.java.model.MedicineLocation;
import main.java.model.StockTransfer;
import main.java.util.DBUtil;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

public class TransferDao {
    public static int getNextTransferId() {
        String sql = "select transfer_id from stock_transfer order by transfer_id desc limit 1";
        ResultSet resultSet = DBUtil.executeQuery(sql);
        try {
            if (resultSet != null && resultSet.next()) {
                return resultSet.getInt("transfer_id") + 1;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 1001;
    }

    public static ObservableList<StockTransfer> getTransfers(LocalDate start, LocalDate end) {
        ObservableList<StockTransfer> list = FXCollections.observableArrayList();
        String sql = "select transfer_id, sum(quantity) as quantity, origin, stock_transfer.date_created, " +
                "users.FirstName, users.lastName " +
                "from stock_transfer " +
                "inner join users on users.ID = stock_transfer.user_id " +
                " where stock_transfer.date_created between '" + start + "' and '" + end + "' " +
                "group by transfer_id, FirstName, LastName, origin, date_created";
        ResultSet resultSet = DBUtil.executeQuery(sql);
        try {
            if (resultSet != null) {
                while (resultSet.next()) {
                    StockTransfer stockTransfer = new StockTransfer();
                    stockTransfer.setOrigin(MedicineLocation.valueOf(resultSet.getString("origin")));
                    stockTransfer.setTransferredBy(resultSet.getString("FirstName") + " " + resultSet.getString("LastName"));
                    stockTransfer.setTransferNo(resultSet.getInt("transfer_id"));
                    stockTransfer.setDateCreated(resultSet.getObject("date_created", LocalDate.class));
                    stockTransfer.setQuantity(resultSet.getInt("quantity"));
                    list.add(stockTransfer);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public static ObservableList<StockTransfer> getStockTransfer(int transferNo) {
        ObservableList<StockTransfer> list = FXCollections.observableArrayList();
        String sql = "select stock_transfer.drug_id, quantity, drugs.name " +
                "from stock_transfer " +
                "inner join drugs on drugs.DrugCode = stock_transfer.drug_id " +
                "where transfer_id = " + transferNo;
        ResultSet resultSet = DBUtil.executeQuery(sql);
        if (resultSet != null) {
            try {
                while (resultSet.next()) {
                    StockTransfer transfer = new StockTransfer();
                    transfer.setDrugId(resultSet.getInt("drug_id"));
                    transfer.setQuantity(resultSet.getInt("quantity"));
                    transfer.setDrugName(resultSet.getString("name"));
                    list.add(transfer);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return list;

    }
}
