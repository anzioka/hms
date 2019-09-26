package main.java.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import main.java.model.User;
import main.java.util.AlertUtil;
import main.java.util.DBUtil;

/**
 * Created by alfonce on 01/08/2017.
 */
public class ChangePasswordController {

    private Stage stage;
    private User user;
    @FXML
    private TextField passwordField, confirmPasswordField;
    @FXML
    private Label prompt;

    @FXML
    private void onSave() {
        String alertMsg = "";
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        if (password == null || password.isEmpty()) {
            alertMsg += "Password is required!\n";
        } else if (!password.equals(confirmPassword)) {
            alertMsg += "Passwords do not match!";
        }
        if (alertMsg.isEmpty()) {
            String sql = "SET PASSWORD FOR '" + user.getLoginName() + "'@'localhost' = PASSWORD('" + password
                    + "')";
            if (DBUtil.executeStatement(sql)) {
                DBUtil.saveActivity("Reset password for user " + user.getDescription());
                AlertUtil.showAlert("", "Password for " + user.getLoginName() + " has been successfully reset!", Alert.AlertType.INFORMATION);
                onCancel();

            } else {
                AlertUtil.showGenericError();
            }
        } else {
            AlertUtil.showAlert("Error", alertMsg, Alert.AlertType.ERROR);
        }
    }

    void setStage(Stage stage) {
        this.stage = stage;
    }

    void setUser(User user) {
        this.user = user;
        prompt.setText("Enter new password for '" + user.getLoginName() + "'");
    }

    @FXML
    private void onCancel() {
        stage.close();
    }

}
