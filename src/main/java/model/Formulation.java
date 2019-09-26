package main.java.model;

public enum Formulation {
    TABLET("Tablet"), CAPSULE("Capsule");

    private String formulation;

    Formulation(String formulation) {
        this.formulation = formulation;
    }

    @Override
    public String toString() {
        return this.formulation;
    }
}