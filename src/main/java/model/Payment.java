package main.java.model;

import javafx.beans.property.*;

import java.time.LocalDate;
import java.time.LocalTime;

public class Payment {
    private IntegerProperty billId;
    private PaymentMeans paymentMeans;
    private StringProperty accountName, receivedBy, description, category, patient;
    private DoubleProperty amount;
    private IntegerProperty userId, billNumber, receiptNumber;
    private ObjectProperty<LocalDate> dateCreated;
    private ObjectProperty<LocalTime> timeCreated;

    public Payment() {
        this.billId = new SimpleIntegerProperty();
        this.description = new SimpleStringProperty();
        this.category = new SimpleStringProperty();
        this.accountName = new SimpleStringProperty();
        this.receivedBy = new SimpleStringProperty();
        this.amount = new SimpleDoubleProperty();
        this.userId = new SimpleIntegerProperty();
        this.billNumber = new SimpleIntegerProperty();
        this.dateCreated = new SimpleObjectProperty<>();
        this.timeCreated = new SimpleObjectProperty<>();
        this.receiptNumber = new SimpleIntegerProperty();
        this.patient = new SimpleStringProperty();
        setPaymentMeans(PaymentMeans.CASH);
    }

    public PaymentMeans getPaymentMeans() {
        return paymentMeans;
    }

    public void setPaymentMeans(PaymentMeans paymentMeans) {
        this.paymentMeans = paymentMeans;
    }

    public String getAccountName() {
        return accountName.get();
    }

    public void setAccountName(String accountName) {
        this.accountName.set(accountName);
    }

    public StringProperty accountNameProperty() {
        return accountName;
    }

    public String getReceivedBy() {
        return receivedBy.get();
    }

    public void setReceivedBy(String receivedBy) {
        this.receivedBy.set(receivedBy);
    }

    public StringProperty receivedByProperty() {
        return receivedBy;
    }

    public double getAmount() {
        return amount.get();
    }

    public void setAmount(double amount) {
        this.amount.set(amount);
    }

    public DoubleProperty amountProperty() {
        return amount;
    }

    public int getUserId() {
        return userId.get();
    }

    public void setUserId(int userId) {
        this.userId.set(userId);
    }

    public IntegerProperty userIdProperty() {
        return userId;
    }

    public int getBillNumber() {
        return billNumber.get();
    }

    public void setBillNumber(int billNumber) {
        this.billNumber.set(billNumber);
    }

    public IntegerProperty billNumberProperty() {
        return billNumber;
    }

    public LocalDate getDateCreated() {
        return dateCreated.get();
    }

    public void setDateCreated(LocalDate dateCreated) {
        this.dateCreated.set(dateCreated);
    }

    public ObjectProperty<LocalDate> dateCreatedProperty() {
        return dateCreated;
    }

    public LocalTime getTimeCreated() {
        return timeCreated.get();
    }

    public void setTimeCreated(LocalTime timeCreated) {
        this.timeCreated.set(timeCreated);
    }

    public ObjectProperty<LocalTime> timeCreatedProperty() {
        return timeCreated;
    }

    public int getReceiptNumber() {
        return receiptNumber.get();
    }

    public void setReceiptNumber(int receiptNumber) {
        this.receiptNumber.set(receiptNumber);
    }

    public IntegerProperty receiptNumberProperty() {
        return receiptNumber;
    }

    public StringProperty paymentMeansProperty() {
        return new SimpleStringProperty(getPaymentMeans().toString());
    }

    public String getDescription() {
        return description.get();
    }

    public void setDescription(String description) {
        this.description.set(description);
    }

    public StringProperty descriptionProperty() {
        return description;
    }

    public String getCategory() {
        return category.get();
    }

    public void setCategory(String category) {
        this.category.set(category);
    }

    public StringProperty categoryProperty() {
        return category;
    }

    public String getPatient() {
        return patient.get();
    }

    public void setPatient(String patient) {
        this.patient.set(patient);
    }

    public StringProperty patientProperty() {
        return patient;
    }

    public int getBillId() {
        return billId.get();
    }

    public void setBillId(int billId) {
        this.billId.set(billId);
    }

    public IntegerProperty billIdProperty() {
        return billId;
    }

    public enum PaymentMeans {
        CASH, CHEQUE, MPESA
    }
}
