package main.java.dao;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import main.java.model.PatientProcedure;
import main.java.util.DBUtil;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by alfonce on 20/05/2017.
 */
public class ClinicVisitProcedureDAO {
    public static ObservableList<PatientProcedure> getClinicProcedureByVisitID(int id) {
        String sql = "select * from VisitProcedures where visitId=" + id;
        ResultSet resultSet = DBUtil.executeQuery(sql);
        return getListFromResultSet(resultSet);
    }

    private static ObservableList<PatientProcedure> getListFromResultSet(ResultSet resultSet) {
        ObservableList<PatientProcedure> procedures = FXCollections.observableArrayList();
        try {
            while (resultSet.next()) {
                PatientProcedure procedure = new PatientProcedure();

                procedure.setId(resultSet.getInt("ID"));
                procedure.setVisitId(resultSet.getInt("VisitID"));
                procedure.setName(resultSet.getString("Name"));
                procedure.setCost(resultSet.getDouble("cost"));

                procedures.add(procedure);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return procedures;
    }
}
