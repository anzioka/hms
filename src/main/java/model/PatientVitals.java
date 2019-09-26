package main.java.model;

import javafx.beans.property.*;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Created by alfonce on 14/05/2017.
 */
public class PatientVitals {
    private IntegerProperty admissionNum;
    private IntegerProperty queueId;
    private DoubleProperty bmi;
    private DoubleProperty weight;
    private DoubleProperty height;
    private DoubleProperty systolicBp;
    private DoubleProperty diastolicBp;
    private DoubleProperty bodyTemp;
    private DoubleProperty respiratoryRate;
    private DoubleProperty pulseRate;
    private DoubleProperty spo2;
    private StringProperty bloodGroup;
    private StringProperty rhesusFactor;
    private StringProperty colorCode;
    private StringProperty triageNotes;
    private ObjectProperty<LocalDate> dateCreated;
    private ObjectProperty<LocalTime> timeCreated;

    public PatientVitals() {
        this.admissionNum = new SimpleIntegerProperty(-1);
        this.queueId = new SimpleIntegerProperty(-1);
        this.bmi = new SimpleDoubleProperty();
        this.weight = new SimpleDoubleProperty();
        this.height = new SimpleDoubleProperty();
        this.systolicBp = new SimpleDoubleProperty();
        this.diastolicBp = new SimpleDoubleProperty();
        this.bodyTemp = new SimpleDoubleProperty();
        this.respiratoryRate = new SimpleDoubleProperty();
        this.pulseRate = new SimpleDoubleProperty();
        this.spo2 = new SimpleDoubleProperty();
        this.bloodGroup = new SimpleStringProperty();
        this.rhesusFactor = new SimpleStringProperty();
        this.colorCode = new SimpleStringProperty("Yellow");
        this.triageNotes = new SimpleStringProperty();
        this.dateCreated = new SimpleObjectProperty<>();
        this.timeCreated = new SimpleObjectProperty<>();
    }

    public String getBloodGroup() {
        return bloodGroup.get();
    }

    public void setBloodGroup(String bloodGroup) {
        this.bloodGroup.set(bloodGroup);
    }

    public StringProperty bloodGroupProperty() {
        return bloodGroup;
    }

    public String getRhesusFactor() {
        return rhesusFactor.get();
    }

    public void setRhesusFactor(String rhesusFactor) {
        this.rhesusFactor.set(rhesusFactor);
    }

    public StringProperty rhesusFactorProperty() {
        return rhesusFactor;
    }

    public String getColorCode() {
        return colorCode.get();
    }

    public void setColorCode(String colorCode) {
        this.colorCode.set(colorCode);
    }

    public StringProperty colorCodeProperty() {
        return colorCode;
    }

    public String getTriageNotes() {
        return triageNotes.get();
    }

    public void setTriageNotes(String triageNotes) {
        this.triageNotes.set(triageNotes);
    }

    public StringProperty triageNotesProperty() {
        return triageNotes;
    }

    public int getQueueId() {
        return queueId.get();
    }

    public void setQueueId(int queueId) {
        this.queueId.set(queueId);
    }

    public IntegerProperty queueIdProperty() {
        return queueId;
    }

    public double getBmi() {
        return bmi.get();
    }

    public void setBmi(double bmi) {
        this.bmi.set(bmi);
    }

    public DoubleProperty bmiProperty() {
        return bmi;
    }

    public double getWeight() {
        return weight.get();
    }

    public void setWeight(double weight) {
        this.weight.set(weight);
    }

    public DoubleProperty weightProperty() {
        return weight;
    }

    public double getHeight() {
        return height.get();
    }

    public void setHeight(double height) {
        this.height.set(height);
    }

    public DoubleProperty heightProperty() {
        return height;
    }

    public double getSystolicBp() {
        return systolicBp.get();
    }

    public void setSystolicBp(double systolicBp) {
        this.systolicBp.set(systolicBp);
    }

    public DoubleProperty systolicBpProperty() {
        return systolicBp;
    }

    public double getDiastolicBp() {
        return diastolicBp.get();
    }

    public void setDiastolicBp(double diastolicBp) {
        this.diastolicBp.set(diastolicBp);
    }

    public DoubleProperty diastolicBpProperty() {
        return diastolicBp;
    }

    public double getBodyTemp() {
        return bodyTemp.get();
    }

    public void setBodyTemp(double bodyTemp) {
        this.bodyTemp.set(bodyTemp);
    }

    public DoubleProperty bodyTempProperty() {
        return bodyTemp;
    }

    public double getRespiratoryRate() {
        return respiratoryRate.get();
    }

    public void setRespiratoryRate(double respiratoryRate) {
        this.respiratoryRate.set(respiratoryRate);
    }

    public DoubleProperty respiratoryRateProperty() {
        return respiratoryRate;
    }

    public double getPulseRate() {
        return pulseRate.get();
    }

    public void setPulseRate(double pulseRate) {
        this.pulseRate.set(pulseRate);
    }

    public DoubleProperty pulseRateProperty() {
        return pulseRate;
    }

    public double getSpo2() {
        return spo2.get();
    }

    public void setSpo2(double spo2) {
        this.spo2.set(spo2);
    }

    public DoubleProperty spo2Property() {
        return spo2;
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
}

