package main.java.model;

import javafx.beans.property.*;

import java.time.LocalDate;

/**
 * Created by alfonce on 08/05/2017.
 */
public class CashSale {
    private IntegerProperty id;
    private IntegerProperty billNumber;
    private DoubleProperty amount;
    private StringProperty description;
    private StringProperty status;
    private StringProperty category;
    private ObjectProperty<LocalDate> dateCreated;
    private IntegerProperty queueId;
    private IntegerProperty quantity;

    public CashSale() {
        this.id = new SimpleIntegerProperty();
        this.billNumber = new SimpleIntegerProperty();
        this.queueId = new SimpleIntegerProperty();
        this.amount = new SimpleDoubleProperty();
        this.category = new SimpleStringProperty();
        this.dateCreated = new SimpleObjectProperty<>();
        this.description = new SimpleStringProperty();
        this.status = new SimpleStringProperty("Payable");
        this.quantity = new SimpleIntegerProperty(1);
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

    public LocalDate getDateCreated() {
        return dateCreated.get();
    }

    public void setDateCreated(LocalDate dateCreated) {
        this.dateCreated.set(dateCreated);
    }

    public ObjectProperty<LocalDate> dateCreatedProperty() {
        return dateCreated;
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

    public int getQueueId() {
        return queueId.get();
    }

    public void setQueueId(int patientId) {
        this.queueId.set(patientId);
    }

    public IntegerProperty queueIdProperty() {
        return queueId;
    }

    public String getStatus() {
        return status.get();
    }

    public void setStatus(String status) {
        this.status.set(status);
    }

    public StringProperty statusProperty() {
        return status;
    }

    public int getId() {
        return id.get();
    }

    public void setId(int id) {
        this.id.set(id);
    }

    public IntegerProperty idProperty() {
        return id;
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

    @Override
    public boolean equals(Object object) {
        boolean isEqual = false;
        if (object != null && object instanceof CashSale) {
            isEqual = (this.getId() == ((CashSale) object).getId());
        }
        return isEqual;
    }

    @Override
    public int hashCode() {
        return this.getId();
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

    public int getQuantity() {
        return quantity.get();
    }

    public void setQuantity(int quantity) {
        this.quantity.set(quantity);
    }

    public IntegerProperty quantityProperty() {
        return quantity;
    }
}
