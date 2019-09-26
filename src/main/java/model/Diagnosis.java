package main.java.model;

import javafx.beans.property.*;

import java.time.LocalDate;

public class Diagnosis extends ICD10_Diagnosis {
    private IntegerProperty id, visitId, admissionNum;
    private StringProperty user;
    private ObjectProperty<LocalDate> dateCreated;
    private IntegerProperty userId;

    public Diagnosis() {
        this.visitId = new SimpleIntegerProperty(-1);
        this.id = new SimpleIntegerProperty();
        this.admissionNum = new SimpleIntegerProperty(-1);
        this.dateCreated = new SimpleObjectProperty<>();
        this.userId = new SimpleIntegerProperty();
        this.user = new SimpleStringProperty();
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

    public int getAdmissionNum() {
        return admissionNum.get();
    }

    public void setAdmissionNum(int admissionNum) {
        this.admissionNum.set(admissionNum);
    }

    public IntegerProperty admissionNumProperty() {
        return admissionNum;
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

    public int getUserId() {
        return userId.get();
    }

    public void setUserId(int userId) {
        this.userId.set(userId);
    }

    public IntegerProperty userIdProperty() {
        return userId;
    }

    public String getUser() {
        return user.get();
    }

    public void setUser(String user) {
        this.user.set(user);
    }

    public StringProperty userProperty() {
        return user;
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

}
