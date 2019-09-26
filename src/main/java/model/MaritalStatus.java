package main.java.model;

/**
 * Created by alfonce on 27/07/2017.
 */
public enum MaritalStatus {
    MARRIED("Married"),
    WIDOWED("Widowed"),
    SEPARATED("Separated"),
    DIVORCED("Divorced"),
    SINGLE("Single");

    private String status;

    MaritalStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return status;
    }
}
