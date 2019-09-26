package main.java.model;

import javafx.beans.property.*;

import java.time.LocalDate;
import java.time.LocalTime;

public class Inpatient extends Patient {
    private IntegerProperty admissionNumber, doctorId, wardId, bedId, billNumber;
    private StringProperty inpatientNumber, assignedWard, assignedBed;
    private ObjectProperty<LocalDate> dateAdmitted, dateDischarged;
    private ObjectProperty<LocalTime> timeAdmitted, timeDischarged;
    private boolean nhifApplicable;

    private Status status;
    private PaymentMode paymentMode;

    public Inpatient() {
        this.inpatientNumber = new SimpleStringProperty();
        this.assignedBed = new SimpleStringProperty();
        this.assignedWard = new SimpleStringProperty();
        this.dateAdmitted = new SimpleObjectProperty<>();
        this.dateDischarged = new SimpleObjectProperty<>();
        this.admissionNumber = new SimpleIntegerProperty();
        this.timeAdmitted = new SimpleObjectProperty<>();
        this.timeDischarged = new SimpleObjectProperty<>();
        this.doctorId = new SimpleIntegerProperty();
        this.bedId = new SimpleIntegerProperty();
        this.wardId = new SimpleIntegerProperty();
        this.billNumber = new SimpleIntegerProperty();
        setNhifApplicable(false);
        setStatus(Status.ADMITTED); //default
    }

    public String getInpatientNumber() {
        return inpatientNumber.get();
    }

    public void setInpatientNumber(String inpatientNumber) {
        this.inpatientNumber.set(inpatientNumber);
    }

    public StringProperty inpatientNumberProperty() {
        return inpatientNumber;
    }

    public String getAssignedWard() {
        return assignedWard.get();
    }

    public void setAssignedWard(String assignedWard) {
        this.assignedWard.set(assignedWard);
    }

    public StringProperty assignedWardProperty() {
        return assignedWard;
    }

    public String getAssignedBed() {
        return assignedBed.get();
    }

    public void setAssignedBed(String assignedBed) {
        this.assignedBed.set(assignedBed);
    }

    public StringProperty assignedBedProperty() {
        return assignedBed;
    }

    public LocalDate getDateAdmitted() {
        return dateAdmitted.get();
    }

    public void setDateAdmitted(LocalDate dateAdmitted) {
        this.dateAdmitted.set(dateAdmitted);
    }

    public ObjectProperty<LocalDate> dateAdmittedProperty() {
        return dateAdmitted;
    }

    public LocalDate getDateDischarged() {
        return dateDischarged.get();
    }

    public void setDateDischarged(LocalDate dateDischarged) {
        this.dateDischarged.set(dateDischarged);
    }

    public ObjectProperty<LocalDate> dateDischargedProperty() {
        return dateDischarged;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public int getAdmissionNumber() {
        return admissionNumber.get();
    }

    public void setAdmissionNumber(int admissionNumber) {
        this.admissionNumber.set(admissionNumber);
    }

    public IntegerProperty admissionNumberProperty() {
        return admissionNumber;
    }

    public LocalTime getTimeAdmitted() {
        return timeAdmitted.get();
    }

    public void setTimeAdmitted(LocalTime timeAdmitted) {
        this.timeAdmitted.set(timeAdmitted);
    }

    public ObjectProperty<LocalTime> timeAdmittedProperty() {
        return timeAdmitted;
    }

    public LocalTime getTimeDischarged() {
        return timeDischarged.get();
    }

    public void setTimeDischarged(LocalTime timeDischarged) {
        this.timeDischarged.set(timeDischarged);
    }

    public ObjectProperty<LocalTime> timeDischargedProperty() {
        return timeDischarged;
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

    public int getWardId() {
        return wardId.get();
    }

    public void setWardId(int wardId) {
        this.wardId.set(wardId);
    }

    public IntegerProperty wardIdProperty() {
        return wardId;
    }

    public int getBedId() {
        return bedId.get();
    }

    public void setBedId(int bedId) {
        this.bedId.set(bedId);
    }

    public IntegerProperty bedIdProperty() {
        return bedId;
    }

    public PaymentMode getPaymentMode() {
        return paymentMode;
    }

    public void setPaymentMode(PaymentMode paymentMode) {
        this.paymentMode = paymentMode;
    }

    public boolean isNhifApplicable() {
        return nhifApplicable;
    }

    public void setNhifApplicable(boolean nhifApplicable) {
        this.nhifApplicable = nhifApplicable;
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

    public enum Status {
        ADMITTED, DISCHARGED;
    }
}
