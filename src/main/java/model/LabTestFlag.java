package main.java.model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class LabTestFlag {
    private StringProperty test;
    private IntegerProperty testId;
    private StringProperty name;
    private StringProperty defaultVal;

    public LabTestFlag() {
        this.test = new SimpleStringProperty();
        this.name = new SimpleStringProperty();
        this.defaultVal = new SimpleStringProperty();
        this.testId = new SimpleIntegerProperty();
    }

    public String getTest() {
        return test.get();
    }

    public void setTest(String test) {
        this.test.set(test);
    }

    public StringProperty testProperty() {
        return test;
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

    public String getDefaultVal() {
        return defaultVal.get();
    }

    public void setDefaultVal(String defaultVal) {
        this.defaultVal.set(defaultVal);
    }

    public StringProperty defaultValProperty() {
        return defaultVal;
    }

    public int getTestId() {
        return testId.get();
    }

    public void setTestId(int testId) {
        this.testId.set(testId);
    }

    public IntegerProperty testIdProperty() {
        return testId;
    }
}
