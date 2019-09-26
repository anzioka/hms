package main.java.model;

import javafx.beans.property.*;

import java.time.LocalDate;
import java.time.LocalTime;

public class InpatientVisit {
    private StringProperty notes, userName;
    private ObjectProperty<LocalDate> dateCreated;
    private ObjectProperty<LocalTime> timeCreated;
    private IntegerProperty userId, visitId, admissionNum;
    private Category category;

    public InpatientVisit() {
        this.notes = new SimpleStringProperty();
        this.userId = new SimpleIntegerProperty();
        this.userName = new SimpleStringProperty();
        this.dateCreated = new SimpleObjectProperty<>();
        this.timeCreated = new SimpleObjectProperty<>();
        this.admissionNum = new SimpleIntegerProperty();
        this.visitId = new SimpleIntegerProperty();
    }

    public String getNotes() {
        return notes.get();
    }

    public void setNotes(String notes) {
        this.notes.set(notes);
    }

    public StringProperty notesProperty() {
        return notes;
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

    public int getVisitId() {
        return visitId.get();
    }

    public void setVisitId(int visitId) {
        this.visitId.set(visitId);
    }

    public IntegerProperty visitIdProperty() {
        return visitId;
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

    public int getUserId() {
        return userId.get();
    }

    public void setUserId(int userId) {
        this.userId.set(userId);
    }

    public IntegerProperty userIdProperty() {
        return userId;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public String getUserName() {
        return userName.get();
    }

    public void setUserName(String userName) {
        this.userName.set(userName);
    }

    public StringProperty userNameProperty() {
        return userName;
    }

    public enum Category {
        DOCTOR, NURSE;
    }
}
