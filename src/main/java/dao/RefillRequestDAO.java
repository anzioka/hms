package main.java.dao;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import main.java.model.RefillRequest;
import main.java.util.DBUtil;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

/**
 * Created by alfonce on 07/07/2017.
 */
public class RefillRequestDAO {
    public static ObservableList<RefillRequest> getRefillRequests(String sql) {
        ObservableList<RefillRequest> list = FXCollections.observableArrayList();
        ResultSet resultSet = DBUtil.executeQuery(sql);
        if (resultSet != null) {
            getRequestsFromResultSet(resultSet, list);

        }
        return list;
    }

    private static void getRequestsFromResultSet(ResultSet resultSet, ObservableList<RefillRequest> list) {
        try {

            while (resultSet.next()) {
                RefillRequest refillRequest = new RefillRequest();
                refillRequest.setRefillId(resultSet.getInt("ID"));
                refillRequest.setRefillerId(resultSet.getInt("RefillerID"));
                refillRequest.setMedicineName(resultSet.getString("Name"));
                refillRequest.setDateRequested(resultSet.getObject("DateCreated", LocalDate.class));

                if (resultSet.getObject(6) != null) {
                    refillRequest.setDateServiced(resultSet.getObject("DateServiced", LocalDate.class));
                }

                refillRequest.setAmountRequested(resultSet.getInt("AmountRequested"));
                refillRequest.setAmountReceived(resultSet.getInt("AmountReceived"));
                refillRequest.setStatus(resultSet.getString("Status"));

                list.add(refillRequest);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
