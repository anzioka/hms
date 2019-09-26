package main.java.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import main.java.util.AlertUtil;

public class PharmacyPosController {
    @FXML
    private HBox searchBox;
    @FXML
    private VBox container;
    @FXML
    private TextField searchField;
    private Stage stage;

    @FXML
    private void initialize() {
        togglePosVisibility(false);
    }

    private void togglePosVisibility(boolean visible) {
        for (int i = 1; i < container.getChildren().size(); i++) {
            container.getChildren().get(i).setVisible(visible);
            container.getChildren().get(i).setManaged(visible);
        }
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @FXML
    private void onSearchReceipt() {
        AlertUtil.showAlert("POS", "This option is not currently available. Sorry!", Alert.AlertType.INFORMATION);
        stage.close();
    }
}
