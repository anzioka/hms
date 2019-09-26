package main.java.dao;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import main.java.model.*;
import main.java.util.*;

import java.sql.ResultSet;
import java.sql.SQLException;

public class RadiologyDao {
    public static ObservableList<RadiologyItem> getAllRadiologyItems() {
        ObservableList<RadiologyItem> items = FXCollections.observableArrayList();
        ResultSet resultSet = DBUtil.executeQuery("select * from radiology_items");
        if (resultSet != null) {
            try {
                while (resultSet.next()) {
                    RadiologyItem radiologyItem = new RadiologyItem();
                    radiologyItem.setId(resultSet.getInt("id"));
                    radiologyItem.setCategory(resultSet.getString("category"));
                    radiologyItem.setDescription(resultSet.getString("description"));
                    radiologyItem.setCost(resultSet.getDouble("cost"));
                    items.add(radiologyItem);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return items;
    }
}
