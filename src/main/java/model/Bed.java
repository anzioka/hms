package main.java.model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Bed {
    private IntegerProperty bedId;
    private StringProperty label;
    private BedStatus bedStatus;

    public Bed() {
        this.bedId = new SimpleIntegerProperty();
        this.label = new SimpleStringProperty();
        setBedStatus(BedStatus.EMPTY); //default
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

    public String getLabel() {
        return label.get();
    }

    public void setLabel(String label) {
        this.label.set(label);
    }

    public StringProperty labelProperty() {
        return label;
    }

    public BedStatus getBedStatus() {
        return bedStatus;
    }

    public void setBedStatus(BedStatus bedStatus) {
        this.bedStatus = bedStatus;
    }

    public enum BedStatus {
        OCCUPIED, EMPTY;
    }
}
