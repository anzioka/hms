package main.java.model;

/**
 * Created by alfonce on 27/07/2017.
 */
public enum FamilyRelation {
    BROTHER("Brother"), SISTER("Sister"), SPOUSE("Spouse"), FATHER("Father"),
    MOTHER("Mother"), SON("Son"), DAUGHTER("Daughter"), GUARDIAN("Guardian"), OTHER("Other");

    private String relation;

    FamilyRelation(String relation) {
        this.relation = relation;
    }

    @Override
    public String toString() {
        return relation;
    }
}
