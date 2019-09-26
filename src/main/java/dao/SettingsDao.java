package main.java.dao;

import main.java.model.Setting;
import main.java.util.DBUtil;

import java.sql.ResultSet;
import java.sql.SQLException;

public class SettingsDao {
    public static Setting getSettings() {
        Setting setting = new Setting();
        String sql = "select * from general_settings";
        ResultSet resultSet = DBUtil.executeQuery(sql);
        try {
            if (resultSet != null && resultSet.next()) {
                setting.setId(resultSet.getInt("setting_id"));
                setting.setConsultationFee(resultSet.getDouble("consultation_fee"));
                setting.setCorporateConsultationFee(resultSet.getDouble("corporate_consultation_fee"));
                setting.setLabPrepay(resultSet.getBoolean("lab_prepay"));
                setting.setPharmacyPrepay(resultSet.getBoolean("pharmacy_prepay"));
                setting.setNHIFRebate(resultSet.getDouble("nhif_rebate"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return setting;
    }
}
