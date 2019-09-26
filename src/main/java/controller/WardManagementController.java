package main.java.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import main.Main;
import main.java.dao.InpatientDao;
import main.java.model.Permission;
import main.java.model.Ward;

import java.io.IOException;

public class WardManagementController {
    private ObservableList<Ward> wards = FXCollections.observableArrayList();
    @FXML
    private FlowPane flowPane;
    @FXML
    private TabPane tabPane;
    @FXML
    private Button createWardBtn;
    @FXML
    private void initialize() {
        //get data
        getData();
        createWardBtn.setDisable(!Main.userPermissions.get(Permission.EDIT_WARD));
    }

    public void getData() {
        Task<ObservableList<Ward>> task = new Task<ObservableList<Ward>>() {
            @Override
            protected ObservableList<Ward> call() throws Exception {
                return InpatientDao.getWards();
            }
        };

        task.setOnSucceeded(event -> {
            wards = task.getValue();
            if (wards.isEmpty()) {
                Label label = new Label("No wards found. Click on button above to create a new ward");
                label.getStyleClass().add("text-danger");
                flowPane.getChildren().clear();
                flowPane.getChildren().add(label);
                flowPane.setAlignment(Pos.CENTER);
            } else {
                flowPane.setAlignment(Pos.TOP_CENTER);
                setDetails();
            }
        });

        new Thread(task).start();
    }

    private void setDetails() {
        flowPane.getChildren().clear();
        for (Ward ward : wards) {

            VBox vBox = new VBox();
            vBox.getStyleClass().add("ward");
            vBox.setId(ward.getId() + "");

            Label wardNameLabel = new Label(ward.getName().toUpperCase());
            wardNameLabel.getStyleClass().add("ward_name");

            Label numBedsLabel = new Label(ward.getNumBeds() + " BEDS");

            int numAvailableBeds = ward.getNumBeds() - ward.getNumOccupiedBeds();
            Label numAvailableBedsLabel = new Label(numAvailableBeds + " available");
            if (numAvailableBeds == 0) {
                numAvailableBedsLabel.setStyle("-fx-text-fill: #ff4444");
            } else {
                numAvailableBedsLabel.getStyleClass().add("color-success-dark");
            }
            vBox.getChildren().addAll(wardNameLabel, numBedsLabel, numAvailableBedsLabel);
            flowPane.getChildren().add(vBox);

            vBox.setOnMouseClicked(event -> showWardDetails(((VBox) event.getSource()).getId()));
        }
    }

    private void showWardDetails(String id) {
        showTab(id);
    }

    private void showTab(String wardId) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("main/resources/view/edit-ward" +
                    ".fxml"));
            Node node = loader.load();

            Tab tab = new Tab("Create Ward");
            tab.setClosable(true);
            tab.setContent(node);

            for (int i = 1; i < tabPane.getTabs().size(); i++) {
                tabPane.getTabs().remove(i);
            }
            tabPane.getTabs().add(tab);
            tabPane.getSelectionModel().select(tab);

            //controller
            EditWardController controller = loader.getController();
            controller.setParent(this);
            if (wardId != null) {
                controller.setWard(getWard(wardId));
                tab.setText("View Ward");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Ward getWard(String wardId) {
        for (Ward ward : wards) {
            if (ward.getId() == Integer.parseInt(wardId)) {
                return ward;
            }
        }
        return null;
    }

    @FXML
    private void onCreateNewWard() {
        showTab(null);
    }

    boolean duplicateWard(String wardName, Ward currentWard) {
        for (Ward ward : wards) {
            if (ward.getName().toLowerCase().equals(wardName.toLowerCase())) {
                if (currentWard != null) {
                    if (currentWard.getId() != ward.getId()) {
                        return true;
                    }
                } else {
                    return true;
                }

            }
        }
        return false;
    }
}
