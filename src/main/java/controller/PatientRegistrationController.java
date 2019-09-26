package main.java.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

import java.io.IOException;

/**
 * Created by alfonce on 23/04/2017.
 */
public class PatientRegistrationController {

    @FXML
    private TabPane tabPane;

    @FXML
    public void initialize() {
        tabPane.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> {
            loadTab(newValue.intValue());
        });
        Tab tab = new Tab("Register Patient");
        tabPane.getTabs().add(0, tab);
        tabPane.getSelectionModel().select(tab);
    }

    private void loadTab(int index) {
        Tab selected = tabPane.getTabs().get(index);
        String[] tabResources = new String[]{"register-patient", "patient_queue"};
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource
                    ("main/resources/view/" + tabResources[index] + ".fxml"));
            Node node = loader.load();
            selected.setContent(node);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

