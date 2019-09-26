package main.java.model;

import javafx.beans.property.*;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Created by alfonce on 26/04/2017.
 */
public class PatientQueue {
    private IntegerProperty queueId;
    private StringProperty patientId;
    private IntegerProperty billNumber;
    private ObjectProperty<LocalDate> dateCreated;
    private ObjectProperty<LocalTime> timeCreated;
    private Status status;
    private PaymentMode paymentMode;
    private IntegerProperty doctorId;
    private ServiceType serviceType;

    public PatientQueue() {
        this.queueId = new SimpleIntegerProperty();
        this.dateCreated = new SimpleObjectProperty<>();
        this.billNumber = new SimpleIntegerProperty();
        this.patientId = new SimpleStringProperty();
        this.doctorId = new SimpleIntegerProperty();
        this.timeCreated = new SimpleObjectProperty<>();
        setStatus(Status.PENDING_TRIAGE);
    }

    public int getQueueId() {
        return queueId.get();
    }

    public void setQueueId(int queueId) {
        this.queueId.set(queueId);
    }

    public IntegerProperty queueIdProperty() {
        return queueId;
    }

    public String getPatientId() {
        return patientId.get();
    }

    public void setPatientId(String patientId) {
        this.patientId.set(patientId);
    }

    public StringProperty patientIdProperty() {
        return patientId;
    }

    public PaymentMode getPaymentMode() {
        return paymentMode;
    }

    public void setPaymentMode(PaymentMode paymentMode) {
        this.paymentMode = paymentMode;
    }

    public StringProperty paymentModeProperty() {
        if (paymentMode != null) {
            return new SimpleStringProperty(paymentMode.toString());
        }
        return null;
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

    public int getDoctorId() {
        return doctorId.get();
    }

    public void setDoctorId(int doctorId) {
        this.doctorId.set(doctorId);
    }

    public IntegerProperty doctorIdProperty() {
        return doctorId;
    }

    public ServiceType getServiceType() {
        return serviceType;
    }

    public void setServiceType(ServiceType serviceType) {
        this.serviceType = serviceType;
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

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public enum Status {
        PENDING_CONSULTATION,
        DISCHARGED,
        CONSULTATION,
        AWAITING_CONSULTATION,
        PENDING_TRIAGE,
        AWAITING_PRESCRIPTION,
        AWAITING_LAB
    }
}
