package main.java.model;

/**
 * Created by alfonce on 13/07/2017.
 */
public enum AgeGroup {
    CHILDREN(0, 14, "Children (0 -14 years)"),
    YOUTH(15, 24, "Youth (15 - 24 years"),
    ADULTS(25, 64, "Adults (25 -64 years)"),
    SENIORS(65, Integer.MAX_VALUE, "Seniors (65 and above years)");

    private int minAge, maxAge;
    private String description;

    AgeGroup(int minAge, int maxAge, String description) {
        this.minAge = minAge;
        this.maxAge = maxAge;
        this.description = description;
    }

    public boolean containsAge(int age) {
        return age >= this.minAge && age <= this.maxAge;
    }

    @Override
    public String toString() {
        return this.description;
    }

}


