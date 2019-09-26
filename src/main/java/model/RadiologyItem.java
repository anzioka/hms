package main.java.model;

import javafx.beans.property.*;
import main.java.model.*;
import main.java.util.*;

public class RadiologyItem {
    private IntegerProperty id;
    private StringProperty description;
    private StringProperty category;
    private DoubleProperty cost;

    public RadiologyItem() {
        this.id = new SimpleIntegerProperty();
        this.description = new SimpleStringProperty();
        this.cost = new SimpleDoubleProperty();
        this.category = new SimpleStringProperty();
    }

    public String getDescription() {
        return description.get();
    }

    public StringProperty descriptionProperty() {
        return description;
    }

    public void setDescription(String description) {
        this.description.set(description);
    }

    public double getCost() {
        return cost.get();
    }

    public DoubleProperty costProperty() {
        return cost;
    }

    public void setCost(double cost) {
        this.cost.set(cost);
    }

    public String getCategory() {
        return category.get();
    }

    public StringProperty categoryProperty() {
        return category;
    }

    public void setCategory(String category) {
        this.category.set(category);
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
}
