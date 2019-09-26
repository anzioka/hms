package main.java.model;

public enum RadiologyCategory {

    X_RAY("X-ray"),
    CT("CT Scan"),
    MRI("MRI"),
    ULTRASOUND("Ultrasound"),
    PET("PET");

    private String category;

    RadiologyCategory(String category) {
        this.category = category;
    }

    @Override
    public String toString() {
        return this.category;
    }
}