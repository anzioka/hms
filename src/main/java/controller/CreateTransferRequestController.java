package main.java.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import main.Main;
import main.java.dao.MedicineDAO;
import main.java.model.RefillRequest;
import main.java.util.AlertUtil;
import main.java.util.DBUtil;
import main.java.util.NumberUtil;
import org.controlsfx.control.textfield.TextFields;

import java.time.LocalDate;

/**
 * Created by alfonce on 01/08/2017.
 */
public class CreateTransferRequestController {

    @FXML
    private TextField medicineName, quantityRequested;
    private Stage stage;

    @FXML
    public void initialize() {
        //autocomplete
        TextFields.bindAutoCompletion(medicineName, MedicineDAO.getMedicineList());
    }

    @FXML
    private void onSendRequest() {
        if (validInput()) {
            RefillRequest refillRequest = new RefillRequest();
            refillRequest.setRequesterId(Main.currentUser.getUserId());
            refillRequest.setAmountRequested(NumberUtil.stringToInt(quantityRequested.getText()));
            refillRequest.setDateRequested(LocalDate.now());
            refillRequest.setMedicineName(medicineName.getText());

            if (DBUtil.addRefillRequest(refillRequest)) {
                AlertUtil.showAlert("Refill Request", "Your transfer request has been successfully submitted", Alert
                        .AlertType.INFORMATION);
                stage.close();
            } else {
                AlertUtil.showAlert("Refill Error", "An error occurred while submitting your request", Alert
                        .AlertType.ERROR);
            }
        }
    }

    private boolean validInput() {
        String errorMsg = "";
        if (medicineName.getText() == null || medicineName.getText().isEmpty()) {
            errorMsg += "Medicine name is required!\n";
        }

        int quantity = NumberUtil.stringToInt(quantityRequested.getText());
        if (quantity == -1) {
            errorMsg += "Invalid quantity value! (only numbers allowed) \n";
        }

        if (quantity == 0) {
            errorMsg += "Quantity should be at least 1\n";
        }

        if (errorMsg.isEmpty()) {
            return true;
        }
        AlertUtil.showAlert("Input Errors", errorMsg, Alert.AlertType.ERROR);
        return false;
    }

    void setStage(Stage stage) {
        this.stage = stage;
    }

    @FXML
    private void onClose() {
        stage.close();
    }
}
