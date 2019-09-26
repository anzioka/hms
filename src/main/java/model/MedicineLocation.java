package main.java.model;

public enum MedicineLocation {
    STORE("Drug Store"),
    SHOP("Shop");

    private String location;

    MedicineLocation(String location) {
        this.location = location;
    }

    @Override
    public String toString() {
        return this.location;
    }
}
