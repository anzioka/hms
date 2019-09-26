package main.java.controller;

import javafx.fxml.FXML;
import javafx.geometry.Side;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import main.java.util.DBUtil;
import main.java.util.DateUtil;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by alfonce on 22/04/2017.
 */
public class DashboardController {

    @FXML
    private Label todayVisits, totalVisits;

    @FXML
    private LineChart<String, Integer> lineChart;

    @FXML
    private DatePicker fromDate, toDate;

    @FXML
    private NumberAxis numberAxis;

    @FXML
    private CategoryAxis categoryAxis;

    @FXML
    public void initialize() {
        fromDate.setValue(LocalDate.of(2017, 7, 1));
        toDate.setValue(LocalDate.now());

        fromDate.setOnAction(event -> {
            getVisitsOverPeriod();
        });

        toDate.setOnAction(event -> {
            getVisitsOverPeriod();
        });

        fromDate.setConverter(DateUtil.getDatePickerConverter());
        toDate.setConverter(DateUtil.getDatePickerConverter());

        getTodayVisits();
        getVisitsOverPeriod();
        configureChart();
    }

    private void configureChart() {
        lineChart.setTitle("Patient Visits");
        categoryAxis.setLabel("Dates");
        numberAxis.setLabel("Total Visits");

        lineChart.setLegendSide(Side.LEFT);

    }

    private void getVisitsOverPeriod() {
        lineChart.getData().clear();
        String sql = "select count(visitId) as count, datecreated from queues " +
                "group by datecreated";
        Map<String, Integer> visitsCountMap = new HashMap<>();
        try {
            ResultSet resultSet = DBUtil.executeQuery(sql);
            if (resultSet != null) {
                while (resultSet.next()) {
                    String dateCreated = DateUtil.formatDate(resultSet.getObject("DateCreated", LocalDate.class));
                    int numVisits = resultSet.getInt("Count");
                    visitsCountMap.put(dateCreated, numVisits);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        updateView(visitsCountMap);
    }

    private void updateView(Map<String, Integer> visitsCountMap) {

        //set up chart
        XYChart.Series<String, Integer> series = new XYChart.Series<>();
        series.setName("Number of Visits");
        int totalCount = 0;
        for (String date : visitsCountMap.keySet()) {
            series.getData().add(new XYChart.Data<>(date, visitsCountMap.get(date)));
            totalCount += visitsCountMap.get(date);
        }
        totalVisits.setText(totalCount + "");
        lineChart.getData().add(series);
    }

    private void getTodayVisits() {
        int result = 0;
        String sql = "select count(VisitId) from queues where DateCreated = '" + LocalDate.now() + "'";
        try {
            ResultSet resultSet = DBUtil.executeQuery(sql);
            if (resultSet != null) {
                if (resultSet.next()) {
                    result = resultSet.getInt("count(visitId)");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        todayVisits.setText("" + result);
    }
}

