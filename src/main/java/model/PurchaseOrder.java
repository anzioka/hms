package main.java.model;

import javafx.beans.property.*;

import java.time.LocalDate;

/**
 * Created by alfonce on 08/07/2017.
 */
public class PurchaseOrder {
    private IntegerProperty orderId, id, supplierId, quantity, drugId;
    private StringProperty description, supplierName;
    private ObjectProperty<LocalDate> orderDate;
    private ObjectProperty<LocalDate> dateReceived;
    private DoubleProperty unitPrice;
    private DoubleProperty discount;
    private DoubleProperty totalPrice;
    private OrderStatus orderStatus;

    public PurchaseOrder() {
        this.orderDate = new SimpleObjectProperty<>();
        this.description = new SimpleStringProperty();
        this.unitPrice = new SimpleDoubleProperty(0);
        this.quantity = new SimpleIntegerProperty(0);
        this.totalPrice = new SimpleDoubleProperty();
        this.dateReceived = new SimpleObjectProperty<>();
        this.orderId = new SimpleIntegerProperty();
        this.id = new SimpleIntegerProperty();
        this.supplierId = new SimpleIntegerProperty();
        this.supplierName = new SimpleStringProperty();
        this.drugId = new SimpleIntegerProperty();
        this.discount = new SimpleDoubleProperty(0);
        this.setOrderStatus(OrderStatus.PENDING);
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

    public int getSupplierId() {
        return supplierId.get();
    }

    public void setSupplierId(int supplierId) {
        this.supplierId.set(supplierId);
    }

    public IntegerProperty supplierIdProperty() {
        return supplierId;
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

    public String getDescription() {
        return description.get();
    }

    public void setDescription(String description) {
        this.description.set(description);
    }

    public StringProperty descriptionProperty() {
        return description;
    }

    public LocalDate getOrderDate() {
        return orderDate.get();
    }

    public void setOrderDate(LocalDate orderDate) {
        this.orderDate.set(orderDate);
    }

    public ObjectProperty<LocalDate> orderDateProperty() {
        return orderDate;
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

    public double getTotalPrice() {
        return totalPrice.get();
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice.set(totalPrice);
    }

    public DoubleProperty totalPriceProperty() {
        return totalPrice;
    }

    public LocalDate getDateReceived() {
        return dateReceived.get();
    }

    public void setDateReceived(LocalDate dateReceived) {
        this.dateReceived.set(dateReceived);
    }

    public ObjectProperty<LocalDate> dateReceivedProperty() {
        return dateReceived;
    }

    public OrderStatus getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(OrderStatus orderStatus) {
        this.orderStatus = orderStatus;
    }

    public StringProperty orderStatusProperty() {
        return new SimpleStringProperty(getOrderStatus().toString());
    }

    public String getSupplierName() {
        return supplierName.get();
    }

    public void setSupplierName(String supplierName) {
        this.supplierName.set(supplierName);
    }

    public StringProperty supplierNameProperty() {
        return supplierName;
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

    public int getDrugId() {
        return drugId.get();
    }

    public void setDrugId(int drugId) {
        this.drugId.set(drugId);
    }

    public IntegerProperty drugIdProperty() {
        return drugId;
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
}
