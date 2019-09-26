package main.java.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import main.java.model.Bill;
import main.java.util.CurrencyUtil;
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

import static main.java.controller.BillingController.AMOUNT_PAID;
import static main.java.controller.BillingController.DATE;

public class CashCollectionReportController {
    @FXML
    private Label reportTitle, totalLabel;

    @FXML
    private TableView<Map<String, String>> tableView;
    @FXML
    private TableColumn<Map<String, String>, String> date, total;
    @FXML
    private DatePicker startDate, endDate;

    @FXML
    private ChoiceBox<Bill.Category> categoryChoiceBox;
    @FXML
    private LineChart<String, Double> lineChart;
    @FXML
    private CategoryAxis categoryAxis;
    @FXML
    private NumberAxis numberAxis;
    @FXML
    private PieChart pieChart;

    @FXML
    private void initialize() {
        setUpTable();
        endDate.setConverter(DateUtil.getDatePickerConverter());
        endDate.setValue(LocalDate.now());

        startDate.setConverter(DateUtil.getDatePickerConverter());
        startDate.setValue(LocalDate.now().withDayOfMonth(1));

        onSearch();

        categoryChoiceBox.setItems(FXCollections.observableArrayList(Bill.Category.values()));
        categoryChoiceBox.getSelectionModel().select(Bill.Category.ALL);
        categoryChoiceBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            onSearch();
        });
    }

    private void setUpTable() {
        Label label = new Label("No records matching search parameters!");
        label.getStyleClass().add("text-danger");
        tableView.setPlaceholder(label);

        date.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().get(DATE)));
        total.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().get(AMOUNT_PAID)));
    }

    @FXML
    private void onSearch() {
        //get data, generate line chart and pie chart
        LocalDate start = startDate.getValue() != null ? startDate.getValue() : LocalDate.now().withDayOfMonth(1);
        startDate.setValue(start);
        LocalDate end = endDate.getValue() != null ? endDate.getValue() : LocalDate.now();
        endDate.setValue(end);

        Task<ObservableList<Map<String, String>>> task = new Task<ObservableList<Map<String, String>>>() {
            @Override
            protected ObservableList<Map<String, String>> call() throws Exception {
                return getDailyCashPayments(start, end, categoryChoiceBox.getValue());
            }
        };
        task.setOnSucceeded(event -> {
            tableView.setItems(task.getValue());
            reportTitle.setText("Cash Collection: " + DateUtil.formatDateLong(start) + " - " + DateUtil.formatDateLong(end));
            totalLabel.setText("Ksh. " + getTotal());
            setUpChart(task.getValue());
        });
        new Thread(task).start();
    }

    private ObservableList<Map<String, String>> getDailyCashPayments(LocalDate start, LocalDate end, Bill.Category category) throws SQLException {
        ObservableList<Map<String, String>> list = FXCollections.observableArrayList();
        String sql = "select sum(payments.amount) as total, date_created from payments " +
                "where date_created between '" + start + "' and '" + end + "' ";
        if (category != null && category != Bill.Category.ALL) {
            sql += " and category = '" + category.name() + "' ";
        }
        sql += " group by date_created";
        ResultSet resultSet = DBUtil.executeQuery(sql);
        if (resultSet != null) {
            while (resultSet.next()) {
                Map<String, String> data = new HashMap<>();
                data.put(AMOUNT_PAID, CurrencyUtil.formatCurrency(resultSet.getDouble("total")));
                data.put(DATE, DateUtil.formatDateLong(resultSet.getObject("date_created", LocalDate.class)));
                list.add(data);
            }
        }
        return list;
    }

    private String getTotal() {
        double total = 0;
        for (Map<String, String> data : tableView.getItems()) {
            total += CurrencyUtil.parseCurrency(data.get(AMOUNT_PAID));
        }
        return CurrencyUtil.formatCurrency(total);
    }

    private void setUpChart(ObservableList<Map<String, String>> payments) {
        //line chart

        Task<XYChart.Series<String, Double>> task = new Task<XYChart.Series<String, Double>>() {
            @Override
            protected XYChart.Series<String, Double> call() {
                XYChart.Series<String, Double> series = new XYChart.Series<>();
                for (Map<String, String> entry : payments) {
                    XYChart.Data<String, Double> data = new XYChart.Data<>(entry.get(DATE), CurrencyUtil.parseCurrency(entry.get(AMOUNT_PAID)));
                    series.getData().add(data);
                }
                return series;
            }
        };
        task.setOnSucceeded(event -> {
            drawLineChart(task.getValue());
            if ((categoryChoiceBox.getValue() == null || categoryChoiceBox.getValue() == Bill.Category.ALL) && !payments.isEmpty()) {
                setUpPieChart();
                pieChart.setVisible(true);
                pieChart.setManaged(true);
            } else {
                pieChart.setVisible(false);
                pieChart.setManaged(false);
            }
        });
        new Thread(task).start();
    }

    private void setUpPieChart() {
        Task<ObservableList<PieChart.Data>> task = new Task<ObservableList<PieChart.Data>>() {
            @Override
            protected ObservableList<PieChart.Data> call() throws Exception {
                ObservableList<PieChart.Data> list = FXCollections.observableArrayList();
                ObservableList<Map<String, Double>> data = getCategorizedPayments();
                double total = CurrencyUtil.parseCurrency(getTotal());
                for (Map<String, Double> entry : data) {
                    List<String> key = new ArrayList<>(entry.keySet());
                    list.add(new PieChart.Data(key.get(0), getPercentage(entry.get(key.get(0)), total)));
                }
                return list;
            }

            private double getPercentage(Double aDouble, double total) {
                return NumberUtil.getNearestWholeNumber(aDouble / total * 100);
            }
        };
        task.setOnSucceeded(event -> {
            pieChart.setData(task.getValue());
        });
        new Thread(task).start();
    }

    private ObservableList<Map<String, Double>> getCategorizedPayments() throws SQLException {
        ObservableList<Map<String, Double>> list = FXCollections.observableArrayList();
        String sql = "select category, sum(amount) as total " +
                "from payments " +
                "where date_created between '" + startDate.getValue() + "' and '" + endDate.getValue() + "' " +
                "group by category";
        ResultSet resultSet = DBUtil.executeQuery(sql);
        if (resultSet != null) {
            while (resultSet.next()) {
                Map<String, Double> map = new HashMap<>();
                map.put(Bill.Category.valueOf(resultSet.getString("category")).toString(), resultSet.getDouble("total"));
                list.add(map);
            }
        }
        return list;
    }

    private void drawLineChart(XYChart.Series<String, Double> value) {
        lineChart.getData().clear();
        numberAxis.setLabel("Total");
        categoryAxis.setLabel("Date");
        lineChart.getData().add(value);
    }
}
