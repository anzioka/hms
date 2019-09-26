package main.java.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import main.java.model.AgeGroup;
import main.java.util.AgeUtil;
import main.java.util.DBUtil;
import main.java.util.DateUtil;
import main.java.util.NumberUtil;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by alfonce on 11/07/2017.
 */
public class PatientVisitReportsController {
    private final String DATE = "date";
    private final String COUNT = "count";
    private final String AGE = "age";
    @FXML
    private DatePicker fromDate, toDate;

    @FXML
    private ChoiceBox<String> sexChoice;

    @FXML
    private ChoiceBox<AgeGroup> ageChoice;
    @FXML
    private LineChart<String, Integer> lineChart;
    @FXML
    private PieChart pieChart;
    @FXML
    private NumberAxis numberAxis;
    @FXML
    private CategoryAxis categoryAxis;
    @FXML
    private Label reportTitle;

    @FXML
    private TableView<Map<String, String>> tableView;

    @FXML
    private TableColumn<Map<String, String>, String> dateCol, totalCol;

    @FXML
    public void initialize() {
        fromDate.setConverter(DateUtil.getDatePickerConverter());
        fromDate.setValue(LocalDate.now().withDayOfMonth(1));

        toDate.setConverter(DateUtil.getDatePickerConverter());
        toDate.setValue(LocalDate.now());
        ageChoice.setItems(FXCollections.observableArrayList(AgeGroup.values()));
        ageChoice.getItems().add(0, null);
        ageChoice.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            pieChart.setVisible(newValue == null);
            pieChart.setManaged(newValue == null);
            onSearch();
        });
        ageChoice.getSelectionModel().select(0);

        sexChoice.setItems(FXCollections.observableArrayList(null, "Male", "Female"));
        sexChoice.getSelectionModel().select(0);
        sexChoice.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            onSearch();
        });
        onSearch();

        setUpTable();

    }

    private void setUpTable() {
        //place holder
        Label label = new Label("No records found!");
        label.getStyleClass().add("text-danger");
        tableView.setPlaceholder(label);

        dateCol.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().get(DATE)));
        totalCol.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().get(COUNT)));

    }

    @FXML
    private void onSearch() {
        Task<ObservableList<Map<String, String>>> task = new Task<ObservableList<Map<String, String>>>() {
            @Override
            protected ObservableList<Map<String, String>> call() throws Exception {
                return getPatientVisits();
            }
        };
        task.setOnSucceeded(event -> {
            tableView.setItems(task.getValue());
            reportTitle.setText("Patient Visits Summary : " + DateUtil.formatDateLong(fromDate.getValue()) + " - " + DateUtil.formatDateLong(toDate.getValue()));
            setUpLineChart(task.getValue());
            if (ageChoice.getValue() == null && !task.getValue().isEmpty()) {
                setUpAgeDistributionChart();
            } else {
                pieChart.setVisible(false);
                pieChart.setManaged(false);
            }
        });
        new Thread(task).start();
    }

    private void setUpLineChart(ObservableList<Map<String, String>> list) {
        Task<XYChart.Series<String, Integer>> task = new Task<XYChart.Series<String, Integer>>() {
            @Override
            protected XYChart.Series<String, Integer> call() {
                XYChart.Series<String, Integer> series = new XYChart.Series<>();
                for (Map<String, String> entry : list) {
                    series.getData().add(new XYChart.Data<>(entry.get(DATE), Integer.valueOf(entry.get(COUNT))));
                }
                return series;
            }
        };

        task.setOnSucceeded(event -> {
            lineChart.getData().clear();
            lineChart.getData().add(task.getValue());
            numberAxis.setLabel("Visits");
            categoryAxis.setLabel("Date");
        });
        new Thread(task).start();
    }

    private ObservableList<Map<String, String>> getPatientVisits() throws SQLException {
        ObservableList<Map<String, String>> data = FXCollections.observableArrayList();
        String sql = getSearchString();
        if (sql != null) {
            ResultSet resultSet = DBUtil.executeQuery(sql);
            if (resultSet != null) {
                while (resultSet.next()) {
                    Map<String, String> item = new HashMap<>();
                    item.put(DATE, DateUtil.formatDate(resultSet.getObject("DateCreated", LocalDate.class)));
                    if (ageChoice.getValue() != null) {
                        item.put(AGE, AgeUtil.getYears(resultSet.getObject("DateOfBirth", LocalDate.class)) + "");
                    } else {
                        item.put(COUNT, resultSet.getInt("Count") + "");
                    }
                    data.add(item);
                }
            }
            if (ageChoice.getValue() != null) {
                data = filterByAge(data);
            }

        }

        return data;
    }

    private void setUpAgeDistributionChart() {

        Task<ObservableList<PieChart.Data>> task = new Task<ObservableList<PieChart.Data>>() {
            @Override
            protected ObservableList<PieChart.Data> call() throws Exception {

                List<LocalDate> dateList = new ArrayList<>();
                String sql = "SELECT DateOfBirth from Patients " +
                        "INNER JOIN Queues on Queues.PatientID  = Patients.PatientID " +
                        "WHERE Queues.DateCreated Between '" + fromDate.getValue() + "' AND '" + toDate.getValue() + "'";
                if (sexChoice.getValue() != null) {
                    sql += " and Sex = '" + sexChoice.getValue() + "'";
                }
                ResultSet resultSet = DBUtil.executeQuery(sql);
                if (resultSet != null) {
                    while (resultSet.next()) {
                        dateList.add(resultSet.getObject("DateOfBirth", LocalDate.class));
                    }
                }

                return getAgeGroupDistribution(dateList);
            }
        };

        task.setOnSucceeded(event -> {
            pieChart.getData().clear();
            pieChart.setData(task.getValue());
        });

        new Thread(task).start();

    }

    private ObservableList<PieChart.Data> getAgeGroupDistribution(List<LocalDate> dateList) {
        ObservableList<PieChart.Data> list = FXCollections.observableArrayList();
        Map<String, Integer> map = new HashMap<>();
        for (LocalDate date : dateList) {
            int age = AgeUtil.getYears(date);
            for (AgeGroup ageGroup : AgeGroup.values()) {
                if (ageGroup.containsAge(age)) {
                    if (map.containsKey(ageGroup.toString())) {
                        map.put(ageGroup.toString(), map.get(ageGroup.toString()) + 1);
                    } else {
                        map.put(ageGroup.toString(), 1);
                    }
                    break;
                }
            }
        }

        for (String key : map.keySet()) {
            list.add(new PieChart.Data(key, (double) map.get(key)));
        }
        return list;
    }

    private ObservableList<Map<String, String>> filterByAge(ObservableList<Map<String, String>> data) {
        ObservableList<Map<String, String>> list = FXCollections.observableArrayList();

        //get all dates
        //map each date to count
        Map<String, Integer> dateCountMap = new HashMap<>();
        for (Map<String, String> item : data) {
            int age = NumberUtil.stringToInt(item.get(AGE));
            if (ageChoice.getValue().containsAge(age)) {
                if (dateCountMap.containsKey(item.get(AGE))) {
                    dateCountMap.put(item.get(AGE), dateCountMap.get(item.get(AGE)) + 1);
                } else {
                    dateCountMap.put(item.get(AGE), 1);
                }
            }
        }

        for (String date : dateCountMap.keySet()) {
            Map<String, String> map = new HashMap<>();
            map.put(DATE, date);
            map.put(COUNT, dateCountMap.get(date) + "");
            list.add(map);
        }

        return list;
    }

    private String getSearchString() {
        String sql;

        LocalDate start = fromDate.getValue() != null ? fromDate.getValue() : LocalDate.now().withDayOfMonth(1);
        fromDate.setValue(start);
        LocalDate end = toDate.getValue() != null ? toDate.getValue() : LocalDate.now();
        toDate.setValue(end);

        if (ageChoice.getValue() != null) {
            sql = "SELECT Queues.DateCreated, Patients.DateOfBirth " +
                    "FROM Queues \n";
        } else {
            sql = "SELECT Queues.DateCreated, count(VisitId) as Count " +
                    "FROM Queues ";
        }
        sql += "INNER JOIN Patients on Patients.PatientId = Queues.PatientId " +
                "WHERE Queues.DateCreated Between '" + start + "' AND '" + end + "' \n";

        if (sexChoice.getValue() != null) {
            sql += " and Sex = '" + sexChoice.getValue() + "' ";
        }
        if (ageChoice.getValue() == null) {
            sql += " group by DateCreated";
        }

        return sql;
    }

}
