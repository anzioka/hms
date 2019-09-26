package main.java.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.io.IOException;

public class BillingController {
    public static final String SERVER = "server";
    public static final String BILL_NUM = "bill number";
    public static final String PATIENT_NUM = "patient number";
    public static final String PATIENT_NAME = "patient name";
    public static final String AMOUNT_PAID = "amount paid";
    public static final String CATEGORY = "category";
    public static final String OUTSTANDING_AMOUNT = "outstanding";
    public static final String INSURER = "insurer";
    public static final String INSURANCE_ID = "insurance_id";
    public static final String DATE = "date";
    public static final String REBATE = "rebate";
    @FXML
    private HBox buttonPanel;
    private Button selectedButton;
    @FXML
    private VBox container;
    @FXML
    private void initialize() {
        for (Node button : buttonPanel.getChildren()) {
            button.setOnMouseClicked(event -> {
                handleClicked((Button) button);
            });
        }
        selectedButton = (Button) buttonPanel.getChildren().get(0);
        handleClicked(selectedButton);
    }

    private void handleClicked(Button button) {
        selectButton(selectedButton, false);
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("main/resources/view/" + button.getId() + ".fxml"));
            Node node = loader.load();

            //remove last child
            if (container.getChildren().size() > 2) {
                container.getChildren().remove(container.getChildren().size() - 1);
            }
            container.getChildren().add(node);
            VBox.setVgrow(node, Priority.ALWAYS);
        } catch (IOException e) {
            e.printStackTrace();
        }
        selectButton(button, true);
        selectedButton = button;
    }

    private void selectButton(Button button, boolean select) {
        if (button != null) {
            if (select) {
                button.getStyleClass().add("tab");
            } else {
                button.getStyleClass().remove("tab");
            }
        }

    }
}
