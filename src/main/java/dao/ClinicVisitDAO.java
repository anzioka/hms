package main.java.dao;

import main.java.model.ClinicVisitNotes;
import main.java.util.DBUtil;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by alfonce on 19/05/2017.
 */
public class ClinicVisitDAO {

    public static ClinicVisitNotes getNotesByVisitId(int id) {
        ClinicVisitNotes notes = new ClinicVisitNotes();

        String sql = "select * from VisitNotes where VisitId=" + id;
        try {
            ResultSet resultSet = DBUtil.executeQuery(sql);
            if (resultSet != null && resultSet.next()) {
                notes.setVisitId(resultSet.getInt("VisitId"));
                notes.setPrimaryComplains(resultSet.getString("Complains"));
                notes.setMedicalHistory(resultSet.getString("History"));
                notes.setPhysicalExam(resultSet.getString("PhysicalExam"));
                notes.setInvestigation(resultSet.getString("Investigation"));
                notes.setTreatment(resultSet.getString("Treatment"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return notes;
    }

}
