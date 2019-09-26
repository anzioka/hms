package main.java.model;

import javafx.beans.property.*;

/**
 * Created by alfonce on 11/07/2017.
 */
public class Insurance {
    private StringProperty name;
    private StringProperty group;
    private DoubleProperty consultationFee;
    private BooleanProperty isAssigned;

    public Insurance() {
        this.name = new SimpleStringProperty();
        this.group = new SimpleStringProperty();
        this.consultationFee = new SimpleDoubleProperty(0);
        this.isAssigned = new SimpleBooleanProperty();

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

    public String getGroup() {
        return group.get();
    }

    public void setGroup(String group) {
        this.group.set(group);
    }

    public StringProperty groupProperty() {
        return group;
    }

    public boolean isIsAssigned() {
        return isAssigned.get();
    }

    public void setIsAssigned(boolean isAssigned) {
        this.isAssigned.set(isAssigned);
    }

    public BooleanProperty isAssignedProperty() {
        return isAssigned;
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

    @Override
    public String toString() {
        return getName();
    }
}
