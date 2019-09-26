package main.java.controller;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import main.java.dao.SettingsDao;
import main.java.model.Setting;
import main.java.util.AlertUtil;
import main.java.util.CurrencyUtil;
import main.java.util.DBUtil;

public class GeneralSettingsController {
    @FXML
    private CheckBox labPrepayCheckBox, pharmacyPrepayCheckbox, radiologyPrepayCheckbox;

    @FXML
    private TextField consultationFee, corporateConsultationFee, NHIFRebate;
    private Setting setting;

    @FXML
    private void initialize() {
        getSettings();
    }

    private void getSettings() {
        Task<Setting> settingsTask = new Task<Setting>() {
            @Override
            protected Setting call() {
                return SettingsDao.getSettings();
            }
        };

        settingsTask.setOnSucceeded(event -> {
            setting = settingsTask.getValue();
            consultationFee.setText(CurrencyUtil.formatCurrency(setting.getConsultationFee()));
            corporateConsultationFee.setText(CurrencyUtil.formatCurrency(setting.getCorporateConsultationFee()));
            labPrepayCheckBox.setSelected(setting.isLabPrepay());
            pharmacyPrepayCheckbox.setSelected(setting.isPharmacyPrepay());
            radiologyPrepayCheckbox.setSelected(setting.isRadiologyPrepay());
            NHIFRebate.setText(CurrencyUtil.formatCurrency(setting.getNHIFRebate()));

        });

        new Thread(settingsTask).start();
    }

    @FXML
    private void onSaveSettings() {
        if (validInput()) {
            if (setting == null) {
                setting = new Setting();
            }
            setting.setRadiologyPrepay(radiologyPrepayCheckbox.isSelected());
            setting.setLabPrepay(labPrepayCheckBox.isSelected());
            setting.setPharmacyPrepay(pharmacyPrepayCheckbox.isSelected());
            setting.setCorporateConsultationFee(CurrencyUtil.parseCurrency(corporateConsultationFee.getText()));
            setting.setConsultationFee(CurrencyUtil.parseCurrency(consultationFee.getText()));
            setting.setNHIFRebate(CurrencyUtil.parseCurrency(NHIFRebate.getText()));
            if (DBUtil.saveSettings(setting)) {
                AlertUtil.showAlert("General Setting", "Changes have been successfully saved", Alert.AlertType
                        .INFORMATION);
            } else {
                AlertUtil.showAlert("Error", "An error occurred while attempting to save changes", Alert.AlertType
                        .ERROR);
            }
        }
    }

    private boolean validInput() {
        String errorMsg = "";
        if (CurrencyUtil.parseCurrency(consultationFee.getText()) == -1) {
            errorMsg += "Invalid consultation fee value.\n";
        }
        if (CurrencyUtil.parseCurrency(corporateConsultationFee.getText()) == -1) {
            errorMsg += "Invalid corporate consultation fee value.";
        }
        if (CurrencyUtil.parseCurrency(NHIFRebate.getText()) == -1) {
            errorMsg += "Invalid NHIF rebate amount\n";
        }
        if (errorMsg.isEmpty()) {
            return true;
        }
        AlertUtil.showAlert("Input Error(s)", errorMsg, Alert.AlertType.ERROR);
        return false;
    }
}
