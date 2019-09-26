package main.java.model;

/**
 * Created by alfonce on 24/07/2017.
 */
public enum Specimen {
    BLOOD("Blood"),
    STOOL("Stool"),
    URINE("Urine"),
    SWAB("Swab"),
    SPUTUM("Sputum"),
    SYNOVIAL_FLUID("Synovial Fluid"),
    CEREBROSPINAL_FLUID("Cerebrospinal Fluid");

    private String specimen;

    Specimen(String specimen) {
        this.specimen = specimen;
    }

    @Override
    public String toString() {
        return this.specimen;
    }
}
