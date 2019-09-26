package main.java.model;

public enum Module {
    REGISTRATION("Registration"),
    DOCTOR("Doctor"),
    PHARMACY("Pharmacy"),
    INVENTORY("Inventory"),
    LAB("Lab"),
    ADMIN_DASHBOARD("Admin Dashboard"),
    PATIENT_RECORDS("Patient Records"),
    REPORTS("Reports"),
    TRIAGE_STATION("Triage"),
    BED_AND_WARD("Bed and Ward Management"),
    INPATIENT_MANAGEMENT("Inpatient Management"),
    RADIOLOGY("Radiology"),
    ACCOUNTING("Accounting"),
    BILLING("Billing");

    private String moduleName;

    Module(String module) {
        this.moduleName = module;
    }

    @Override
    public String toString() {
        return moduleName;
    }
}
