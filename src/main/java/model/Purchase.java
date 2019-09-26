package main.java.model;

import javafx.beans.property.*;
import main.java.util.CurrencyUtil;
import main.java.util.DateUtil;

import java.time.LocalDate;

public class Purchase {
    private StringProperty supplier, receivedBy, invoiceNumber, drugName, batchNo;
    private IntegerProperty quantity, currentQty, supplierId, purchaseId, drugId, orderId;
    private DoubleProperty unitPrice, discount, totalPrice;
    private ObjectProperty<LocalDate> dateDelivered, expiryDate;
    private MedicineLocation location;

    public Purchase() {
        this.drugName = new SimpleStringProperty();
        this.supplier = new SimpleStringProperty();
        this.receivedBy = new SimpleStringProperty();
        this.quantity = new SimpleIntegerProperty(0);
        this.purchaseId = new SimpleIntegerProperty();
        this.unitPrice = new SimpleDoubleProperty();
        this.discount = new SimpleDoubleProperty();
        this.dateDelivered = new SimpleObjectProperty<>(LocalDate.now());
        this.invoiceNumber = new SimpleStringProperty();
        this.totalPrice = new SimpleDoubleProperty();
        this.batchNo = new SimpleStringProperty();
        this.currentQty = new SimpleIntegerProperty(0);
        this.expiryDate = new SimpleObjectProperty<>();
        this.supplierId = new SimpleIntegerProperty();
        this.drugId = new SimpleIntegerProperty();
        this.orderId = new SimpleIntegerProperty(0);
        setLocation(MedicineLocation.STORE);
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

    public String getReceivedBy() {
        return receivedBy.get();
    }

    public void setReceivedBy(String receivedBy) {
        this.receivedBy.set(receivedBy);
    }

    public StringProperty receivedByProperty() {
        return receivedBy;
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

    public int getPurchaseId() {
        return purchaseId.get();
    }

    public void setPurchaseId(int purchaseId) {
        this.purchaseId.set(purchaseId);
    }

    public IntegerProperty purchaseIdProperty() {
        return purchaseId;
    }

    public double getDiscount() {
        return discount.get();
    }

    public void setDiscount(double discount) {
        this.discount.set(discount);
    }

    public DoubleProperty discountProperty() {
        return discount;
    }

    public double getUnitPrice() {
        return unitPrice.get();
    }

    public void setUnitPrice(double unitPrice) {
        this.unitPrice.set(unitPrice);
    }

    public DoubleProperty unitPriceProperty() {
        return unitPrice;
    }

    public StringProperty totalPriceProperty() {
        double cost = getUnitPrice() == 0 ? getTotalPrice() : (100 - getDiscount()) / 100 * getQuantity() *
                getUnitPrice();
        return CurrencyUtil.getStringProperty(cost);
    }

    public LocalDate getDateDelivered() {
        return dateDelivered.get();
    }

    public void setDateDelivered(LocalDate dateDelivered) {
        this.dateDelivered.set(dateDelivered);
    }

    public StringProperty dateDeliveredProperty() {
        return new SimpleStringProperty(DateUtil.formatDate(getDateDelivered()));
    }

    public MedicineLocation getLocation() {
        return location;
    }

    public void setLocation(MedicineLocation location) {
        this.location = location;
    }

    public StringProperty locationProperty() {
        if (getLocation() != null) {
            return new SimpleStringProperty(getLocation().toString());
        } else {
            return null;
        }
    }

    public String getInvoiceNumber() {
        return invoiceNumber.get();
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber.set(invoiceNumber);
    }

    public StringProperty invoiceNumberProperty() {
        return invoiceNumber;
    }

    public double getTotalPrice() {
        return totalPrice.get();
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice.set(totalPrice);
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

    public String getBatchNo() {
        return batchNo.get();
    }

    public void setBatchNo(String batchNo) {
        this.batchNo.set(batchNo);
    }

    public StringProperty batchNoProperty() {
        return batchNo;
    }

    public LocalDate getExpiryDate() {
        return expiryDate.get();
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate.set(expiryDate);
    }

    public StringProperty expiryDateProperty() {
        return new SimpleStringProperty(DateUtil.formatDateLong(getExpiryDate()));
    }

    public int getCurrentQty() {
        return currentQty.get();
    }

    public void setCurrentQty(int currentQty) {
        this.currentQty.set(currentQty);
    }

    public IntegerProperty currentQtyProperty() {
        return currentQty;
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

    public int getDrugId() {
        return drugId.get();
    }

    public void setDrugId(int drugId) {
        this.drugId.set(drugId);
    }

    public IntegerProperty drugIdProperty() {
        return drugId;
    }

    public int getOrderId() {
        return orderId.get();
    }

    public void setOrderId(int orderId) {
        this.orderId.set(orderId);
    }

    public IntegerProperty orderIdProperty() {
        return orderId;
    }

    @Override
    public String toString() {
        return getDrugName();
    }
}
