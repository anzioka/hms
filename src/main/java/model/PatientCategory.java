package main.java.model;

public enum PatientCategory {
    ALL("All patients"), INPATIENT("Inpatient"), OUTPATIENT("Outpatient"), WALK_IN("Walk-in patient");
    private String category;

    PatientCategory(String category) {
        this.category = category;
    }

    @Override
    public String toString() {
        return category;
    }
}
