package main.java.model;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import main.java.util.AgeUtil;

import java.time.LocalDate;

/**
 * Created by alfonce on 24/04/2017.
 */
public class Patient {
    private StringProperty patientId;
    private StringProperty firstName;
    private StringProperty lastName;
    private StringProperty NHIFNumber;
    private MaritalStatus maritalStatus;
    private StringProperty telephoneNumber;
    private StringProperty residence;
    private ObjectProperty<LocalDate> dateOfBirth;
    private StringProperty sexuality;

    //contact details
    private StringProperty contactFirstName;
    private StringProperty contactLastName;
    private StringProperty contactTelephone;
    private FamilyRelation familyRelation;

    //TODO : health details :
    private StringProperty insurer;
    private StringProperty insuranceID;

    private ObjectProperty<LocalDate> dateCreated;

    public Patient() {
        patientId = new SimpleStringProperty();
        firstName = new SimpleStringProperty();
        lastName = new SimpleStringProperty();
        telephoneNumber = new SimpleStringProperty();
        dateOfBirth = new SimpleObjectProperty<>();
        sexuality = new SimpleStringProperty();
        insuranceID = new SimpleStringProperty("");
        insurer = new SimpleStringProperty("");
        contactFirstName = new SimpleStringProperty();
        contactLastName = new SimpleStringProperty();
        contactTelephone = new SimpleStringProperty();
        NHIFNumber = new SimpleStringProperty();
        residence = new SimpleStringProperty();
        this.dateCreated = new SimpleObjectProperty<>();
    }

    public String getPatientId() {
        return patientId.get();
    }

    public void setPatientId(String patientId) {
        this.patientId.set(patientId);
    }

    public StringProperty patientIdProperty() {
        return patientId;
    }

    public String getFirstName() {
        return firstName.get();
    }

    public void setFirstName(String firstName) {
        this.firstName.set(firstName);
    }

    public StringProperty firstNameProperty() {
        return firstName;
    }

    public String getLastName() {
        return lastName.get();
    }

    public void setLastName(String lastName) {
        this.lastName.set(lastName);
    }

    public StringProperty lastNameProperty() {
        return lastName;
    }

    public String getTelephoneNumber() {
        return telephoneNumber.get();
    }

    public void setTelephoneNumber(String telephoneNumber) {
        this.telephoneNumber.set(telephoneNumber);
    }

    public StringProperty telephoneNumberProperty() {
        return telephoneNumber;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth.get();
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth.set(dateOfBirth);
    }

    public ObjectProperty<LocalDate> dateOfBirthProperty() {
        return dateOfBirth;
    }

    public String getSexuality() {
        return sexuality.get();
    }

    public void setSexuality(String sexuality) {
        this.sexuality.set(sexuality);
    }

    public StringProperty sexualityProperty() {
        return sexuality;
    }

    public String getContactFirstName() {
        return contactFirstName.get();
    }

    public void setContactFirstName(String contactFirstName) {
        this.contactFirstName.set(contactFirstName);
    }

    public StringProperty contactFirstNameProperty() {
        return contactFirstName;
    }

    public String getContactLastName() {
        return contactLastName.get();
    }

    public void setContactLastName(String contactLastName) {
        this.contactLastName.set(contactLastName);
    }

    public StringProperty contactLastNameProperty() {
        return contactLastName;
    }

    public FamilyRelation getContactRelationship() {
        return familyRelation;
    }

    public void setContactRelationship(FamilyRelation familyRelation) {
        this.familyRelation = familyRelation;
    }

    public StringProperty contactRelationshipProperty() {
        if (familyRelation == null) {
            return null;
        }
        return new SimpleStringProperty(familyRelation.toString());
    }

    public String getContactTelephone() {
        return contactTelephone.get();
    }

    public void setContactTelephone(String contactTelephone) {
        this.contactTelephone.set(contactTelephone);
    }

    public StringProperty contactTelephoneProperty() {
        return contactTelephone;
    }

    public String getInsurer() {
        return insurer.get();
    }

    public void setInsurer(String insurer) {
        this.insurer.set(insurer);
    }

    public StringProperty insurerProperty() {
        return insurer;
    }

    public String getInsuranceID() {
        return insuranceID.get();
    }

    public void setInsuranceID(String insuranceID) {
        this.insuranceID.set(insuranceID);
    }

    public StringProperty insuranceIDProperty() {
        return insuranceID;
    }

    public String getPatientAge() {

        return AgeUtil.getAge(getDateOfBirth());
    }


    public MaritalStatus getMaritalStatus() {
        return maritalStatus;
    }

    public void setMaritalStatus(MaritalStatus maritalStatus) {
        this.maritalStatus = maritalStatus;
    }

    public StringProperty maritalStatusProperty() {
        if (maritalStatus != null) {
            return new SimpleStringProperty(maritalStatus.toString());
        }
        return null;
    }

    public String getResidence() {
        return residence.get();
    }

    public void setResidence(String residence) {
        this.residence.set(residence);
    }

    public StringProperty residenceProperty() {
        return residence;
    }

    @Override
    public String toString() {
        return getFirstName() + " " + getLastName();
    }

    public LocalDate getDateCreated() {
        return dateCreated.get();
    }

    public void setDateCreated(LocalDate dateCreated) {
        this.dateCreated.set(dateCreated);
    }

    public ObjectProperty<LocalDate> dateCreatedProperty() {
        return dateCreated;
    }

    public String getNHIFNumber() {
        return NHIFNumber.get();
    }

    public void setNHIFNumber(String NHIFNumber) {
        this.NHIFNumber.set(NHIFNumber);
    }

    public StringProperty NHIFNumberProperty() {
        return NHIFNumber;
    }
}
