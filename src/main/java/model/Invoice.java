package main.java.model;

/**
 * Created by alfonce on 14/06/2017.
 */
public class Invoice {
    private String description, amount;

    public Invoice(String description, String amount) {
        this.description = description;
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }
}
