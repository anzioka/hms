package main.java.dao;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import main.java.util.DBUtil;

import java.sql.ResultSet;
import java.sql.SQLException;

public class AllergiesDao {
    public static ObservableList<String> getAllergies(String patientId) {
        ObservableList<String> list = FXCollections.observableArrayList();
        getAllergiesHelper(DBUtil.executeQuery("select name from allergies " +
                "where patient_id = '" + patientId + " '"), list);
        return list;
    }

    public static ObservableList<String> getAllAllergies() {
        ObservableList<String> list = FXCollections.observableArrayList();
        getAllergiesHelper(DBUtil.executeQuery("select distinct name from allergies"), list);
        return list;
    }

    private static void getAllergiesHelper(ResultSet resultSet, ObservableList<String> list) {
        if (resultSet != null) {
            try {
                while (resultSet.next()) {
                    list.add(resultSet.getString("name"));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
