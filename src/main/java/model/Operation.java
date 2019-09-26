package main.java.model;

import javafx.beans.property.*;

import java.time.LocalDate;
import java.time.LocalTime;

public class Operation {
    private IntegerProperty admissionNum;
    private ObjectProperty<LocalDate> date;
    private ObjectProperty<LocalTime> time;
    private StringProperty operation, indication, surgeon, assistants, anaesthetist, anaesthesia, incision, procedure;

    public Operation() {
        this.date = new SimpleObjectProperty<>();
        this.admissionNum = new SimpleIntegerProperty();
        this.time = new SimpleObjectProperty<>();
        this.procedure = new SimpleStringProperty();
        this.operation = new SimpleStringProperty();
        this.incision = new SimpleStringProperty();
        this.indication = new SimpleStringProperty();
        this.surgeon = new SimpleStringProperty();
        this.assistants = new SimpleStringProperty();
        this.anaesthetist = new SimpleStringProperty();
        this.anaesthesia = new SimpleStringProperty();
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

    public LocalDate getDate() {
        return date.get();
    }

    public void setDate(LocalDate date) {
        this.date.set(date);
    }

    public ObjectProperty<LocalDate> dateProperty() {
        return date;
    }

    public LocalTime getTime() {
        return time.get();
    }

    public void setTime(LocalTime time) {
        this.time.set(time);
    }

    public ObjectProperty<LocalTime> timeProperty() {
        return time;
    }

    public String getOperation() {
        return operation.get();
    }

    public void setOperation(String operation) {
        this.operation.set(operation);
    }

    public StringProperty operationProperty() {
        return operation;
    }

    public String getIndication() {
        return indication.get();
    }

    public void setIndication(String indication) {
        this.indication.set(indication);
    }

    public StringProperty indicationProperty() {
        return indication;
    }

    public String getSurgeon() {
        return surgeon.get();
    }

    public void setSurgeon(String surgeon) {
        this.surgeon.set(surgeon);
    }

    public StringProperty surgeonProperty() {
        return surgeon;
    }

    public String getAssistants() {
        return assistants.get();
    }

    public void setAssistants(String assistants) {
        this.assistants.set(assistants);
    }

    public StringProperty assistantsProperty() {
        return assistants;
    }

    public String getAnaesthetist() {
        return anaesthetist.get();
    }

    public void setAnaesthetist(String anaesthetist) {
        this.anaesthetist.set(anaesthetist);
    }

    public StringProperty anaesthetistProperty() {
        return anaesthetist;
    }

    public String getAnaesthesia() {
        return anaesthesia.get();
    }

    public void setAnaesthesia(String anaesthesia) {
        this.anaesthesia.set(anaesthesia);
    }

    public StringProperty anaesthesiaProperty() {
        return anaesthesia;
    }

    public String getIncision() {
        return incision.get();
    }

    public void setIncision(String incision) {
        this.incision.set(incision);
    }

    public StringProperty incisionProperty() {
        return incision;
    }

    public String getProcedure() {
        return procedure.get();
    }

    public void setProcedure(String procedure) {
        this.procedure.set(procedure);
    }

    public StringProperty procedureProperty() {
        return procedure;
    }
}
