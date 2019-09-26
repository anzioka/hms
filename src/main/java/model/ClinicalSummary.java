package main.java.model;

import java.time.LocalDate;

public class ClinicalSummary {
    private int admissionNum;
    private String summary;
    private LocalDate dateModified;

    public ClinicalSummary() {

    }

    public int getAdmissionNum() {
        return admissionNum;
    }

    public void setAdmissionNum(int admissionNum) {
        this.admissionNum = admissionNum;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public LocalDate getDateModified() {
        return dateModified;
    }

    public void setDateModified(LocalDate dateModified) {
        this.dateModified = dateModified;
    }
}
