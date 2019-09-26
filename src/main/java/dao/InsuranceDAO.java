package main.java.dao;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import main.java.model.Insurance;
import main.java.util.DBUtil;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by alfonce on 22/07/2017.
 */
public class InsuranceDAO {
    public static ObservableList<Insurance> getList(String sql) {
        ObservableList<Insurance> list = FXCollections.observableArrayList();
        try {
            ResultSet resultSet = DBUtil.executeQuery(sql);
            if (resultSet != null) {
                while (resultSet.next()) {
                    Insurance insurance = new Insurance();
                    insurance.setName(resultSet.getString("Name"));
                    insurance.setIsAssigned(false);
                    insurance.setGroup(resultSet.getString("InsuranceGroup"));
                    list.add(insurance);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public static Double getConsultationFee(String insurer) {
        String sql = "select insurance.name, ConsultationFee from insurance " +
                "inner join InsuranceGroups " +
                "on Insurance.InsuranceGroup = InsuranceGroups.Name " +
                "where Insurance.name = '" + insurer + "'";
        ResultSet resultSet = DBUtil.executeQuery(sql);
        try {
            if (resultSet != null && resultSet.next()) {
                return resultSet.getDouble("ConsultationFee");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
