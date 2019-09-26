package main.java.controller;

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

public class BillPaymentsController {
    @FXML
    private VBox container;
    @FXML
    private TableView<Payment> tableView;
    @FXML
    private TableColumn<Payment, Integer> receiptNo;
    @FXML
    private Label totalPayments;
    @FXML
    private TableColumn<Payment, String> time, date, receivedBy, paymentMeans, accountAffected, options, amount;
    private String patientName;

    @FXML
    private void initialize() {
        setUpTable();
    }

    private void setUpTable() {
        receiptNo.setCellValueFactory(param -> param.getValue().receiptNumberProperty().asObject());
        amount.setCellValueFactory(param -> CurrencyUtil.getStringProperty(param.getValue().getAmount()));
        accountAffected.setCellValueFactory(param -> param.getValue().accountNameProperty());
        time.setCellValueFactory(param -> DateUtil.timeStringProperty(param.getValue().getTimeCreated()));
        date.setCellValueFactory(param -> DateUtil.dateStringProperty(param.getValue().getDateCreated()));
        receivedBy.setCellValueFactory(param -> param.getValue().receivedByProperty());
        paymentMeans.setCellValueFactory(param -> param.getValue().paymentMeansProperty());
        options.setCellFactory(param -> new TableCell<Payment, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                int index = getIndex();
                if (index >= 0 && index < tableView.getItems().size()) {
                    Button details = new Button("View Details");
                    details.getStyleClass().add("btn-info-outline");
                    details.setOnAction(event -> {
                        showPaymentDetails(tableView.getItems().get(index));
                    });
                    details.setPrefWidth(105);

                    Button print = new Button("Print Receipt");
                    print.getStyleClass().add("btn-info-outline");
                    print.setOnAction(event -> {
                        printReceipt(tableView.getItems().get(index));
                    });
                    print.setPrefWidth(105);
                    VBox vBox = new VBox(5.0, details, print);
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

    private void showPaymentDetails(Payment payment) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("main/resources/view/bill-payment-details.fxml"));
            Stage stage = new Stage();
            stage.setScene(new Scene(loader.load()));
            stage.setResizable(false);
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(container.getScene().getWindow());

            BillPaymentDetailsController controller = loader.getController();
            payment.setPatient(patientName);
            controller.setPayment(payment);

            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setBillNumber(String billNumber) {
        Task<ObservableList<Payment>> task = new Task<ObservableList<Payment>>() {
            @Override
            protected ObservableList<Payment> call() throws Exception {
                return BillingDao.getBillPayments(billNumber);
            }
        };
        task.setOnSucceeded(event -> {
            tableView.setItems(task.getValue());
            totalPayments.setText("Ksh. " + getTotalPayments());
        });
        new Thread(task).start();
    }

    private String getTotalPayments() {
        double total = 0;
        for (Payment payment : tableView.getItems()) {
            total += payment.getAmount();
        }
        return CurrencyUtil.formatCurrency(total);
    }

    public void setPatientName(String name) {
        this.patientName = name;
    }
}
