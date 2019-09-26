package main.java.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import main.java.dao.AllergiesDao;
import main.java.util.AlertUtil;
import main.java.util.DBUtil;
import org.controlsfx.control.textfield.TextFields;

public class AllergiesController {
    @FXML
    private VBox container;
    @FXML
    private TableView<String> tableView;
    @FXML
    private CheckBox noAllergies;
    @FXML
    private TableColumn<String, String> name, options;
    @FXML
    private TextField editField;
    private String patientId;

    @FXML
    private void initialize() {
        setUpTable();
        setUpAutocomplete();
        noAllergies.selectedProperty().addListener((observable, oldValue, newValue) -> {
            for (int i = 2; i < container.getChildren().size(); i++) {
                container.getChildren().get(i).setDisable(newValue);
            }
        });
    }

    private void setUpAutocomplete() {
        Task<ObservableList<String>> task = new Task<ObservableList<String>>() {
            @Override
            protected ObservableList<String> call() throws Exception {
                return AllergiesDao.getAllAllergies();
            }
        };
        task.setOnSucceeded(event -> {
            TextFields.bindAutoCompletion(editField, task.getValue());
        });
        new Thread(task).start();
    }

    private void getData() {
        //TODO get allergies for this patient
        Task<ObservableList<String>> task = new Task<ObservableList<String>>() {
            @Override
            protected ObservableList<String> call() {
                return AllergiesDao.getAllergies(patientId);
            }
        };
        task.setOnSucceeded(event -> {
            tableView.setItems(task.getValue());
        });
        new Thread(task).start();
    }

    @FXML
    private void onAdd() {
        //TODO : add allergies to table and save to db
        String allergy = editField.getText();
        if (allergy == null || allergy.isEmpty()) {
            AlertUtil.showAlert("Invalid Name", "Please enter the name of allergy", Alert.AlertType.ERROR);
        } else {
            if (!alreadyAdded(allergy)) {
                if (DBUtil.saveAllergy(allergy, patientId)) {
                    tableView.getItems().add(allergy);
                    editField.clear();
                    editField.requestFocus();
                } else {
                    AlertUtil.showAlert("Error", "An error occurred while attempting to save allergy", Alert.AlertType.ERROR);
                }
            }
        }
    }

    private boolean alreadyAdded(String allergyName) {
        for (String allergy : tableView.getItems()) {
            if (allergy.toLowerCase().equals(allergyName.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    private void setUpTable() {
        //col widths

        name.setCellValueFactory(param -> new SimpleStringProperty(param.getValue()));
        options.setCellFactory(param -> new TableCell<String, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                int index = getIndex();
                if (index >= 0 && index < tableView.getItems().size()) {
                    Button button = new Button("Delete");
                    button.getStyleClass().add("btn-danger-outline");
                    button.setOnAction(event -> {
                        String sql = "delete from allergies where patient_id = '" + patientId + "' and name = '" + tableView.getItems().get(index) + "'";
                        if (DBUtil.executeStatement(sql)) {
                            tableView.getItems().remove(index);
                        }
                    });
                    setGraphic(button);
                } else {
                    setGraphic(null);
                }
            }
        });
        //placeholder
        Label label = new Label("No allergies found!");
        label.getStyleClass().add("text-danger");
        tableView.setPlaceholder(label);
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
        getData();

    }

}
