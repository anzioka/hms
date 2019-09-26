package main.java.model;

/**
 * Created by alfonce on 27/07/2017.
 */
public enum PaymentMode {
    INSURANCE("Insurance"),
    CASH("Cash");

    private final String paymentMode;

    PaymentMode(String paymentMode) {
        this.paymentMode = paymentMode;
    }

    @Override
    public String toString() {
        return paymentMode;
    }
}
