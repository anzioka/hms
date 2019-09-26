package main.java.model;

import javafx.beans.property.*;

/**
 * Created by alfonce on 03/06/2017.
 */
public class LabTest {
    private IntegerProperty testId;
    private StringProperty name;
    private DoubleProperty cost;

    public LabTest() {
        this.name = new SimpleStringProperty();
        this.cost = new SimpleDoubleProperty();
        this.testId = new SimpleIntegerProperty();
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

    public double getCost() {
        return cost.get();
    }

    public void setCost(double cost) {
        this.cost.set(cost);
    }

    public DoubleProperty costProperty() {
        return cost;
    }

    @Override
    public String toString() {
        return getName();
    }

    @Override
    public int hashCode() {
        return getName().hashCode();
    }

    public int getTestId() {
        return testId.get();
    }

    public void setTestId(int id) {
        this.testId.set(id);
    }

    public IntegerProperty testIdProperty() {
        return testId;
    }
}

