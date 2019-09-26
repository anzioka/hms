package main.java.model;

import javafx.beans.property.*;

import java.time.LocalDate;
import java.time.LocalTime;

public class Appointment {
    private IntegerProperty doctorId, id;
    private StringProperty doctorName, patientId, patientName, note;
    private ObjectProperty<LocalDate> date;
    private ObjectProperty<LocalTime> time;

    public Appointment() {
        this.id = new SimpleIntegerProperty();
        this.doctorId = new SimpleIntegerProperty();
        this.doctorName = new SimpleStringProperty();
        this.patientId = new SimpleStringProperty();
        this.patientName = new SimpleStringProperty();
        this.date = new SimpleObjectProperty<>();
        this.time = new SimpleObjectProperty<>();
        this.note = new SimpleStringProperty("");

    }

    public int getDoctorId() {
        return doctorId.get();
    }

    public IntegerProperty doctorIdProperty() {
        return doctorId;
    }

    public void setDoctorId(int doctorId) {
        this.doctorId.set(doctorId);
    }

    public String getDoctorName() {
        return doctorName.get();
    }

    public StringProperty doctorNameProperty() {
        return doctorName;
    }

    public void setDoctorName(String doctorName) {
        this.doctorName.set(doctorName);
    }

    public String getPatientId() {
        return patientId.get();
    }

    public StringProperty patientIdProperty() {
        return patientId;
    }

    public void setPatientId(String patientId) {
        this.patientId.set(patientId);
    }

    public String getPatientName() {
        return patientName.get();
    }

    public StringProperty patientNameProperty() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName.set(patientName);
    }

    public LocalDate getDate() {
        return date.get();
    }

    public ObjectProperty<LocalDate> dateProperty() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date.set(date);
    }

    public LocalTime getTime() {
        return time.get();
    }

    public ObjectProperty<LocalTime> timeProperty() {
        return time;
    }

    public void setTime(LocalTime time) {
        this.time.set(time);
    }

    @Override
    public String toString() {
        return getPatientName();
    }

    public int getId() {
        return id.get();
    }

    public IntegerProperty idProperty() {
        return id;
    }

    public void setId(int id) {
        this.id.set(id);
    }

    public String getNote() {
        return note.get();
    }

    public StringProperty noteProperty() {
        return note;
    }

    public void setNote(String note) {
        this.note.set(note);
    }

}
