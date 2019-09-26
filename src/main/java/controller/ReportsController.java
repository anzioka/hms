package main.java.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.io.IOException;

/**
 * Created by alfonce on 11/07/2017.
 */
public class ReportsController {

    private Button clickedButton;
    @FXML
    private VBox container;
    @FXML
    private FlowPane buttonPanel;

    @FXML
    public void initialize() {
        for (Node node : buttonPanel.getChildren()) {
            node.setOnMouseClicked(event -> {
                setSelected(clickedButton, false);
                loadView((Button) event.getSource());
                clickedButton = (Button) event.getSource();
                setSelected(clickedButton, true);
            });
        }

        //TODO : remove this later
        buttonPanel.getChildren().get(1).setManaged(false);
        buttonPanel.getChildren().get(1).setVisible(false);

        clickedButton = (Button) buttonPanel.getChildren().get(0);
        loadView(clickedButton);
        setSelected(clickedButton, true);

    }

    private void loadView(Button button) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("main/resources/view/" + button.getId() + ".fxml"));

            if (container.getChildren().size() > 2) {
                container.getChildren().remove(container.getChildren().size() - 1);
            }
            Node node = loader.load();
            container.getChildren().add(node);
            VBox.setVgrow(node, Priority.ALWAYS);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setSelected(Button button, boolean select) {
        if (button != null) {
            if (select) {
                button.getStyleClass().add("tab");
            } else {
                button.getStyleClass().remove("tab");
            }
        }
    }

}
