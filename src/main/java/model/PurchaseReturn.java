package main.java.model;

import javafx.beans.property.*;

import java.time.LocalDate;

public class PurchaseReturn {
    private StringProperty invoiceNo, note, supplier, drugName, returnedBy;
    private ObjectProperty<LocalDate> date;
    private IntegerProperty userId, drugId, returnId, quantity, supplierId;
    private DoubleProperty buyingPrice;
    private MedicineLocation location;

    public PurchaseReturn() {
        this.note = new SimpleStringProperty();
        this.invoiceNo = new SimpleStringProperty();
        this.supplier = new SimpleStringProperty();
        this.drugName = new SimpleStringProperty();
        this.returnedBy = new SimpleStringProperty();
        this.quantity = new SimpleIntegerProperty();
        this.date = new SimpleObjectProperty<>();
        this.userId = new SimpleIntegerProperty();
        this.drugId = new SimpleIntegerProperty();
        this.returnId = new SimpleIntegerProperty();
        this.supplierId = new SimpleIntegerProperty();
        this.buyingPrice = new SimpleDoubleProperty();
    }

    public String getInvoiceNo() {
        return invoiceNo.get();
    }

    public void setInvoiceNo(String invoiceNo) {
        this.invoiceNo.set(invoiceNo);
    }

    public StringProperty invoiceNoProperty() {
        return invoiceNo;
    }

    public String getSupplier() {
        return supplier.get();
    }

    public void setSupplier(String supplier) {
        this.supplier.set(supplier);
    }

    public StringProperty supplierProperty() {
        return supplier;
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

    public String getReturnedBy() {
        return returnedBy.get();
    }

    public void setReturnedBy(String returnedBy) {
        this.returnedBy.set(returnedBy);
    }

    public StringProperty returnedByProperty() {
        return returnedBy;
    }

    public Integer getQuantity() {
        return quantity.get();
    }

    public void setQuantity(int quantity) {
        this.quantity.set(quantity);
    }

    public IntegerProperty quantityProperty() {
        return quantity;
    }

    public LocalDate getDate() {
        return date.get();
    }

    public void setDate(LocalDate date) {
        this.date.set(date);
    }

    public ObjectProperty<LocalDate> dateProperty() {
        return date;
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

    public int getReturnId() {
        return returnId.get();
    }

    public void setReturnId(int returnId) {
        this.returnId.set(returnId);
    }

    public IntegerProperty returnIdProperty() {
        return returnId;
    }

    public double getBuyingPrice() {
        return buyingPrice.get();
    }

    public void setBuyingPrice(double buyingPrice) {
        this.buyingPrice.set(buyingPrice);
    }

    public DoubleProperty buyingPriceProperty() {
        return buyingPrice;
    }

    public String getNote() {
        return note.get();
    }

    public void setNote(String note) {
        this.note.set(note);
    }

    public StringProperty noteProperty() {
        return note;
    }

    public int getSupplierId() {
        return supplierId.get();
    }

    public void setSupplierId(int supplierId) {
        this.supplierId.set(supplierId);
    }

    public IntegerProperty supplierIdProperty() {
        return supplierId;
    }

    public MedicineLocation getLocation() {
        return location;
    }

    public void setLocation(MedicineLocation location) {
        this.location = location;
    }
}
