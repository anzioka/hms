package main.java.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import main.java.dao.InpatientDao;
import main.java.model.Ward;
import main.java.util.DBUtil;
import main.java.util.DateUtil;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WardOccupancyReportsController {
    private final String DATE_ADMITTED = "date_admitted";
    private final String DURATION = "duration";

    @FXML
    private PieChart pieChart;
    @FXML
    private LineChart<String, Number> lineChart;
    @FXML
    private Label averageStay;
    @FXML
    private DatePicker startDate, endDate;
    @FXML
    private ChoiceBox<Ward> wardChoiceBox;


    @FXML
    private void initialize() {
        startDate.setConverter(DateUtil.getDatePickerConverter());
        startDate.setValue(LocalDate.now().withDayOfYear(1));

        endDate.setConverter(DateUtil.getDatePickerConverter());
        endDate.setValue(LocalDate.now());

        getData();
    }

    private void getData() {
        Task<ObservableList<Ward>> task = new Task<ObservableList<Ward>>() {
            @Override
            protected ObservableList<Ward> call() throws Exception {
                return InpatientDao.getWards();
            }
        };
        task.setOnSucceeded(event -> {
            wardChoiceBox.setItems(task.getValue());
            wardChoiceBox.getItems().add(0, null);
            getNumBeds();
        });
        new Thread(task).start();
    }

    private void getNumBeds() {
        Task<Integer> task = new Task<Integer>() {
            @Override
            protected Integer call() throws Exception {
                ResultSet resultSet = DBUtil.executeQuery("select sum(num_beds) as total from wards");
                if (resultSet != null && resultSet.next()) {
                    return resultSet.getInt("total");
                }
                return 0;
            }
        };
        task.setOnSucceeded(event -> {
            onGenerateReport();
        });
        new Thread(task).start();
    }

    @FXML
    private void onGenerateReport() {
        Task<List<Map<String, String>>> task = new Task<List<Map<String, String>>>() {
            @Override
            protected List<Map<String, String>> call() throws Exception {
                return getAdmissionData();
            }
        };
        task.setOnSucceeded(event -> {
            computeStats(task.getValue());
            setUpLineGraph(task.getValue());
            setUpPieChart();
        });
        new Thread(task).start();
    }

    private void setUpPieChart() {
        Task<ObservableList<PieChart.Data>> task = new Task<ObservableList<PieChart.Data>>() {
            @Override
            protected ObservableList<PieChart.Data> call() throws Exception {
                ObservableList<PieChart.Data> data = FXCollections.observableArrayList();
                String sql = "select sum(occupied) as occupied, sum(num_beds) as total " +
                        "from wards";
                if (wardChoiceBox.getValue() != null) {
                    sql = "select occupied, num_beds as total from wards " +
                            "where ward_id = " + wardChoiceBox.getValue().getId();
                }
                ResultSet resultSet = DBUtil.executeQuery(sql);
                if (resultSet != null && resultSet.next()) {
                    data.add(new PieChart.Data("Occupied", resultSet.getInt("Occupied")));
                    data.add(new PieChart.Data("Empty", resultSet.getInt("total") - resultSet.getInt("occupied")));

                }


                return data;
            }
        };
        task.setOnSucceeded(event -> {
            pieChart.setTitle("Bed Occupation Chart");
            pieChart.getData().clear();
            pieChart.setData(task.getValue());
        });
        new Thread(task).start();
    }

    private void setUpLineGraph(List<Map<String, String>> data) {
        Task<XYChart.Series<String, Number>> task = new Task<XYChart.Series<String, Number>>() {
            @Override
            protected XYChart.Series<String, Number> call() {
                XYChart.Series<String, Number> series = new XYChart.Series<>();
                Map<String, Integer> admissionDateCountMap = new HashMap<>();
                for (Map<String, String> item : data) {
                    if (admissionDateCountMap.containsKey(item.get(DATE_ADMITTED))) {
                        admissionDateCountMap.put(item.get(DATE_ADMITTED), admissionDateCountMap.get(item.get(DATE_ADMITTED) + 1));
                    } else {
                        admissionDateCountMap.put(item.get(DATE_ADMITTED), 1);
                    }
                }
                for (String date : admissionDateCountMap.keySet()) {
                    series.getData().add(new XYChart.Data<>(date, admissionDateCountMap.get(date)));
                }
                return series;
            }
        };
        task.setOnSucceeded(event -> {
            lineChart.getData().clear();
            lineChart.getData().add(task.getValue());
        });
        new Thread(task).start();
    }

    private void computeStats(List<Map<String, String>> data) {
        Task<Integer> task = new Task<Integer>() {
            @Override
            protected Integer call() {
                return getAverageStay(data);
            }
        };
        task.setOnSucceeded(event -> {
            averageStay.setText(task.getValue() + " Days");
        });
        new Thread(task).start();
    }


    private List<Map<String, String>> getAdmissionData() throws SQLException {
        List<Map<String, String>> data = new ArrayList<>();
        LocalDate start = startDate.getValue() != null ? startDate.getValue() : LocalDate.now().withDayOfYear(1);
        LocalDate end = endDate.getValue() != null ? endDate.getValue() : LocalDate.now();
        String sql = "select date_admitted, date_discharged from inpatients " +
                "where (date_admitted between '" + start + "' and '" + end + "' " +
                "or date_discharged between '" + start + "' and '" + end + "' ) ";
        if (wardChoiceBox.getValue() != null) {
            sql += " and ward_id = " + wardChoiceBox.getValue().getId();
        }
        ResultSet resultSet = DBUtil.executeQuery(sql);
        if (resultSet != null) {
            while (resultSet.next()) {
                Map<String, String> map = new HashMap<>();
                map.put(DATE_ADMITTED, DateUtil.formatDateLong(resultSet.getObject("date_admitted", LocalDate.class)));
                LocalDate first, last;
                if (resultSet.getObject("date_admitted", LocalDate.class).isBefore(start)) {
                    first = start;
                } else {
                    first = resultSet.getObject("date_admitted", LocalDate.class);
                }
                if (resultSet.getObject("date_discharged") != null && resultSet.getObject("date_discharged", LocalDate.class).isBefore(end)) {
                    last = resultSet.getObject("date_discharged", LocalDate.class);
                } else {
                    last = end;
                }
                map.put(DURATION, Integer.toString(DateUtil.getNumDaysDiff(first, last)));
                data.add(map);
            }
        }
        return data;
    }

    private int getAverageStay(List<Map<String, String>> data) {
        if (data.isEmpty()) {
            return 0;
        }
        int numDays = 0;
        for (Map<String, String> item : data) {
            numDays += Integer.parseInt(item.get(DURATION));
        }
        return numDays / data.size();
    }
}

