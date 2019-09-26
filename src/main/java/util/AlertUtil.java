package main.java.util;

import javafx.scene.control.Alert;

/**
 * Created by alfonce on 01/07/2017.
 */
public class AlertUtil {
    public static void showAlert(String title, String content, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public static void showGenericError() {
        showAlert("Error", "An unknown error occurred", Alert.AlertType.ERROR);
    }
}
