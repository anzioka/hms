package main.java.model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Created by alfonce on 19/05/2017.
 */
public class Prescription {
    private IntegerProperty visitId, admissionNum, id, drugId, quantity, duration;
    private ObjectProperty<LocalDate> dateCreated;
    private ObjectProperty<LocalTime> timeCreated;
    private Status status;
    private Dosage dosage;
    private Formulation formulation;

    public Prescription() {
        this.admissionNum = new SimpleIntegerProperty(-1);
        this.id = new SimpleIntegerProperty(0);
        this.visitId = new SimpleIntegerProperty(-1);
        this.quantity = new SimpleIntegerProperty();
        this.duration = new SimpleIntegerProperty();
        this.drugId = new SimpleIntegerProperty();
        this.dateCreated = new SimpleObjectProperty<>();
        this.timeCreated = new SimpleObjectProperty<>();
        setStatus(Status.PENDING);
    }

    public int getVisitId() {
        return visitId.get();
    }

    public void setVisitId(int visitId) {
        this.visitId.set(visitId);
    }

    public IntegerProperty visitIdProperty() {
        return visitId;
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

    public int getId() {
        return id.get();
    }

    public void setId(int id) {
        this.id.set(id);
    }

    public IntegerProperty idProperty() {
        return id;
    }

    public int getAdmissionNum() {
        return admissionNum.get();
    }

    public void setAdmissionNum(int admissionNum) {
        this.admissionNum.set(admissionNum);
    }

    public IntegerProperty admissionNumProperty() {
        return admissionNum;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Dosage getDosage() {
        return dosage;
    }

    public void setDosage(Dosage dosage) {
        this.dosage = dosage;
    }

    public void setDurationFromDosage() {
        setDuration(getQuantity() / getDosage().getFactor());
    }

    public void setQuantityFromDosage() {
        setQuantity(getDuration() * getDosage().getFactor());
    }

    public int getDuration() {
        return duration.get();
    }

    public void setDuration(int duration) {
        this.duration.set(duration);
    }

    public IntegerProperty durationProperty() {
        return duration;
    }

    public int getDrugId() {
        return drugId.get();
    }

    public void setDrugId(int drugId) {
        this.drugId.set(drugId);
    }

    public IntegerProperty drugIdProperty() {
        return drugId;
    }

    public Formulation getFormulation() {
        return formulation;
    }

    public void setFormulation(Formulation formulation) {
        this.formulation = formulation;
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

    public enum Status {
        PENDING, COMPLETED;
    }

    public enum Dosage {
        OD("o.d", 1), BD("b.d", 2), TID("t.i.d", 3), QID("q.i.d", 4);

        private String dosage;
        private int factor;

        Dosage(String dosage, int factor) {
            this.dosage = dosage;
            this.factor = factor;
        }

        @Override
        public String toString() {
            return dosage;
        }

        public int getFactor() {
            return factor;
        }

    }
}
