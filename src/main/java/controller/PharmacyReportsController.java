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
import javafx.scene.control.*;
import main.java.model.Prescription;
import main.java.util.DBUtil;
import main.java.util.DateUtil;
import org.controlsfx.control.textfield.TextFields;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by alfonce on 11/07/2017.
 */
public class PharmacyReportsController {
    private final String QUANTITY = "quantity";
    private final String DATE = "date";

    @FXML
    private Label tableViewTitle;
    @FXML
    private TextField drugSearchField;
    @FXML
    private DatePicker startDate, endDate;
    @FXML
    private TableView<Map<String, String>> tableView;
    @FXML
    private TableColumn<Map<String, String>, String> dateColumn, totalColumn;

    @FXML
    private LineChart<String, Integer> chart;

    @FXML
    private CategoryAxis categoryAxis;

    @FXML
    private NumberAxis numberAxis;

    @FXML
    public void initialize() {
        endDate.setValue(LocalDate.now());
        endDate.setConverter(DateUtil.getDatePickerConverter());
        startDate.setConverter(DateUtil.getDatePickerConverter());
        startDate.setValue(LocalDate.now().withDayOfMonth(1));
        configureTable();
        getDrugs();
    }

    private void getDrugs() {
        Task<ObservableList<String>> task = new Task<ObservableList<String>>() {
            @Override
            protected ObservableList<String> call() {
                ObservableList<String> list = FXCollections.observableArrayList();
                String sql = "select Distinct name from drugs";
                try {
                    ResultSet resultSet = DBUtil.executeQuery(sql);
                    if (resultSet != null) {
                        while (resultSet.next()) {
                            list.add(resultSet.getString("name"));
                        }
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                return list;
            }
        };
        task.setOnSucceeded(event -> {
            TextFields.bindAutoCompletion(drugSearchField, task.getValue());
            onSearch();
        });
        new Thread(task).start();
    }

    private void configureTable() {
        //columns
        dateColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().get(DATE)));
        totalColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().get(QUANTITY)));

        //place holder;
        Label label = new Label("No records found!");
        label.getStyleClass().add("text-danger");
        tableView.setPlaceholder(label);
    }

    @FXML
    private void onSearch() {

        Task<ObservableList<Map<String, String>>> task = new Task<ObservableList<Map<String, String>>>() {
            @Override
            protected ObservableList<Map<String, String>> call() throws Exception {
                return getDispensationData();
            }
        };
        task.setOnSucceeded(event -> {
            tableView.setItems(task.getValue());
            tableViewTitle.setText("Medicine Dispensation Summary : " + DateUtil.formatDateLong(startDate.getValue()) + " - " + DateUtil.formatDateLong(endDate.getValue()));
            setUpChart(task.getValue());
        });
        new Thread(task).start();

    }

    private void setUpChart(ObservableList<Map<String, String>> data) {
        Task<XYChart.Series<String, Integer>> task = new Task<XYChart.Series<String, Integer>>() {
            @Override
            protected XYChart.Series<String, Integer> call() {
                XYChart.Series<String, Integer> series = new XYChart.Series<>();
                for (Map<String, String> item : data) {
                    series.getData().add(new XYChart.Data<>(item.get(DATE), Integer.valueOf(item.get(QUANTITY))));
                }
                return series;
            }
        };
        task.setOnSucceeded(event -> {
            chart.getData().clear();
            numberAxis.setLabel("Quantity Dispensed");
            categoryAxis.setLabel("Date");
            chart.getData().add(task.getValue());
        });
        new Thread(task).start();
    }

    private ObservableList<Map<String, String>> getDispensationData() throws SQLException {
        ObservableList<Map<String, String>> list = FXCollections.observableArrayList();
        String sql = getSqlQueryString();
        ResultSet resultSet = DBUtil.executeQuery(sql);
        if (resultSet != null) {
            while (resultSet.next()) {
                Map<String, String> map = new HashMap<>();
                map.put(DATE, DateUtil.formatDateLong(resultSet.getObject("date_created", LocalDate.class)));
                map.put(QUANTITY, resultSet.getString("quantity"));
                list.add(map);
            }
        }
        return list;
    }

    private String getSqlQueryString() {
        LocalDate start = startDate.getValue() != null ? startDate.getValue() : LocalDate.now().withDayOfMonth(1);
        startDate.setValue(start);
        LocalDate end = endDate.getValue() != null ? endDate.getValue() : LocalDate.now();
        endDate.setValue(end);

        String sql = "select sum(quantity) as quantity, date_created from prescriptions ";
        boolean allDrugs = true;
        if (drugSearchField.getText() != null && !drugSearchField.getText().isEmpty()) {
            sql += "inner join drugs on drugs.DrugCode = prescriptions.drug_code ";
            allDrugs = false;
        }
        sql += " where status = '" + Prescription.Status.COMPLETED + "' " +
                "and date_created between '" + start + "' and '" + end + "' ";
        if (!allDrugs) {
            sql += " and drugs.Name = '" + drugSearchField.getText() + "' ";
        }
        sql += "group by date_created";
        return sql;
    }
}
