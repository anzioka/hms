package main.java.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Created by alfonce on 01/07/2017.
 */
public class Hospital {
    private StringProperty name;
    private StringProperty address;
    private StringProperty phoneNumber;
    private StringProperty city;

    public Hospital() {
        this.name = new SimpleStringProperty("Name of Hospital");
        this.address = new SimpleStringProperty("Address of Hospital");
        this.phoneNumber = new SimpleStringProperty("Phone Number");

        this.city = new SimpleStringProperty("Town");

    }

    public String getName() {
        return name.get();
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public String getAddress() {
        return address.get();
    }

    public void setAddress(String address) {
        this.address.set(address);
    }

    public String getPhoneNumber() {
        return phoneNumber.get();
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber.set(phoneNumber);
    }

    public String getCity() {
        return city.get();
    }

    public void setCity(String city) {
        this.city.set(city);
    }

    public StringProperty cityProperty() {
        return city;
    }
}
