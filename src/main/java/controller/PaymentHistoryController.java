package main.java.controller;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import main.java.dao.BillingDao;
import main.java.model.Payment;
import main.java.util.CurrencyUtil;
import main.java.util.DateUtil;

import java.io.IOException;
import java.time.LocalDate;

public class PaymentHistoryController {
    private ObservableList<Payment> allPayments = FXCollections.observableArrayList();
    @FXML
    private VBox container;
    @FXML
    private DatePicker startDate, endDate;
    @FXML
    private TableView<Payment> tableView;
    @FXML
    private TableColumn<Payment, String> date, time, patientName, amount, paymentMode, options;
    @FXML
    private TableColumn<Payment, Integer> receiptNo;
    @FXML
    private TextField searchField;

    @FXML
    private void initialize() {
        startDate.setConverter(DateUtil.getDatePickerConverter());
        endDate.setConverter(DateUtil.getDatePickerConverter());

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterResults(newValue);
        });
        setUpTable();
        Platform.runLater(this::onSearch);
    }

    private void setUpTable() {
        Label label = new Label("No payments matching the search criteria");
        label.getStyleClass().add("text-danger");
        tableView.setPlaceholder(label);

        receiptNo.setCellValueFactory(param -> param.getValue().receiptNumberProperty().asObject());
        date.setCellValueFactory(param -> DateUtil.dateStringProperty(param.getValue().getDateCreated()));
        time.setCellValueFactory(param -> DateUtil.timeStringProperty(param.getValue().getTimeCreated()));
        patientName.setCellValueFactory(param -> param.getValue().patientProperty());
        amount.setCellValueFactory(param -> CurrencyUtil.getStringProperty(param.getValue().getAmount()));
        paymentMode.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getPaymentMeans().toString()));
        options.setCellFactory(param -> new TableCell<Payment, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                int index = getIndex();
                if (index >= 0 && index < tableView.getItems().size()) {
                    Button viewDetails = new Button("View Details");
                    viewDetails.setPrefWidth(105);
                    viewDetails.setOnAction(event -> {
                        viewPaymentDetails(tableView.getItems().get(index));
                    });
                    viewDetails.getStyleClass().add("btn-info-outline");

                    Button receipt = new Button("Print Receipt");
                    receipt.setPrefWidth(105);
                    receipt.setOnAction(event -> {
                        printReceipt(tableView.getItems().get(index));
                    });
                    receipt.getStyleClass().add("btn-info-outline");

                    VBox vBox = new VBox(5.0, viewDetails, receipt);
                    vBox.setAlignment(Pos.CENTER);
                    setGraphic(vBox);
                } else {
                    setGraphic(null);
                }
            }
        });
    }

    private void printReceipt(Payment payment) {
        //TODO : print receipt
    }

    private void viewPaymentDetails(Payment payment) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("main/resources/view/bill-payment-details.fxml"));
            Stage stage = new Stage();
            stage.setScene(new Scene(loader.load()));
            stage.setResizable(false);
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(container.getScene().getWindow());

            BillPaymentDetailsController controller = loader.getController();
            controller.setPayment(payment);

            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void filterResults(String newValue) {
        if (newValue == null || newValue.isEmpty()) {
            tableView.setItems(allPayments);
        } else {
            ObservableList<Payment> filtered = FXCollections.observableArrayList();
            for (Payment payment : allPayments) {
                if (payment.getPatient().toLowerCase().contains(newValue.toLowerCase())) {
                    filtered.add(payment);
                }
            }
            tableView.setItems(filtered);
        }
    }

    @FXML
    private void onSearch() {
        LocalDate start = startDate.getValue() != null ? startDate.getValue() : LocalDate.now();
        startDate.setValue(start);

        LocalDate end = endDate.getValue() != null ? endDate.getValue() : LocalDate.now();
        endDate.setValue(end);

        Task<ObservableList<Payment>> task = new Task<ObservableList<Payment>>() {
            @Override
            protected ObservableList<Payment> call() throws Exception {
                return BillingDao.getPaymentsForPeriod(start, end);
            }
        };
        task.setOnSucceeded(event -> {
            tableView.setItems(task.getValue());
        });
        new Thread(task).start();
    }
}
