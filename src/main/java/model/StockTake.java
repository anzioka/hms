package main.java.model;

import javafx.beans.property.*;

import java.time.LocalDate;

public class StockTake {
    private StringProperty userName, medicineName;
    private IntegerProperty userId, drugId, stockTakeId, qtyOnHand, countedQty, qtyChange, numDrugs;
    private DoubleProperty valueChange, sellingPrice;
    private ObjectProperty<LocalDate> dateCreated;
    private MedicineLocation medicineLocation;

    public StockTake() {
        this.userId = new SimpleIntegerProperty();
        this.userName = new SimpleStringProperty();
        this.drugId = new SimpleIntegerProperty();
        this.stockTakeId = new SimpleIntegerProperty();
        this.qtyOnHand = new SimpleIntegerProperty();
        this.countedQty = new SimpleIntegerProperty();
        this.dateCreated = new SimpleObjectProperty<>();
        this.valueChange = new SimpleDoubleProperty();
        this.qtyChange = new SimpleIntegerProperty();
        this.sellingPrice = new SimpleDoubleProperty();
        this.medicineName = new SimpleStringProperty();
        this.numDrugs = new SimpleIntegerProperty();
    }

    public String getUserName() {
        return userName.get();
    }

    public void setUserName(String userName) {
        this.userName.set(userName);
    }

    public StringProperty userNameProperty() {
        return userName;
    }

    public int getUserId() {
        return userId.get();
    }

    public void setUserId(int userId) {
        this.userId.set(userId);
    }

    public IntegerProperty userIdProperty() {
        return userId;
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

    public int getStockTakeId() {
        return stockTakeId.get();
    }

    public void setStockTakeId(int stockTakeId) {
        this.stockTakeId.set(stockTakeId);
    }

    public IntegerProperty stockTakeIdProperty() {
        return stockTakeId;
    }

    public int getQtyOnHand() {
        return qtyOnHand.get();
    }

    public void setQtyOnHand(int qtyOnHand) {
        this.qtyOnHand.set(qtyOnHand);
    }

    public IntegerProperty qtyOnHandProperty() {
        return qtyOnHand;
    }

    public int getCountedQty() {
        return countedQty.get();
    }

    public void setCountedQty(int countedQty) {
        this.countedQty.set(countedQty);
    }

    public IntegerProperty countedQtyProperty() {
        return countedQty;
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

    public int getQtyChange() {
        return qtyChange.get();
    }

    public void setQtyChange(int qtyChange) {
        this.qtyChange.set(qtyChange);
    }

    public IntegerProperty qtyChangeProperty() {
        return qtyChange;
    }

    public double getValueChange() {
        return valueChange.get();
    }

    public void setValueChange(double valueChange) {
        this.valueChange.set(valueChange);
    }

    public DoubleProperty valueChangeProperty() {
        return valueChange;
    }

    public StringProperty medicineLocationProperty() {
        if (medicineLocation != null) {
            return new SimpleStringProperty(medicineLocation.toString());
        }
        return null;
    }

    public double getSellingPrice() {
        return sellingPrice.get();
    }

    public void setSellingPrice(double sellingPrice) {
        this.sellingPrice.set(sellingPrice);
    }

    public DoubleProperty sellingPriceProperty() {
        return sellingPrice;
    }

    public String getMedicineName() {
        return medicineName.get();
    }

    public void setMedicineName(String medicineName) {
        this.medicineName.set(medicineName);
    }

    public StringProperty medicineNameProperty() {
        return medicineName;
    }

    public int getNumDrugs() {
        return numDrugs.get();
    }

    public void setNumDrugs(int numDrugs) {
        this.numDrugs.set(numDrugs);
    }

    public IntegerProperty numDrugsProperty() {
        return numDrugs;
    }

    public MedicineLocation getMedicineLocation() {
        return this.medicineLocation;
    }

    public void setMedicineLocation(MedicineLocation medicineLocation) {
        this.medicineLocation = medicineLocation;
    }
}
