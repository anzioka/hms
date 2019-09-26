package main.java.model;

import javafx.beans.property.*;

import java.time.LocalDate;
import java.time.LocalTime;

public class LabTestResult {
    private IntegerProperty requestId;
    private StringProperty testName, result, flag, range, comment, status;
    private Specimen specimen;
    private ObjectProperty<LocalDate> dateCreated;
    private ObjectProperty<LocalTime> timeCreated;

    public LabTestResult() {
        this.testName = new SimpleStringProperty();
        this.result = new SimpleStringProperty("");
        this.flag = new SimpleStringProperty();
        this.range = new SimpleStringProperty();
        this.comment = new SimpleStringProperty();
        this.status = new SimpleStringProperty();
        this.requestId = new SimpleIntegerProperty();
        this.dateCreated = new SimpleObjectProperty<>();
        this.timeCreated = new SimpleObjectProperty<>();

    }

    public String getTestName() {
        return testName.get();
    }

    public void setTestName(String testName) {
        this.testName.set(testName);
    }

    public StringProperty testNameProperty() {
        return testName;
    }

    public String getResult() {
        return result.get();
    }

    public void setResult(String result) {
        this.result.set(result);
    }

    public StringProperty resultProperty() {
        return result;
    }

    public String getFlag() {
        return flag.get();
    }

    public void setFlag(String flag) {
        this.flag.set(flag);
    }

    public StringProperty flagProperty() {
        return flag;
    }

    public String getRange() {
        return range.get();
    }

    public void setRange(String range) {
        this.range.set(range);
    }

    public StringProperty rangeProperty() {
        return range;
    }

    public String getComment() {
        return comment.get();
    }

    public void setComment(String comment) {
        this.comment.set(comment);
    }

    public StringProperty commentProperty() {
        return comment;
    }

    public Specimen getSpecimen() {
        return specimen;
    }

    public void setSpecimen(Specimen specimen) {
        this.specimen = specimen;
    }

    public String getStatus() {
        return status.get();
    }

    public void setStatus(String status) {
        this.status.set(status);
    }

    public StringProperty statusProperty() {
        return status;
    }

    public int getRequestId() {
        return requestId.get();
    }

    public void setRequestId(int requestId) {
        this.requestId.set(requestId);
    }

    public IntegerProperty requestIdProperty() {
        return requestId;
    }

    public LocalDate getDateCreated() {
        return dateCreated.get();
    }

    public void setDateCreated(LocalDate dateCreated) {
        this.dateCreated.set(dateCreated);
    }

    public ObjectProperty<LocalDate> dateCreatedProperty() {
        return dateCreated;
    }

    public LocalTime getTimeCreated() {
        return timeCreated.get();
    }

    public void setTimeCreated(LocalTime timeCreated) {
        this.timeCreated.set(timeCreated);
    }

    public ObjectProperty<LocalTime> timeCreatedProperty() {
        return timeCreated;
    }
}
