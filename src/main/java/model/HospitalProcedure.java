package main.java.model;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Created by alfonce on 20/05/2017.
 */
public class HospitalProcedure {
    private StringProperty name;
    private DoubleProperty cost;

    public HospitalProcedure() {
        this.name = new SimpleStringProperty();
        this.cost = new SimpleDoubleProperty(0.0);
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
}
