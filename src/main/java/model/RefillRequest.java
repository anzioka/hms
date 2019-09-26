package main.java.model;

import javafx.beans.property.*;

import java.time.LocalDate;

/**
 * Created by alfonce on 07/07/2017.
 */
public class RefillRequest {
    private IntegerProperty refillId, refillerId, requesterId, amountRequested, amountReceived;
    private StringProperty medicineName, status;
    private ObjectProperty<LocalDate> dateRequested, dateServiced;

    public RefillRequest() {
        this.refillerId = new SimpleIntegerProperty();
        this.refillId = new SimpleIntegerProperty(0);
        this.requesterId = new SimpleIntegerProperty();
        this.amountReceived = new SimpleIntegerProperty();
        this.amountRequested = new SimpleIntegerProperty();
        this.medicineName = new SimpleStringProperty();
        this.status = new SimpleStringProperty("Pending");
        this.dateRequested = new SimpleObjectProperty<>();
        this.dateServiced = new SimpleObjectProperty<>();
    }

    public int getRefillId() {
        return refillId.get();
    }

    public void setRefillId(int refillId) {
        this.refillId.set(refillId);
    }

    public IntegerProperty refillIdProperty() {
        return refillId;
    }

    public int getRefillerId() {
        return refillerId.get();
    }

    public void setRefillerId(int refillerId) {
        this.refillerId.set(refillerId);
    }

    public IntegerProperty refillerIdProperty() {
        return refillerId;
    }

    public int getRequesterId() {
        return requesterId.get();
    }

    public void setRequesterId(int requesterId) {
        this.requesterId.set(requesterId);
    }

    public IntegerProperty requesterIdProperty() {
        return requesterId;
    }

    public int getAmountRequested() {
        return amountRequested.get();
    }

    public void setAmountRequested(int amountRequested) {
        this.amountRequested.set(amountRequested);
    }

    public IntegerProperty amountRequestedProperty() {
        return amountRequested;
    }

    public int getAmountReceived() {
        return amountReceived.get();
    }

    public void setAmountReceived(int amountReceived) {
        this.amountReceived.set(amountReceived);
    }

    public IntegerProperty amountReceivedProperty() {
        return amountReceived;
    }

    public String getMedicineName() {
        return medicineName.get();
    }

    public void setMedicineName(String medicineName) {
        this.medicineName.set(medicineName);
    }

    public StringProperty medicineNameProperty() {
        return medicineName;
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

    public LocalDate getDateRequested() {
        return dateRequested.get();
    }

    public void setDateRequested(LocalDate dateRequested) {
        this.dateRequested.set(dateRequested);
    }

    public ObjectProperty<LocalDate> dateRequestedProperty() {
        return dateRequested;
    }

    public LocalDate getDateServiced() {
        return dateServiced.get();
    }

    public void setDateServiced(LocalDate dateServiced) {
        this.dateServiced.set(dateServiced);
    }

    public ObjectProperty<LocalDate> dateServicedProperty() {
        return dateServiced;
    }
}
