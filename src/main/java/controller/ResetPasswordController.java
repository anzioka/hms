package main.java.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.stage.Stage;
import main.java.model.User;
import main.java.util.AlertUtil;
import main.java.util.DBUtil;

public class ResetPasswordController {
    private Stage stage;
    private User user;

    @FXML
    private Label userName;

    @FXML
    private PasswordField password, confirmPassword;

    void setStage(Stage stage) {
        this.stage = stage;
    }

    User getUser() {
        return user;
    }

    void setUser(User user) {
        this.user = user;
        userName.setText(user.getLoginName());
    }

    @FXML
    private void onSavePassword() {
        if (validInput()) {
            String sql = "SET PASSWORD FOR '" + user.getLoginName() + "'@'localhost' = Password('" + password
                    .getText() + "')";
            if (DBUtil.executeStatement(sql)) {
                AlertUtil.showAlert("Password Reset", "Login password for " + user.getLoginName() + " has been " +
                        "successfully reset", Alert.AlertType.INFORMATION);
                onCloseDialog();
            } else {
                AlertUtil.showAlert("Password Reset", "An error occurred when trying to reset password for " + user
                        .getLoginName(), Alert.AlertType.ERROR);
            }
        }
    }

    private boolean validInput() {
        String errorMsg = "";
        if (password.getText() == null || password.getText().isEmpty()) {
            errorMsg += "Password cannot be empty!\n";
        } else if (!password.getText().equals(confirmPassword.getText())) {
            errorMsg += "Passwords do not match!\n";
        }
        if (errorMsg.isEmpty()) {
            return true;
        } else {
            AlertUtil.showAlert("Reset password error", errorMsg, Alert.AlertType.ERROR);
        }
        return false;
    }

    @FXML
    private void onCloseDialog() {
        stage.close();
    }
}
