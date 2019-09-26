package main.java.dao;

import main.java.model.Hospital;
import main.java.util.DBUtil;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by alfonce on 01/07/2017.
 */
public class HospitalDAO {
    public static Hospital getHospital() {
        Hospital hospital = new Hospital();
        String sql = "select * from hospital";
        try {
            ResultSet resultSet = DBUtil.executeQuery(sql);
            if (resultSet != null) {
                if (resultSet.next()) {
                    hospital.setName(resultSet.getString("Name"));
                    hospital.setAddress(resultSet.getString("Address"));
                    hospital.setPhoneNumber(resultSet.getString("PhoneNumber"));
                    hospital.setCity(resultSet.getString("City"));

                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return hospital;
    }

    public static InputStream getHospitalLetterHeadInputStream() {
        ResultSet resultSet = DBUtil.executeQuery("select letter_head from hospital limit 1");
        try {
            if (resultSet != null && resultSet.next()) {
                if (resultSet.getBlob("letter_head") != null) {
                    return resultSet.getBlob("letter_head").getBinaryStream();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static BufferedImage getLetterHeadBufferedImage() {
        try {
            InputStream inputStream = getHospitalLetterHeadInputStream();
            if (inputStream != null) {
                return ImageIO.read(inputStream);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
