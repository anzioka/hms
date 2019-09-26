package main.java.model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Created by alfonce on 19/05/2017.
 */
public class ClinicVisitNotes {
    private IntegerProperty visitId;
    private StringProperty patientId;
    private StringProperty primaryComplains;
    private StringProperty medicalHistory;
    private StringProperty physicalExam;
    private StringProperty investigation;
    private StringProperty treatment;

    public ClinicVisitNotes() {
        this.visitId = new SimpleIntegerProperty(0);
        this.patientId = new SimpleStringProperty("");
        this.primaryComplains = new SimpleStringProperty("");
        this.medicalHistory = new SimpleStringProperty("");
        this.physicalExam = new SimpleStringProperty("");
        this.investigation = new SimpleStringProperty("");
        this.treatment = new SimpleStringProperty("");
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

    public String getPatientId() {
        return patientId.get();
    }

    public void setPatientId(String patientId) {
        this.patientId.set(patientId);
    }

    public StringProperty patientIdProperty() {
        return patientId;
    }

    public String getPrimaryComplains() {
        return primaryComplains.get();
    }

    public void setPrimaryComplains(String primaryComplains) {
        this.primaryComplains.set(primaryComplains);
    }

    public StringProperty primaryComplainsProperty() {
        return primaryComplains;
    }

    public String getMedicalHistory() {
        return medicalHistory.get();
    }

    public void setMedicalHistory(String medicalHistory) {
        this.medicalHistory.set(medicalHistory);
    }

    public StringProperty medicalHistoryProperty() {
        return medicalHistory;
    }

    public String getPhysicalExam() {
        return physicalExam.get();
    }

    public void setPhysicalExam(String physicalExam) {
        this.physicalExam.set(physicalExam);
    }

    public StringProperty physicalExamProperty() {
        return physicalExam;
    }

    public String getInvestigation() {
        return investigation.get();
    }

    public void setInvestigation(String investigation) {
        this.investigation.set(investigation);
    }

    public StringProperty investigationProperty() {
        return investigation;
    }

    public String getTreatment() {
        return treatment.get();
    }

    public void setTreatment(String treatment) {
        this.treatment.set(treatment);
    }

    public StringProperty treatmentProperty() {
        return treatment;
    }

}
