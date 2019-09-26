package main.java.model;

public class Setting {
    private int id;
    private boolean labPrepay;
    private boolean pharmacyPrepay;
    private boolean radiologyPrepay;
    private double consultationFee;
    private double corporateConsultationFee;
    private double NHIFRebate;

    public Setting() {
        setConsultationFee(1000);
        setCorporateConsultationFee(1000);
        setNHIFRebate(0);
        setLabPrepay(true);
        setPharmacyPrepay(true);
        setRadiologyPrepay(true);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getConsultationFee() {
        return consultationFee;
    }

    public void setConsultationFee(double consultationFee) {
        this.consultationFee = consultationFee;
    }

    public double getCorporateConsultationFee() {
        return corporateConsultationFee;
    }

    public void setCorporateConsultationFee(double corporateConsultationFee) {
        this.corporateConsultationFee = corporateConsultationFee;
    }

    public boolean isLabPrepay() {
        return labPrepay;
    }

    public void setLabPrepay(boolean labPrepay) {
        this.labPrepay = labPrepay;
    }

    public boolean isPharmacyPrepay() {
        return pharmacyPrepay;
    }

    public void setPharmacyPrepay(boolean pharmacyPrepay) {
        this.pharmacyPrepay = pharmacyPrepay;
    }

    public double getNHIFRebate() {
        return NHIFRebate;
    }

    public void setNHIFRebate(double NHIFRebate) {
        this.NHIFRebate = NHIFRebate;
    }

    public boolean isRadiologyPrepay() {
        return radiologyPrepay;
    }

    public void setRadiologyPrepay(boolean radiologyPrepay) {
        this.radiologyPrepay = radiologyPrepay;
    }
}
