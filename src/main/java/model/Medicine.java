package main.java.model;

import javafx.beans.property.*;

/**
 * Created by alfonce on 19/05/2017.
 */
public class Medicine {
    private StringProperty name;
    private StringProperty manufacturer;
    private DoubleProperty sellingPrice;
    private DoubleProperty buyingPrice;
    private IntegerProperty storeQuantity;
    private IntegerProperty shopQuantity;
    private IntegerProperty reorderLevel;
    private IntegerProperty drugCode;
    private StringProperty batchNumber;

    public Medicine() {
        this.manufacturer = new SimpleStringProperty("");
        this.name = new SimpleStringProperty();
        this.sellingPrice = new SimpleDoubleProperty();
        this.buyingPrice = new SimpleDoubleProperty();
        this.reorderLevel = new SimpleIntegerProperty(0);
        this.storeQuantity = new SimpleIntegerProperty(0);
        this.shopQuantity = new SimpleIntegerProperty(0);
        this.batchNumber = new SimpleStringProperty("");
        this.drugCode = new SimpleIntegerProperty();
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

    public String getManufacturer() {
        return manufacturer.get();
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer.set(manufacturer);
    }

    public StringProperty manufacturerProperty() {
        return manufacturer;
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

    public double getBuyingPrice() {
        return buyingPrice.get();
    }

    public void setBuyingPrice(double buyingPrice) {
        this.buyingPrice.set(buyingPrice);
    }

    public DoubleProperty buyingPriceProperty() {
        return buyingPrice;
    }

    public int getReorderLevel() {
        return reorderLevel.get();
    }

    public void setReorderLevel(int reorderLevel) {
        this.reorderLevel.set(reorderLevel);
    }

    public IntegerProperty reorderLevelProperty() {
        return reorderLevel;
    }

    public String getBatchNumber() {
        return batchNumber.get();
    }

    public void setBatchNumber(String batchNumber) {
        this.batchNumber.set(batchNumber);
    }

    public StringProperty batchNumberProperty() {
        return batchNumber;
    }

    @Override
    public String toString() {
        return this.getName();
    }

    public int getStoreQuantity() {
        return storeQuantity.get();
    }

    public void setStoreQuantity(int storeQuantity) {
        this.storeQuantity.set(storeQuantity);
    }

    public IntegerProperty storeQuantityProperty() {
        return storeQuantity;
    }

    public int getShopQuantity() {
        return shopQuantity.get();
    }

    public void setShopQuantity(int shopQuantity) {
        this.shopQuantity.set(shopQuantity);
    }

    public IntegerProperty shopQuantityProperty() {
        return shopQuantity;
    }

    public int getDrugCode() {
        return drugCode.get();
    }

    public void setDrugCode(int drugCode) {
        this.drugCode.set(drugCode);
    }

    public IntegerProperty drugCodeProperty() {
        return drugCode;
    }

}
