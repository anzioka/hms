package main.java.model;

import javafx.beans.property.*;

import java.time.LocalDate;

public class Bill {
    private IntegerProperty billNumber, queueNumber, admissionNumber, id;
    //the same patient may change insurance company
    private StringProperty patientNumber, insuranceId, insurer, description;
    private Category category;
    private Status status;
    private DoubleProperty amount, amountPaid;
    private ObjectProperty<LocalDate> dateCreated;

    public Bill() {
        this.patientNumber = new SimpleStringProperty();
        this.id = new SimpleIntegerProperty(0);
        this.amountPaid = new SimpleDoubleProperty();
        this.insuranceId = new SimpleStringProperty();
        this.billNumber = new SimpleIntegerProperty();
        this.insurer = new SimpleStringProperty();
        this.description = new SimpleStringProperty();
        this.dateCreated = new SimpleObjectProperty<>();
        this.amount = new SimpleDoubleProperty();
        this.queueNumber = new SimpleIntegerProperty(-1);
        this.admissionNumber = new SimpleIntegerProperty(-1);
        setStatus(Status.PENDING);
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

    public String getPatientNumber() {
        return patientNumber.get();
    }

    public void setPatientNumber(String patientNumber) {
        this.patientNumber.set(patientNumber);
    }

    public StringProperty patientNumberProperty() {
        return patientNumber;
    }

    public String getInsuranceId() {
        return insuranceId.get();
    }

    public void setInsuranceId(String insuranceId) {
        this.insuranceId.set(insuranceId);
    }

    public StringProperty insuranceIdProperty() {
        return insuranceId;
    }

    public String getInsurer() {
        return insurer.get();
    }

    public void setInsurer(String insurer) {
        this.insurer.set(insurer);
    }

    public StringProperty insurerProperty() {
        return insurer;
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

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public int getQueueNumber() {
        return queueNumber.get();
    }

    public void setQueueNumber(int queueNumber) {
        this.queueNumber.set(queueNumber);
    }

    public IntegerProperty queueNumberProperty() {
        return queueNumber;
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

    public int getId() {
        return id.get();
    }

    public void setId(int id) {
        this.id.set(id);
    }

    public IntegerProperty idProperty() {
        return id;
    }

    public double getAmountPaid() {
        return amountPaid.get();
    }

    public void setAmountPaid(double amountPaid) {
        this.amountPaid.set(amountPaid);
    }

    public DoubleProperty amountPaidProperty() {
        return amountPaid;
    }

    public enum Category {
        ALL("All payments"), CONSULTATION("Consultation Charges"), LAB("Lab Charges"), RADIOLOGY("Radiology"), OPERATION("Operation"), ADMISSION("Admission"), NURSING_CHARGES("Nursing Charges"), DOCTOR_CHARGES("Doctor Charges"), OTHER("Other"), PROCEDURE("Procedure"), BED_CHARGES("Bed Charges"), MEDICATION("Medication");

        private String category;

        Category(String category) {
            this.category = category;
        }

        @Override
        public String toString() {
            return category;
        }
    }

    public enum Status {
        PAID, PENDING;
    }

}
