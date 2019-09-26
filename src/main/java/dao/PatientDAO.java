package main.java.dao;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import main.java.model.FamilyRelation;
import main.java.model.MaritalStatus;
import main.java.model.Patient;
import main.java.util.DBUtil;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

/**
 * Created by alfonce on 24/04/2017.
 */
public class PatientDAO {

    public static ObservableList<Patient> getPatientObservableList() {
        String query = "SELECT * from Patients";
        ResultSet resultSet = DBUtil.executeQuery(query);
        return getPatientObservableListFromResultSet(resultSet);
    }

    private static ObservableList<Patient> getPatientObservableListFromResultSet(ResultSet resultSet) {
        ObservableList<Patient> list = FXCollections.observableArrayList();
        try {
            if (resultSet != null) {
                while (resultSet.next()) {
                    Patient patient = new Patient();
                    patient.setPatientId(resultSet.getString("PatientID"));
                    patient.setFirstName(resultSet.getString("FirstName"));
                    patient.setLastName(resultSet.getString("LastName"));
                    patient.setTelephoneNumber(resultSet.getString("PhoneNumber"));
                    patient.setDateOfBirth(resultSet.getObject("DateOfBirth", LocalDate.class));
                    patient.setSexuality(resultSet.getString("Sex"));
                    patient.setNHIFNumber(resultSet.getString("NHIFNumber"));
                    patient.setResidence(resultSet.getString("Residence"));
                    patient.setMaritalStatus(MaritalStatus.valueOf(resultSet.getString("MaritalStatus")));
                    patient.setInsuranceID(resultSet.getString("InsuranceId"));
                    patient.setInsurer(resultSet.getString("InsuranceProvider"));
                    patient.setContactFirstName(resultSet.getString("ContactFirstName"));
                    patient.setContactLastName(resultSet.getString("ContactLastName"));

                    if (resultSet.getString("ContactRelationShip") != null) {
                        patient.setContactRelationship(FamilyRelation.valueOf(resultSet.getString("ContactRelationship")));
                    }
                    patient.setContactTelephone(resultSet.getString("ContactPhoneNumber"));
                    patient.setDateCreated(resultSet.getObject("DateCreated", LocalDate.class));
                    list.add(patient);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public static Patient getPatient(String sql) {
        ResultSet resultSet = DBUtil.executeQuery(sql);
        ObservableList<Patient> list = getPatientObservableListFromResultSet(resultSet);
        if (list.size() > 0) {
            return list.get(0);
        } else {
            return null;
        }
    }
}
