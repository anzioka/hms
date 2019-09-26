package main.java.model;

public enum NHIFApplicability {
    APPLICABLE("Applicable"), NON_APPLICABLE("Not applicable");
    private final String applicable;

    NHIFApplicability(String applicable) {
        this.applicable = applicable;
    }

    @Override
    public String toString() {
        return applicable;
    }
}
