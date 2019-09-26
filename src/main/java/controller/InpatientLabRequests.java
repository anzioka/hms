package main.java.controller;

import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import main.java.model.LabRequest;

public class InpatientLabRequests {
    @FXML
    private TextField searchField;
    @FXML
    private TableView<LabRequest> tableView;
    @FXML
    private TableColumn<LabRequest, String> date, sample, testName, cost, status, options;

    @FXML
    private void initialize() {

    }

    @FXML
    private void onSendRequest() {

    }
}
