package main.java.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import main.java.dao.BillingDao;
import main.java.model.Payment;
import main.java.util.CurrencyUtil;
import main.java.util.DateUtil;

import java.sql.SQLException;

public class BillPaymentDetailsController {
    @FXML
    private Label receiptNo, date, seller, total, patientName, time;
    @FXML
    private TableView<Payment> tableView;
    @FXML
    private TableColumn<Payment, String> description, category, amount;
    private Payment payment;

    @FXML
    private void initialize() {
        setUpTable();
    }

    private void setUpTable() {
        description.prefWidthProperty().bind(tableView.widthProperty().divide(2));
        category.prefWidthProperty().bind(tableView.widthProperty().divide(4));
        amount.prefWidthProperty().bind(tableView.widthProperty().divide(4));

        description.setCellValueFactory(param -> param.getValue().descriptionProperty());
        category.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getCategory()));
        amount.setCellValueFactory(param -> CurrencyUtil.getStringProperty(param.getValue().getAmount()));
    }

    void setPayment(Payment payment) {
        this.payment = payment;
        getPaymentDetails();
    }

    private void getPaymentDetails() {
        Task<ObservableList<Payment>> task = new Task<ObservableList<Payment>>() {
            @Override
            protected ObservableList<Payment> call() throws SQLException {
                return BillingDao.getPaymentDetails(payment.getReceiptNumber());
            }
        };
        task.setOnSucceeded(event -> {
            tableView.setItems(task.getValue());
            setDetails();
        });
        new Thread(task).start();
    }

    private void setDetails() {
        receiptNo.setText(payment.getReceiptNumber() + "");
        date.setText(DateUtil.formatDateLong(payment.getDateCreated()));
        time.setText(DateUtil.formatTime(payment.getTimeCreated()));
        seller.setText(payment.getReceivedBy());
        total.setText("Ksh. " + CurrencyUtil.formatCurrency(getTotal()));
        patientName.setText(payment.getPatient());
    }

    private double getTotal() {
        double total = 0;
        for (Payment payment : tableView.getItems()) {
            total += payment.getAmount();
        }
        return total;
    }
}
