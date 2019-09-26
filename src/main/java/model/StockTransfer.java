package main.java.model;

import javafx.beans.property.*;
import main.java.util.DateUtil;

import java.time.LocalDate;

public class StockTransfer {
    private StringProperty drugName, transferredBy, description;
    private ObjectProperty<LocalDate> dateCreated;
    private IntegerProperty quantity, transferNo, drugId;
    private MedicineLocation origin;

    public StockTransfer() {
        this.drugName = new SimpleStringProperty();
        this.transferNo = new SimpleIntegerProperty();
        this.transferredBy = new SimpleStringProperty();
        this.description = new SimpleStringProperty();
        this.dateCreated = new SimpleObjectProperty<>(LocalDate.now());
        this.quantity = new SimpleIntegerProperty();
        this.drugId = new SimpleIntegerProperty();

    }

    public String getDrugName() {
        return drugName.get();
    }

    public void setDrugName(String drugName) {
        this.drugName.set(drugName);
    }

    public StringProperty drugNameProperty() {
        return drugName;
    }

    public String getTransferredBy() {
        return transferredBy.get();
    }

    public void setTransferredBy(String transferredBy) {
        this.transferredBy.set(transferredBy);
    }

    public StringProperty transferredByProperty() {
        return transferredBy;
    }

    public String getDescription() {
        return description.get();
    }

    public void setDescription(String description) {
        this.description.set(description);
    }

    public StringProperty descriptionProperty() {
        String desc = null;
        switch (getOrigin()) {
            case SHOP:
                desc = MedicineLocation.SHOP.toString() + " -> " + MedicineLocation.STORE.toString();
                break;
            case STORE:
                desc = MedicineLocation.STORE.toString() + " -> " + MedicineLocation.SHOP.toString();
                break;
        }
        return new SimpleStringProperty(desc);
    }

    public LocalDate getDateCreated() {
        return dateCreated.get();
    }

    public void setDateCreated(LocalDate dateCreated) {
        this.dateCreated.set(dateCreated);
    }

    public StringProperty dateCreatedProperty() {
        return DateUtil.dateStringProperty(getDateCreated());
    }

    public int getQuantity() {
        return quantity.get();
    }

    public void setQuantity(int quantity) {
        this.quantity.set(quantity);
    }

    public IntegerProperty quantityProperty() {
        return quantity;
    }

    public int getTransferNo() {
        return transferNo.get();
    }

    public void setTransferNo(int transferNo) {
        this.transferNo.set(transferNo);
    }

    public IntegerProperty transferNoProperty() {
        return transferNo;
    }

    public int getDrugId() {
        return drugId.get();
    }

    public void setDrugId(int drugId) {
        this.drugId.set(drugId);
    }

    public IntegerProperty drugIdProperty() {
        return drugId;
    }

    public MedicineLocation getOrigin() {
        return origin;
    }

    public void setOrigin(MedicineLocation origin) {
        this.origin = origin;
    }
}
