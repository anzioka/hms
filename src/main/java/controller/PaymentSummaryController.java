package main.java.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class PaymentSummaryController {
    @FXML
    private Label balanceLabel;

    void setBalance(double v) {
        balanceLabel.setText("Ksh. " + v);
        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
    }
}
