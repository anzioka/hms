package main.java.model;

import javafx.beans.property.*;

import java.time.LocalDate;

/**
 * Created by alfonce on 19/07/2017.
 */
public class MedicalRecord {
    private IntegerProperty visitId;
    private ObjectProperty<LocalDate> date;
    private StringProperty doctorName;

    public MedicalRecord() {
        this.visitId = new SimpleIntegerProperty();
        this.date = new SimpleObjectProperty<>();
        this.doctorName = new SimpleStringProperty();
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

    public LocalDate getDate() {
        return date.get();
    }

    public void setDate(LocalDate date) {
        this.date.set(date);
    }

    public ObjectProperty<LocalDate> dateProperty() {
        return date;
    }

    public String getDoctorName() {
        return doctorName.get();
    }

    public void setDoctorName(String doctorName) {
        this.doctorName.set(doctorName);
    }

    public StringProperty doctorNameProperty() {
        return doctorName;
    }
}
