package main.java.dao;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import main.java.model.Diagnosis;
import main.java.model.ICD10_Diagnosis;
import main.java.model.PatientCategory;
import main.java.util.DBUtil;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

public class DiagnosisDAO {
    public static ObservableList<ICD10_Diagnosis> getDiagnosisList() {
        ObservableList<ICD10_Diagnosis> list = FXCollections.observableArrayList();
        ResultSet resultSet = DBUtil.executeQuery("select * from icd10_diagnoses");
        if (resultSet != null) {
            try {
                while (resultSet.next()) {
                    ICD10_Diagnosis ICD10Diagnosis = new ICD10_Diagnosis(resultSet.getString("code"), resultSet.getString("name"));
                    list.add(ICD10Diagnosis);
                }
            } catch (SQLException ignored) {

            }

        }
        return list;
    }

    public static ObservableList<Diagnosis> getDiagnoses(int visitId, PatientCategory patientCategory) throws SQLException{
        ObservableList<Diagnosis> list = FXCollections.observableArrayList();
        String sql = "select icd10_diagnoses.name, diagnosis.id, user_id, date_created " +
                "from diagnosis " +
                "inner join icd10_diagnoses on icd10_diagnoses.code = diagnosis.code " +
                "inner join users on users.Id = diagnosis.user_id ";
        if (patientCategory == PatientCategory.INPATIENT) {
            sql += " where admission_num = " + visitId;
        } else{
            sql += " where visit_id = " + visitId;
        }
        ResultSet resultSet = DBUtil.executeQuery(sql);
        if (resultSet != null) {
            while (resultSet.next()) {
                Diagnosis diagnosis = new Diagnosis();
                diagnosis.setId(resultSet.getInt("id"));
                diagnosis.setUserId(resultSet.getInt("user_id"));
                diagnosis.setName(resultSet.getString("name"));
                diagnosis.setDateCreated(resultSet.getObject("date_created", LocalDate.class));
                list.add(diagnosis);
            }
        }
        return list;
    }
}
