package main.java.model;

/**
 * Created by alfonce on 23/07/2017.
 */
public enum ServiceType {
    CONSULTATION("Consultation"),
    REVIEW("Review"),
    LAB_TEST("Lab Test"),
    PHARMACY("Pharmacy");

    private String service;

    ServiceType(String service) {
        this.service = service;
    }

    @Override
    public String toString() {
        return this.service;
    }
}
