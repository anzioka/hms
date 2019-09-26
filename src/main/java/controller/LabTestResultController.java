package main.java.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;
import main.java.dao.LabTestDAO;
import main.java.model.LabTestFlag;
import main.java.model.LabTestResult;
import main.java.util.DBUtil;
import main.java.util.DateUtil;
import main.java.util.StringUtil;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class LabTestResultController {
    @FXML
    private Label timeCompleted, dateCompleted, comment, test;
    @FXML
    private TableView<Map<String, String>> tableView;
    @FXML
    private TableColumn<Map<String, String>, String> result, flag, flagRefRange;
    private LabTestResult labTestResult;
    private int requestId;
    private int testId;
    private Stage stage;

    @FXML
    private void initialize() {
        setUpTable();
    }

    private void setUpTable() {

        //placeholder
        Label label = new Label("No results to display");
        label.getStyleClass().add("text-danger");
        tableView.setPlaceholder(label);

        result.setCellValueFactory(param -> StringUtil.getStringProperty(param.getValue().get("Result")));
        flagRefRange.setCellValueFactory(param -> StringUtil.getStringProperty(param.getValue().get("Ref")));
        flag.setCellValueFactory(param -> StringUtil.getStringProperty(param.getValue().get("Flag")));
    }

    Stage getStage() {
        return stage;
    }

    void setStage(Stage stage) {
        this.stage = stage;
    }

    public void setParameters(int requestId, int testId) {
        this.requestId = requestId;
        this.testId = testId;
        getResults();
        getTestDetails();
    }

    private void getTestDetails() {
        //get name of test
        Task<String> testNameTask = new Task<String>() {
            @Override
            protected String call() throws Exception {
                String sql = "select TestName from LabTests where TestId = " + testId;
                ResultSet resultSet = DBUtil.executeQuery(sql);
                try {
                    if (resultSet != null && resultSet.next()) {
                        return resultSet.getString("TestName");
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                return null;
            }
        };
        testNameTask.setOnSucceeded(event -> {
            test.setText(testNameTask.getValue());
        });
        new Thread(testNameTask).start();
    }

    private void getResults() {
        Task<ObservableList<Map<String, String>>> task = new Task<ObservableList<Map<String, String>>>() {
            @Override
            protected ObservableList<Map<String, String>> call() {
                ObservableList<Map<String, String>> list = FXCollections.observableArrayList();
                labTestResult = LabTestDAO.getLabTestResult(requestId);
                //get flags
                ObservableList<LabTestFlag> flags = LabTestDAO.getLabTestFlagsForTest(Integer.toString(testId));
                String[] results = labTestResult.getResult().split(",");
                for (int i = 0; i < results.length; i++) {
                    Map<String, String> map = new HashMap<>();
                    map.put("Flag", flags.get(i).getName());
                    map.put("Ref", flags.get(i).getDefaultVal());
                    map.put("Result", results[i]);
                    list.add(map);
                }
                return list;
            }
        };
        task.setOnSucceeded(event -> {
            tableView.setItems(task.getValue());
            dateCompleted.setText(DateUtil.formatDate(labTestResult.getDateCreated()));
            timeCompleted.setText(DateUtil.formatTime(labTestResult.getTimeCreated()));
            comment.setText(labTestResult.getComment());
        });
        new Thread(task).start();
    }

    @FXML
    private void onPrintResults() {
        //TODO : print results
    }
}
