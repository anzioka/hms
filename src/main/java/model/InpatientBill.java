package main.java.model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

public class InpatientBill extends Bill {
    private IntegerProperty admissionNumber;

    public InpatientBill() {
        this.admissionNumber = new SimpleIntegerProperty();
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
}
