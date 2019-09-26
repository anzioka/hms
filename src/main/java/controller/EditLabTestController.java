package main.java.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import main.java.model.LabTest;
import main.java.model.LabTestFlag;
import main.java.util.AlertUtil;
import main.java.util.DBUtil;
import main.java.util.NumberUtil;

import java.util.List;

/**
 * Created by alfonce on 01/08/2017.
 */
public class EditLabTestController {
    @FXML
    private TextField testName, testCost, flagEdit, flagRangeEdit;
    @FXML
    private TableView<LabTestFlag> tableView;
    @FXML
    private TableColumn<LabTestFlag, String> flag, refRange, delete;
    private Stage stage;
    private LabTest test = null;

    @FXML
    public void initialize() {
        setUpTable();
    }

    private void setUpTable() {
        //widths
        flag.prefWidthProperty().bind(tableView.widthProperty().divide(2.5));
        refRange.prefWidthProperty().bind(tableView.widthProperty().divide(2.5));
        delete.prefWidthProperty().bind(tableView.widthProperty().divide(5));

        //placeholder
        Label label = new Label("No flags");
        label.getStyleClass().add("text-danger");
        tableView.setPlaceholder(label);
        delete.setCellFactory(param -> new TableCell<LabTestFlag, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                int index = getIndex();
                if (index >= 0 && index < tableView.getItems().size()) {
                    HBox hBox = new HBox(10.0);
                    ImageView imageView = new ImageView(new Image(getClass().getClassLoader().getResourceAsStream
                            ("main/resources/images/edit.png")));
                    imageView.setFitHeight(24);
                    imageView.setFitWidth(24);
                    imageView.setOnMouseClicked(event -> {
                        flagEdit.requestFocus();
                        flagEdit.setText(tableView.getItems().get(index).getName());
                        flagRangeEdit.setText(tableView.getItems().get(index).getDefaultVal());
                    });
                    hBox.getChildren().add(imageView);

                    imageView = new ImageView(new Image(getClass().getClassLoader().getResourceAsStream
                            ("main/resources/images/delete.png")));
                    imageView.setFitWidth(24);
                    imageView.setFitHeight(24);
                    imageView.setOnMouseClicked(event -> {
                        tableView.getItems().remove(index);
                    });
                    hBox.getChildren().add(imageView);
                    setGraphic(hBox);
                } else {
                    setGraphic(null);
                }
            }
        });

        flag.setCellValueFactory(param -> param.getValue().nameProperty());
        refRange.setCellValueFactory(param -> param.getValue().defaultValProperty());
    }

    @FXML
    private void onAddFlag() {
        String flagName = flagEdit.getText();
        if (flagName == null || flagName.isEmpty()) {
            AlertUtil.showAlert("Name Required", "Please enter name of flag", Alert.AlertType.ERROR);
        } else {
            LabTestFlag flag = searchFlag(flagName);
            if (flag != null) {
                flag.setName(flagName);
                flag.setDefaultVal(flagRangeEdit.getText());
            } else {
                flag = new LabTestFlag();
                flag.setName(flagName);
                flag.setDefaultVal(flagRangeEdit.getText());
                tableView.getItems().add(flag);
            }
            tableView.refresh();
            flagRangeEdit.clear();
            flagEdit.clear();
            flagEdit.requestFocus();
        }
    }

    private LabTestFlag searchFlag(String name) {
        for (LabTestFlag flag : tableView.getItems()) {
            if (flag.getName().toLowerCase().equals(name.toLowerCase())) {
                return flag;
            }
        }
        return null;
    }

    void setStage(Stage stage) {
        this.stage = stage;
    }

    void setLabTest(LabTest test) {
        if (test != null) {
            this.test = test;
            testName.setText(test.getName());
            testCost.setText("" + test.getCost());
        } else {
            this.test = new LabTest();
            this.test.setTestId(DBUtil.getNextAutoIncrementId("labtests"));
        }
    }

    private boolean validInput() {
        String errorMsg = "";
        if (testName.getText() == null || testName.getText().isEmpty()) {
            errorMsg += "Name field cannot be blank!\n";
        }
        double cost = NumberUtil.stringToDouble(testCost.getText());
        if (cost < 0) {
            errorMsg += "Invalid cost value!\n";
        }
        if (errorMsg.isEmpty()) {
            return true;
        }
        AlertUtil.showAlert("Input Error", errorMsg, Alert.AlertType.ERROR);
        return false;
    }

    @FXML
    private void onSave() {
        if (validInput()) {
            test.setName(testName.getText());
            test.setCost(NumberUtil.stringToDouble(testCost.getText()));

            for (LabTestFlag testFlag : tableView.getItems()) {
                testFlag.setTestId(test.getTestId());
            }
            boolean error = false;
            if (DBUtil.saveLabTest(test)) {
                //remove all flags associated with test
                String sql = "delete from lab_test_flags where TestId = " + test.getTestId();
                if (DBUtil.executeStatement(sql)) {
                    if (DBUtil.saveLabTestFlags(tableView.getItems())) {
                        AlertUtil.showAlert("Lab Test", "Lab test information successfully saved", Alert.AlertType.INFORMATION);
                        stage.close();
                    } else {
                        error = true;
                    }
                } else {
                    error = true;
                }
            } else {
                error = true;
            }
            if (error) {
                AlertUtil.showAlert("Error", "An error occurred while attempting to save changes", Alert.AlertType.ERROR);
            }
        }
    }

    void setFlags(List<LabTestFlag> labTestFlags) {
        tableView.setItems(FXCollections.observableArrayList(labTestFlags));
    }

    public LabTest getTest() {
        return test;
    }

    List<LabTestFlag> getLabTestFlags() {
        return tableView.getItems();
    }
}
