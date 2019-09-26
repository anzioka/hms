package main.java.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import main.java.dao.BillingDao;
import main.java.util.DateUtil;

import java.time.LocalDate;
import java.util.Map;

import static main.java.controller.BillingController.*;

public class ReceiptsController {
    @FXML
    private TextField searchField;
    @FXML
    private TableColumn<Map<String, String>, String> receiptNo, amount, patientName, options, date;
    @FXML
    private DatePicker startDate, endDate;
    @FXML
    private VBox container;
    @FXML
    private TableView<Map<String, String>> tableView;
    private ObservableList<Map<String, String>> allPayments = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        setUpTable();
        endDate.setConverter(DateUtil.getDatePickerConverter());
        endDate.setValue(LocalDate.now());

        startDate.setConverter(DateUtil.getDatePickerConverter());
        startDate.setValue(LocalDate.now().withDayOfMonth(1));

        onSearch();
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterByName(newValue);
        });
//        DefaultJasperReportsContext context = DefaultJasperReportsContext.getInstance();
//        JRPropertiesUtil.getInstance(context).setProperty("net.sf.jasperreports.xpath.executer.factory",
//                "net.sf.jasperreports.engine.util.xml.JaxenXPathExecuterFactory");
    }

    private void filterByName(String newValue) {
        if (newValue == null || newValue.isEmpty()) {
            tableView.setItems(allPayments);
        } else {
            ObservableList<Map<String, String>> list = FXCollections.observableArrayList();
            for (Map<String, String> data : allPayments) {
                if (data.get(BillingController.PATIENT_NAME).toLowerCase().contains(newValue.toLowerCase())) {
                    list.add(data);
                }
            }
            tableView.setItems(list);
        }
    }

    private void setUpTable() {
        Label label = new Label("No receipts matching dates!");
        label.getStyleClass().add("text-danger");
        tableView.setPlaceholder(label);

        date.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().get(DATE)));
        patientName.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().get(PATIENT_NAME)));
        receiptNo.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().get(BILL_NUM)));
        amount.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().get(AMOUNT_PAID)));
        options.setCellFactory(param -> new TableCell<Map<String, String>, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                int index = getIndex();
                if (index >= 0 && index < tableView.getItems().size()) {
                    Button button = new Button("Receipt");
                    button.getStyleClass().add("btn-info-outline");
                    button.setOnAction(event -> {
                        new PrintReceipt().viewReceipt(tableView.getItems().get(index));
                    });
                    setGraphic(button);
                } else {
                    setGraphic(null);
                }
            }
        });
    }


    @FXML
    private void onSearch() {
        LocalDate start = startDate.getValue() != null ? startDate.getValue() : LocalDate.now().withDayOfMonth(1);
        LocalDate end = endDate.getValue() != null ? endDate.getValue() : LocalDate.now();
        Task<ObservableList<Map<String, String>>> task = new Task<ObservableList<Map<String, String>>>() {
            @Override
            protected ObservableList<Map<String, String>> call() throws Exception {
                return BillingDao.getReceipts(start, end);
            }
        };
        task.setOnSucceeded(event -> {
            tableView.setItems(task.getValue());
            allPayments = task.getValue();
        });
        new Thread(task).start();
    }
}
