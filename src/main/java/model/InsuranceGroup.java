package main.java.model;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Created by alfonce on 22/07/2017.
 */
public class InsuranceGroup {
    private StringProperty name;
    private DoubleProperty consultationFee;

    public InsuranceGroup() {
        this.name = new SimpleStringProperty();
        this.consultationFee = new SimpleDoubleProperty(0);
    }

    public double getConsultationFee() {
        return consultationFee.get();
    }

    public void setConsultationFee(double consultationFee) {
        this.consultationFee.set(consultationFee);
    }

    public DoubleProperty consultationFeeProperty() {
        return consultationFee;
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

    @Override
    public String toString() {
        return getName();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof InsuranceGroup && ((InsuranceGroup) obj).getName().equals(this.getName());
    }

    @Override
    public int hashCode() {
        return getName().hashCode();
    }
}
