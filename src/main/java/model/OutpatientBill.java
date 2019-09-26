package main.java.model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

public class OutpatientBill extends Bill {
    private IntegerProperty queueNumber;

    public OutpatientBill() {
        this.queueNumber = new SimpleIntegerProperty();
    }

    public int getQueueNumber() {
        return queueNumber.get();
    }

    public void setQueueNumber(int queueNumber) {
        this.queueNumber.set(queueNumber);
    }

    public IntegerProperty queueNumberProperty() {
        return queueNumber;
    }
}
