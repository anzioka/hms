package main.java.model;

import javafx.beans.property.*;

public class Ward {
    private StringProperty name;
    private IntegerProperty id, numBeds, numOccupiedBeds, bedsPerRow;
    private DoubleProperty rate, corporateRate, admissionCharge, corporateAdmissionCharge, doctorCharge,
            corporateDoctorCharge, nurseCharge, corporateNurseCharge;

    public Ward() {
        this.name = new SimpleStringProperty();
        this.id = new SimpleIntegerProperty();
        this.rate = new SimpleDoubleProperty();
        this.numBeds = new SimpleIntegerProperty();
        this.numOccupiedBeds = new SimpleIntegerProperty(0);
        this.corporateAdmissionCharge = new SimpleDoubleProperty();
        this.corporateRate = new SimpleDoubleProperty();
        this.nurseCharge = new SimpleDoubleProperty();
        this.corporateDoctorCharge = new SimpleDoubleProperty();
        this.corporateNurseCharge = new SimpleDoubleProperty();
        this.admissionCharge = new SimpleDoubleProperty();
        this.doctorCharge = new SimpleDoubleProperty();
        this.bedsPerRow = new SimpleIntegerProperty(2);

    }

    public Ward(String name) {
        this.name = new SimpleStringProperty(name);
    }

    public String getName() {
        return name.get();
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public StringProperty nameProperty() {
        return name;
    }

    public int getId() {
        return id.get();
    }

    public void setId(int id) {
        this.id.set(id);
    }

    public IntegerProperty idProperty() {
        return id;
    }

    @Override
    public String toString() {
        return getName();
    }

    public double getRate() {
        return rate.get();
    }

    public void setRate(double rate) {
        this.rate.set(rate);
    }

    public DoubleProperty rateProperty() {
        return rate;
    }

    public int getNumBeds() {
        return numBeds.get();
    }

    public void setNumBeds(int numBeds) {
        this.numBeds.set(numBeds);
    }

    public IntegerProperty numBedsProperty() {
        return numBeds;
    }

    public int getNumOccupiedBeds() {
        return numOccupiedBeds.get();
    }

    public void setNumOccupiedBeds(int numOccupiedBeds) {
        this.numOccupiedBeds.set(numOccupiedBeds);
    }

    public IntegerProperty numOccupiedBedsProperty() {
        return numOccupiedBeds;
    }

    public double getCorporateRate() {
        return corporateRate.get();
    }

    public void setCorporateRate(double corporateRate) {
        this.corporateRate.set(corporateRate);
    }

    public DoubleProperty corporateRateProperty() {
        return corporateRate;
    }

    public double getAdmissionCharge() {
        return admissionCharge.get();
    }

    public void setAdmissionCharge(double admissionCharge) {
        this.admissionCharge.set(admissionCharge);
    }

    public DoubleProperty admissionChargeProperty() {
        return admissionCharge;
    }

    public double getCorporateAdmissionCharge() {
        return corporateAdmissionCharge.get();
    }

    public void setCorporateAdmissionCharge(double corporateAdmissionCharge) {
        this.corporateAdmissionCharge.set(corporateAdmissionCharge);
    }

    public DoubleProperty corporateAdmissionChargeProperty() {
        return corporateAdmissionCharge;
    }

    public double getDoctorCharge() {
        return doctorCharge.get();
    }

    public void setDoctorCharge(double doctorCharge) {
        this.doctorCharge.set(doctorCharge);
    }

    public DoubleProperty doctorChargeProperty() {
        return doctorCharge;
    }

    public double getCorporateDoctorCharge() {
        return corporateDoctorCharge.get();
    }

    public void setCorporateDoctorCharge(double corporateDoctorCharge) {
        this.corporateDoctorCharge.set(corporateDoctorCharge);
    }

    public DoubleProperty corporateDoctorChargeProperty() {
        return corporateDoctorCharge;
    }

    public double getNurseCharge() {
        return nurseCharge.get();
    }

    public void setNurseCharge(double nurseCharge) {
        this.nurseCharge.set(nurseCharge);
    }

    public DoubleProperty nurseChargeProperty() {
        return nurseCharge;
    }

    public double getCorporateNurseCharge() {
        return corporateNurseCharge.get();
    }

    public void setCorporateNurseCharge(double corporateNurseCharge) {
        this.corporateNurseCharge.set(corporateNurseCharge);
    }

    public DoubleProperty corporateNurseChargeProperty() {
        return corporateNurseCharge;
    }

    public int getBedsPerRow() {
        return bedsPerRow.get();
    }

    public void setBedsPerRow(int bedsPerRow) {
        this.bedsPerRow.set(bedsPerRow);
    }

    public IntegerProperty bedsPerRowProperty() {
        return bedsPerRow;
    }
}
