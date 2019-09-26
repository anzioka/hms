package main.java.model;

import javafx.beans.property.*;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Created by alfonce on 19/06/2017.
 */
public class LabRequest extends LabTest {
    private IntegerProperty id, queueNum, admissionNum;
    private Specimen specimen;
    private Status status;
    private StringProperty result, patientId;
    private ObjectProperty<LocalTime> timeCreated;
    private ObjectProperty<LocalDate> dateCreated;

    public LabRequest() {
        super();
        this.id = new SimpleIntegerProperty(0);
        setStatus(Status.PENDING);
        this.patientId = new SimpleStringProperty();
        this.timeCreated = new SimpleObjectProperty<>();
        this.result = new SimpleStringProperty();
        this.queueNum = new SimpleIntegerProperty(-1);
        this.admissionNum = new SimpleIntegerProperty(-1);
        this.dateCreated = new SimpleObjectProperty<>();
    }

    public int getId() {
        return id.get();
    }

    public void setId(int id) {
        this.id.set(id);
    }

    public IntegerProperty idProperty() {
        return id;
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

    public Specimen getSpecimen() {
        return specimen;
    }

    public void setSpecimen(Specimen specimen) {
        this.specimen = specimen;
    }

    public StringProperty specimenStringProperty() {
        if (getSpecimen() != null) {
            return new SimpleStringProperty(this.specimen.toString());
        }
        return null;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public StringProperty statusProperty() {
        return new SimpleStringProperty(getStatus().toString());
    }

    public int getQueueNum() {
        return queueNum.get();
    }

    public void setQueueNum(int queueNum) {
        this.queueNum.set(queueNum);
    }

    public IntegerProperty queueNumProperty() {
        return queueNum;
    }

    public int getAdmissionNum() {
        return admissionNum.get();
    }

    public void setAdmissionNum(int admissionNum) {
        this.admissionNum.set(admissionNum);
    }

    public IntegerProperty admissionNumProperty() {
        return admissionNum;
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

    public LocalDate getDateCreated() {
        return dateCreated.get();
    }

    public void setDateCreated(LocalDate dateCreated) {
        this.dateCreated.set(dateCreated);
    }

    public ObjectProperty<LocalDate> dateCreatedProperty() {
        return dateCreated;
    }

    public String getPatientId() {
        return patientId.get();
    }

    public void setPatientId(String patientId) {
        this.patientId.set(patientId);
    }

    public StringProperty patientIdProperty() {
        return patientId;
    }

    public enum Status {
        PENDING, COMPLETED;
    }
}
