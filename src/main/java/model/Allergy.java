package main.java.model;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.time.LocalDate;

public class Allergy {
    private StringProperty patientNumber;
    private StringProperty name;
    private ObjectProperty<LocalDate> dateAdded;

    public Allergy() {
        this.patientNumber = new SimpleStringProperty();
        this.name = new SimpleStringProperty();
        this.dateAdded = new SimpleObjectProperty<>();
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

    public String getName() {
        return name.get();
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public StringProperty nameProperty() {
        return name;
    }

    public LocalDate getDateAdded() {
        return dateAdded.get();
    }

    public void setDateAdded(LocalDate dateAdded) {
        this.dateAdded.set(dateAdded);
    }

    public ObjectProperty<LocalDate> dateAddedProperty() {
        return dateAdded;
    }
}
