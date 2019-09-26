package main.java.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import main.java.util.DBUtil;
import main.java.util.DateUtil;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by alfonce on 11/07/2017.
 */
public class LaboratoryReportsController {
    private final String DATE = "date";
    private final String COUNT = "count";
    @FXML
    private Label tableTitle;
    @FXML
    private DatePicker startDate, endDate;
    @FXML
    private TableView<Map<String, String>> tableView;
    @FXML
    private TableColumn<Map<String, String>, String> dateCol, numOfRequestsCol;
    @FXML
    private LineChart<String, Integer> chart;
    @FXML
    private NumberAxis numberAxis;
    @FXML
    private CategoryAxis categoryAxis;

    @FXML
    public void initialize() {
        setUpTable();
        startDate.setValue(LocalDate.now().withDayOfMonth(1));
        startDate.setConverter(DateUtil.getDatePickerConverter());
        endDate.setValue(LocalDate.now());
        endDate.setConverter(DateUtil.getDatePickerConverter());
        onSearch();
    }

    private void setUpTable() {

        //place holder
        Label label = new Label("No results to display!\n");
        label.getStyleClass().add("text-danger");
        tableView.setPlaceholder(label);

        dateCol.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().get(DATE)));
        numOfRequestsCol.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().get(COUNT)));
    }

    @FXML
    private void onSearch() {

        Task<ObservableList<Map<String, String>>> task = new Task<ObservableList<Map<String, String>>>() {
            @Override
            protected ObservableList<Map<String, String>> call() throws Exception {
                return getRequestsCount();
            }
        };
        task.setOnSucceeded(event -> {
            tableView.setItems(task.getValue());
            tableTitle.setText("Lab Requests Summary : " + DateUtil.formatDateLong(startDate.getValue()) + " - " + DateUtil.formatDateLong(endDate.getValue()));
            setUpChart(task.getValue());
        });
        new Thread(task).start();

    }

    private ObservableList<Map<String, String>> getRequestsCount() throws SQLException {
        ObservableList<Map<String, String>> list = FXCollections.observableArrayList();
        LocalDate start = startDate.getValue() != null ? startDate.getValue() : LocalDate.now().withDayOfMonth(1);
        LocalDate end = endDate.getValue() != null ? endDate.getValue() : LocalDate.now();
        String sql = "select count(*) as count, DateCreated from lab_requests " +
                "where DateCreated between '" + start + "' and '" + end + "' " +
                "group by dateCreated";
        ResultSet resultSet = DBUtil.executeQuery(sql);
        if (resultSet != null) {
            while (resultSet.next()) {
                Map<String, String> map = new HashMap<>();
                map.put(DATE, DateUtil.formatDate(resultSet.getObject("DateCreated", LocalDate.class)));
                map.put(COUNT, resultSet.getString("count"));
                list.add(map);
            }
        }
        return list;
    }

    private void setUpChart(ObservableList<Map<String, String>> data) {

        XYChart.Series<String, Integer> series = new XYChart.Series<>();
        series.setName("Lab Requests");

        for (Map<String, String> map : data) {
            series.getData().add(new XYChart.Data<>(map.get(DATE), Integer.valueOf(map.get(COUNT))));
        }
        numberAxis.setLabel("Requests");
        categoryAxis.setLabel("Date");
        chart.getData().clear();
        chart.getData().add(series);
    }

}
