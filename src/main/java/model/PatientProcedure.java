package main.java.model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Created by alfonce on 20/05/2017.
 */
public class PatientProcedure extends HospitalProcedure {
    private IntegerProperty visitId, admissionNum, userId;
    private IntegerProperty id;
    private StringProperty userName;

    public PatientProcedure() {
        super();
        this.id = new SimpleIntegerProperty();
        this.visitId = new SimpleIntegerProperty(-1);
        this.admissionNum = new SimpleIntegerProperty(-1);
        this.userId = new SimpleIntegerProperty();
        this.userName = new SimpleStringProperty();
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

    public int getUserId() {
        return userId.get();
    }

    public void setUserId(int userId) {
        this.userId.set(userId);
    }

    public IntegerProperty userIdProperty() {
        return userId;
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

}
