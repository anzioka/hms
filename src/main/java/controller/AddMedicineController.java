package main.java.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import main.java.model.Medicine;
import main.java.util.AlertUtil;
import main.java.util.DBUtil;
import main.java.util.NumberUtil;

/**
 * Created by alfonce on 20/07/2017.
 */
public class AddMedicineController {
    @FXML
    private TextField name, sellingPrice, reorderLevel, buyingPrice;
    @FXML
    private Label dialogTitle;
    private Medicine medicine;
    private StockManagementController context;
    private Stage stage;

    @FXML
    public void initialize() {

    }

    void setStage(Stage stage) {
        this.stage = stage;
    }

    @FXML
    public void onSave() {
        if (isValidInput()) {
            if (medicine == null) {
                this.medicine = new Medicine();
            }
            medicine.setName(name.getText());
            medicine.setSellingPrice(NumberUtil.stringToDouble(sellingPrice.getText()));
            medicine.setBuyingPrice(NumberUtil.stringToDouble(buyingPrice.getText()));
            medicine.setReorderLevel(NumberUtil.stringToInt(reorderLevel.getText()));
            if (DBUtil.addMedicine(medicine)) {
                AlertUtil.showAlert("Changes Saved", "Drug information has been successfully saved", Alert
                        .AlertType
                        .INFORMATION);
                stage.close();
            } else {
                AlertUtil.showAlert("Error", "An error occurred while saving drug information.", Alert.AlertType
                        .ERROR);
            }
        }
    }

    private boolean isValidInput() {
        String errorMsg = "";

        if (name.getText() == null || name.getText().isEmpty()) {
            errorMsg += "Name of medicine required!\n";
        } else if (medicine == null && context.isNameDuplicate(name.getText())) {
            errorMsg += "A drug by that name already exists!\n";
        }

        double sellingPriceVal = NumberUtil.stringToDouble(sellingPrice.getText());
        if (sellingPriceVal < 0) {
            errorMsg += "Please enter a valid  selling price! \n";
        }
        if (reorderLevel.getText() != null && !reorderLevel.getText().isEmpty()) {
            if (NumberUtil.stringToInt(reorderLevel.getText()) < 0) {
                errorMsg += "Please enter a valid reorder level or leave field blank!\n";
            }
        }
        if (buyingPrice.getText() != null && !buyingPrice.getText().isEmpty()) {
            if (NumberUtil.stringToDouble(buyingPrice.getText()) < 0) {
                errorMsg += "Please enter a valid buying price or leave field blank!\n";
            }
        }

        if (errorMsg.isEmpty()) {
            return true;
        }
        AlertUtil.showAlert("Error", errorMsg, Alert.AlertType.ERROR);
        return false;
    }

    public Medicine getMedicine() {
        return medicine;
    }

    public void setMedicine(Medicine medicine) {
        this.medicine = medicine;
        if (medicine != null) {
            dialogTitle.setText("Edit Medicine");
            buyingPrice.setText(medicine.getBuyingPrice() + "");
            sellingPrice.setText(medicine.getSellingPrice() + "");
            name.setText(medicine.getName() + "");
            reorderLevel.setText(medicine.getReorderLevel() + "");
        }
    }

    StockManagementController getContext() {
        return context;
    }

    void setContext(StockManagementController context) {
        this.context = context;
    }
}
