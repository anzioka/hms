package main.java.controller;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import main.Main;
import main.java.dao.BillingDao;
import main.java.model.Bill;
import main.java.model.Payment;
import main.java.util.AlertUtil;
import main.java.util.CurrencyUtil;
import main.java.util.DBUtil;
import main.java.util.DateUtil;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AddPaymentController {
    @FXML
    private VBox container;
    @FXML
    private TableView<Bill> tableView;
    @FXML
    private ChoiceBox<String> accountChoiceBox;
    @FXML
    private ChoiceBox<Payment.PaymentMeans> paymentMeansChoiceBox;
    @FXML
    private TableColumn<Bill, String> description, category, amountReceivedCol, outstandingAmtCol;
    @FXML
    private Label patientTypeLabel, patientName, patientNo, paymentMode, outstandingAmt;
    @FXML
    private TextField editAmount;
    private double amountPaid = 0;
    private boolean inpatient;
    private Stage stage;
    private String billNumber;
    private LocalDate date;

    @FXML
    private void initialize() {
        setUpTable();
        Platform.runLater(() -> {
            editAmount.requestFocus();
        });
        editAmount.textProperty().addListener((observable, oldValue, newValue) -> {
            allocatePayment(newValue);
        });
        paymentMeansChoiceBox.setItems(FXCollections.observableArrayList(Payment.PaymentMeans.values()));
        paymentMeansChoiceBox.getSelectionModel().select(Payment.PaymentMeans.CASH);

        getAccountNames();
    }

    private void getAccountNames() {
        Task<List<String>> task = new Task<List<String>>() {
            @Override
            protected List<String> call() throws Exception {
                List<String> list = new ArrayList<>();
                String sql = "select name from accounts";
                ResultSet resultSet = DBUtil.executeQuery(sql);
                if (resultSet != null) {
                    while (resultSet.next()) {
                        list.add(resultSet.getString("name"));
                    }
                }
                return list;
            }
        };
        task.setOnSucceeded(event -> {
            accountChoiceBox.setItems(FXCollections.observableArrayList(task.getValue()));
            accountChoiceBox.getSelectionModel().select("Cash");
        });
        new Thread(task).start();
    }

    private void allocatePayment(String newValue) {
        if (!editAmount.isFocused()) {
            return;
        }
        //reset table
        for (Bill bill : tableView.getItems()) {
            bill.setAmountPaid(0);
        }
        tableView.refresh();

        //allocate amount to bill items
        double amountReceived = CurrencyUtil.parseCurrency(newValue);
        if (amountReceived > 0) {
            for (Bill bill : tableView.getItems()) {
                double paid = amountReceived >= bill.getAmount() ? bill.getAmount() : amountReceived;
                bill.setAmountPaid(paid);
                amountReceived -= paid;
                if (amountReceived == 0) {
                    break;
                }
            }
        } else {
            for (Bill bill : tableView.getItems()) {
                bill.setAmountPaid(0);
            }
        }
        tableView.refresh();
    }

    private void setUpTable() {
        for (TableColumn column : tableView.getColumns()) {
            if (column == description) {
                column.prefWidthProperty().bind(tableView.widthProperty().divide(2.5));
            } else {
                column.prefWidthProperty().bind(tableView.widthProperty().divide(5));
            }
        }
        description.setCellValueFactory(param -> param.getValue().descriptionProperty());
        category.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getCategory().toString()));
        outstandingAmtCol.setCellValueFactory(param -> CurrencyUtil.getStringProperty(param.getValue().getAmount()));
        amountReceivedCol.setCellFactory(param -> new TableCell<Bill, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                int index = getIndex();
                if (index >= 0 && index < tableView.getItems().size()) {
                    Bill bill = tableView.getItems().get(index);
                    TextField textField = new TextField(CurrencyUtil.formatCurrency(bill.getAmountPaid()));
                    textField.textProperty().addListener((observable, oldValue, newValue) -> {
                        double formerVal = CurrencyUtil.parseCurrency(oldValue), newVal = CurrencyUtil.parseCurrency(newValue);
                        if (newVal < 0) {
                            newVal = 0;
                        }
                        if (formerVal != newVal) {
                            double amountReceived = CurrencyUtil.parseCurrency(editAmount.getText());
                            if (amountReceived < 0) {
                                amountReceived = 0;
                            }
                            editAmount.setText(CurrencyUtil.formatCurrency(amountReceived - formerVal + newVal));
                        }
                        if (newVal >= bill.getAmount()) {
                            newVal = bill.getAmount();
                        }
                        bill.setAmountPaid(newVal);
                    });
                    setGraphic(textField);
                } else {
                    setGraphic(null);
                }
            }
        });
    }

    @FXML
    private void onSavePayment() {
        if (validInput()) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("New Payment");
            alert.setContentText("Receive Ksh. " + CurrencyUtil.parseCurrency(editAmount.getText()) + " as payment for " + patientName.getText() + "'s bill?");
            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get().equals(ButtonType.OK)) {
                setAmountPaid();
                if (applyPayment()) {
                    showPaymentSummary();
                    printReceipt();
                } else {
                    AlertUtil.showAlert("New Payment", "An error occurred while attempting to apply new payment", Alert.AlertType.ERROR);
                }
            }
        }

    }

    private boolean validInput() {
        String errorMsg = "";
        if (CurrencyUtil.parseCurrency(editAmount.getText()) <= 0) {
            errorMsg += "Invalid amount \n";
        }
        if (accountChoiceBox.getValue() == null) {
            errorMsg += "Specify the account in which the payment will be deposited\n";
        }
        if (paymentMeansChoiceBox.getValue() == null) {
            errorMsg += "Select mode of payment\n";
        }
        if (errorMsg.isEmpty()) {
            return true;
        }
        AlertUtil.showAlert("Error(s)", errorMsg, Alert.AlertType.ERROR);
        return false;
    }

    private void printReceipt() {
        //TODO : print receipt in the background
    }

    private void showPaymentSummary() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("main/resources/view/payment-summary.fxml"));
            Stage stage = new Stage();
            stage.setScene(new Scene(loader.load()));
            stage.setTitle("Payment Summary");
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(container.getScene().getWindow());
            stage.setResizable(false);
            PaymentSummaryController controller = loader.getController();
            controller.setBalance(CurrencyUtil.parseCurrency(editAmount.getText()) - amountPaid);
            container.setDisable(true);
            stage.showAndWait();
            this.stage.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setAmountPaid() {
        amountPaid = 0;
        for (Bill bill : tableView.getItems()) {
            amountPaid += bill.getAmountPaid();
        }
    }

    private boolean applyPayment() {
        return DBUtil.updatePatientBill(tableView.getItems()) && DBUtil.savePayment(createPayment());
    }

    private List<Payment> createPayment() {
        List<Payment> payments = new ArrayList<>();
        int receiptNo = BillingDao.getNextReceiptNumber();
        LocalTime localTime = LocalTime.now();
        for (Bill bill : tableView.getItems()) {
            if (bill.getAmountPaid() > 0) {
                Payment payment = new Payment();
                payment.setBillId(bill.getId());
                payment.setReceivedBy(Main.currentUser.getFirstName() + " " + Main.currentUser.getLastName());
                payment.setTimeCreated(localTime);
                payment.setDateCreated(date);
                payment.setPaymentMeans(paymentMeansChoiceBox.getValue());
                payment.setReceiptNumber(receiptNo);
                payment.setAccountName(accountChoiceBox.getValue());
                payment.setBillNumber(Integer.parseInt(billNumber));
                payment.setAmount(bill.getAmountPaid());
                payment.setDescription(bill.getDescription());
                payment.setCategory(bill.getCategory().name());
                payment.setPatient(patientName.getText());
                payments.add(payment);
            }
        }

        return payments;
    }

    public void setParameters(String name, String paymentMode, String patientId, String billNum, String amount, boolean inpatient, String date) {
        this.inpatient = inpatient;
        if (!inpatient) {
            patientTypeLabel.setText("Outpatient No.");
            patientNo.setText(patientId);
        } else {
            patientNo.setText(getInpatientNo(patientId));
        }
        this.date = DateUtil.parseDate(date);
        this.billNumber = billNum;
        this.paymentMode.setText(paymentMode);
        outstandingAmt.setText(amount);
        patientName.setText(name);
        getBillDetails(billNum);
    }

    private String getInpatientNo(String patientId) {
        String sql = "select inpatient_num from inpatients where patient_id = '" + patientId + "'";
        ResultSet resultSet = DBUtil.executeQuery(sql);
        try {
            if (resultSet != null && resultSet.next()) {
                return resultSet.getString("inpatient_num");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void getBillDetails(String billNum) {
        Task<ObservableList<Bill>> task = new Task<ObservableList<Bill>>() {
            @Override
            protected ObservableList<Bill> call() {
                return BillingDao.getPatientBill(billNum, inpatient);
            }
        };
        task.setOnSucceeded(event -> {
            tableView.setItems(task.getValue());
        });
        new Thread(task).start();

    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    double getAmountPaid() {
        return amountPaid;
    }
}
