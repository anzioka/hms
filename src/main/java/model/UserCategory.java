package main.java.model;

/**
 * Created by alfonce on 19/07/2017.
 */
public enum UserCategory {
    ADMIN("Admin"),
    DOCTOR("Doctor"),
    NURSE("Nurse"),
    CASHIER("Cashier"),
    RECEPTIONIST("Receptionist"),
    LAB_ASSISTANT("Lab Assistant"),
    PHARMACIST("Pharmacist"),
    INVENTORY_CONTROL_SPECIALIST("Inventory control specialist");

    private String category;

    UserCategory(String category) {
        this.category = category;
    }

    @Override
    public String toString() {
        return this.category;
    }
}
