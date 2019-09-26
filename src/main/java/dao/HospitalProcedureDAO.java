package main.java.dao;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import main.java.model.HospitalProcedure;
import main.java.model.PatientProcedure;
import main.java.model.UserCategory;
import main.java.util.DBUtil;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by alfonce on 20/05/2017.
 */
public class HospitalProcedureDAO {
    public static ObservableList<HospitalProcedure> getAllProcedures() {
        String sql = "select * from procedures";
        ResultSet resultSet = DBUtil.executeQuery(sql);
        return getProceduresFromResultSet(resultSet);
    }

    private static ObservableList<HospitalProcedure> getProceduresFromResultSet(ResultSet resultSet) {
        ObservableList<HospitalProcedure> list = FXCollections.observableArrayList();
        try {
            if (resultSet != null) {
                while (resultSet.next()) {
                    HospitalProcedure procedure = new HospitalProcedure();
                    procedure.setName(resultSet.getString("Name"));
                    procedure.setCost(resultSet.getDouble("Cost"));
                    list.add(procedure);
                }

            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public static ObservableList<PatientProcedure> getPatientProcedures(int visitId, boolean outpatientMode) {
        ObservableList<PatientProcedure> procedures = FXCollections.observableArrayList();
        String sql = "select patient_procedures.*, procedures.*, Users.Id, users.FirstName, users.LastName, users.UserCategory " +
                "from patient_procedures inner join procedures on procedures.Name = patient_procedures.procedure_name " +
                "inner join users on users.Id = patient_procedures.user_id ";
        if (outpatientMode) {
            sql += " where visit_id = " + visitId;
        } else {
            sql += " where admission_num = " + visitId;
        }
        ResultSet resultSet = DBUtil.executeQuery(sql);
        if (resultSet != null) {
            try {
                while (resultSet.next()) {
                    PatientProcedure patientProcedure = new PatientProcedure();
                    patientProcedure.setId(resultSet.getInt("patient_procedures.id"));
                    UserCategory category = UserCategory.valueOf(resultSet.getString("UserCategory"));
                    if (category == UserCategory.DOCTOR) {
                        patientProcedure.setUserName("Dr. " + resultSet.getString("LastName"));
                    } else {
                        patientProcedure.setUserName(resultSet.getString("FirstName"));
                    }
                    patientProcedure.setCost(resultSet.getDouble("Cost"));
                    patientProcedure.setName(resultSet.getString("Name"));
                    patientProcedure.setUserId(resultSet.getInt("Users.Id"));
                    procedures.add(patientProcedure);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return procedures;
    }
}
