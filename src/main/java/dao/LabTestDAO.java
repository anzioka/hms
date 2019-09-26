package main.java.dao;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import main.java.model.*;
import main.java.util.DBUtil;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Map;

/**
 * Created by alfonce on 03/06/2017.
 */
public class LabTestDAO {
    private static void getTestsFromResultSet(ResultSet resultSet, Map<String, Double> tests) {
        try {
            while (resultSet.next()) {
                tests.put(resultSet.getString("TestName"), resultSet.getDouble("Cost"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static ObservableList<LabTest> getAllLabTests() {
        ObservableList<LabTest> list = FXCollections.observableArrayList();
        String sql = "select * from LabTests";
        try {
            ResultSet resultSet = DBUtil.executeQuery(sql);
            if (resultSet != null) {
                while (resultSet.next()) {
                    LabTest labTest = new LabTest();
                    labTest.setTestId(resultSet.getInt("TestId"));
                    labTest.setName(resultSet.getString("TestName"));
                    labTest.setCost(resultSet.getDouble("Cost"));
                    list.add(labTest);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    private static void getLabRequestsFromResultSet(ObservableList<LabRequest> list, ResultSet resultSet) throws
            SQLException {
        if (resultSet != null) {
            while (resultSet.next()) {
                LabRequest request = new LabRequest();
                request.setId(resultSet.getInt("ID"));
                request.setQueueNum(resultSet.getInt("QueueId"));
                request.setAdmissionNum(resultSet.getInt("AdmissionNum"));
                if (resultSet.getString("Specimen") != null) {
                    request.setSpecimen(Specimen.valueOf(resultSet.getString("Specimen")));
                } else {
                    request.setSpecimen(null);
                }
                request.setTestId(resultSet.getInt("TestId"));
                request.setName(resultSet.getString("TestName"));
                request.setCost(resultSet.getDouble("Cost"));
                request.setStatus(LabRequest.Status.valueOf(resultSet.getString("Status")));
                request.setTimeCreated(resultSet.getObject("TimeCreated", LocalTime.class));
                request.setDateCreated(resultSet.getObject("DateCreated", LocalDate.class));
                list.add(request);
            }
        }

    }

    public static ObservableList<LabRequest> getLabRequests(String sql) {
        ObservableList<LabRequest> requests = FXCollections.observableArrayList();
        try {
            ResultSet resultSet = DBUtil.executeQuery(sql);
            getLabRequestsFromResultSet(requests, resultSet);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return requests;
    }

    public static ObservableList<LabTestFlag> getLabTestFlagsForTest(String testId) {
        ObservableList<LabTestFlag> flags = FXCollections.observableArrayList();
        String sql = "select FlagName, RefRange from lab_test_flags where TestID = '" + testId + "'";
        try {
            ResultSet resultSet = DBUtil.executeQuery(sql);
            if (resultSet != null) {
                while (resultSet.next()) {
                    LabTestFlag flag = new LabTestFlag();
                    flag.setName(resultSet.getString("FlagName"));
                    flag.setDefaultVal(resultSet.getString("RefRange"));
                    flags.add(flag);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return flags;
    }

    public static LabTestResult getLabTestResult(int requestId) {
        String sql = "select * from lab_test_results where requestId = " + requestId;
        ResultSet resultSet = DBUtil.executeQuery(sql);
        if (resultSet != null) {
            try {
                if (resultSet.next()) {
                    LabTestResult result = new LabTestResult();
                    result.setResult(resultSet.getString("Result"));
                    result.setComment(resultSet.getString("comment"));
                    result.setSpecimen(Specimen.valueOf(resultSet.getString("Specimen")));
                    result.setDateCreated(resultSet.getObject("DateCreated", LocalDate.class));
                    result.setTimeCreated(resultSet.getObject("TimeCreated", LocalTime.class));
                    return result;
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static ObservableList<LabTestFlag> getLabTestFlags() {
        ObservableList<LabTestFlag> labTestFlags = FXCollections.observableArrayList();
        String sql = "select FlagName, RefRange, TestName from lab_test_flags " +
                "inner join LabTests on LabTests.TestId = lab_test_flags.TestId";
        try {
            ResultSet resultSet = DBUtil.executeQuery(sql);
            if (resultSet != null) {
                while (resultSet.next()) {
                    LabTestFlag flag = new LabTestFlag();
                    flag.setDefaultVal(resultSet.getString("RefRange"));
                    flag.setName(resultSet.getString("FlagName"));
                    flag.setTest(resultSet.getString("TestName"));
                    labTestFlags.add(flag);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return labTestFlags;
    }
}
