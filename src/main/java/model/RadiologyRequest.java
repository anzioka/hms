package main.java.model;

import javafx.beans.property.*;

import java.time.LocalDate;
import java.time.LocalTime;

public class RadiologyRequest extends RadiologyItem {
    private IntegerProperty requestId, admissionId, visitId;
    private ObjectProperty<LocalDate> dateCreated;
    private ObjectProperty<LocalTime> timeCreated;
    private StringProperty result, patientId;
    private Status status;

    public RadiologyRequest() {
        this.requestId = new SimpleIntegerProperty();
        this.dateCreated = new SimpleObjectProperty<>();
        this.timeCreated = new SimpleObjectProperty<>();
        this.result = new SimpleStringProperty();
        this.admissionId = new SimpleIntegerProperty(-1);
        this.visitId = new SimpleIntegerProperty(-1);
        this.patientId = new SimpleStringProperty();
        setStatus(Status.PENDING);
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


    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public StringProperty statusStringProperty() {
        return new SimpleStringProperty(getStatus().toString());
    }

    public String getResult() {
        return result.get();
    }

    public StringProperty resultProperty() {
        return result;
    }

    public void setResult(String result) {
        this.result.set(result);
    }

    public int getAdmissionId() {
        return admissionId.get();
    }

    public IntegerProperty admissionIdProperty() {
        return admissionId;
    }

    public void setAdmissionId(int admissionId) {
        this.admissionId.set(admissionId);
    }

    public int getVisitId() {
        return visitId.get();
    }

    public IntegerProperty visitIdProperty() {
        return visitId;
    }

    public void setVisitId(int visitId) {
        this.visitId.set(visitId);
    }

    public String getPatientId() {
        return patientId.get();
    }

    public StringProperty patientIdProperty() {
        return patientId;
    }

    public void setPatientId(String patientId) {
        this.patientId.set(patientId);
    }

    public enum Status {
        PENDING("Pending"), COMPLETED("Completed");
        private String status;

        Status(String status) {
            this.status = status;
        }

        @Override
        public String toString() {
            return this.status;
        }
    }
}
