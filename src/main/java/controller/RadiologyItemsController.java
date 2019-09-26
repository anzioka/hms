package main.java.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import main.Main;
import main.java.dao.RadiologyDao;
import main.java.model.Permission;
import main.java.model.RadiologyCategory;
import main.java.model.RadiologyItem;
import main.java.util.AlertUtil;
import main.java.util.CurrencyUtil;
import main.java.util.DBUtil;

import java.util.Optional;

public class RadiologyItemsController {
    private final String ALL_RADIOLOGY_CATEGORIES = "All categories";
    @FXML
    private ChoiceBox<String> radiologyCategoryChoiceBox, editCategoryChoiceBox;
    @FXML
    private Button  addItemBtn;
    @FXML
    private TableView<RadiologyItem> tableView;
    @FXML
    private TableColumn<RadiologyItem, String> description, cost, options, category;
    @FXML
    private TextField searchField;
    @FXML
    private HBox editBox;
    @FXML
    private TextField editCost, editDescription;
    private ObservableList<RadiologyItem> radiologyItems;
    private RadiologyItem radiologyItem;
    private boolean newItem = true;
    @FXML
    private void initialize() {
        toggleEditBoxVisibility(false);
        setUpTable();
        addItemBtn.setDisable(!Main.userPermissions.get(Permission.EDIT_RADIOLOGY_ITEMS));

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterByDescription(newValue);
        });

        radiologyCategoryChoiceBox.getItems().add(ALL_RADIOLOGY_CATEGORIES);
        for (RadiologyCategory category : RadiologyCategory.values()) {
            radiologyCategoryChoiceBox.getItems().add(category.toString());
            editCategoryChoiceBox.getItems().add(category.toString());
        }
        radiologyCategoryChoiceBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            filterByCategory(newValue);
        });
        radiologyCategoryChoiceBox.getSelectionModel().select(0);
        getRadiologyItems();
    }

    private void filterByDescription(String newValue) {
        //empty string
        if (newValue == null || newValue.isEmpty()) {
            filterByCategory(radiologyCategoryChoiceBox.getValue());
        } else{
            ObservableList<RadiologyItem> filtered = FXCollections.observableArrayList();
            for (RadiologyItem radiologyItem : radiologyItems) {
                if (radiologyItem.getCategory().equals(radiologyCategoryChoiceBox.getValue())) {
                    if (radiologyItem.getDescription().toLowerCase().contains(newValue.toLowerCase())) {
                        filtered.add(radiologyItem);
                    }
                }
            }
            tableView.setItems(filtered);
        }
    }

    private void filterByCategory(String newValue) {
        if ( newValue == null || newValue.equals(ALL_RADIOLOGY_CATEGORIES)) {
            tableView.setItems(radiologyItems);
        } else{
            ObservableList<RadiologyItem> filteredList = FXCollections.observableArrayList();
            for (RadiologyItem item : radiologyItems) {
                if (item.getCategory().equals(newValue)) {
                    filteredList.add(item);
                }
            }
            tableView.setItems(filteredList);
        }
    }

    private void getRadiologyItems() {
        Task<ObservableList<RadiologyItem>> task = new Task<ObservableList<RadiologyItem>>() {
            @Override
            protected ObservableList<RadiologyItem> call() {
                return RadiologyDao.getAllRadiologyItems();
            }
        };
        task.setOnSucceeded(event -> {
            radiologyItems = task.getValue();
            tableView.setItems(task.getValue());
        });
        new Thread(task).start();
    }

    private void setUpTable() {
        Label label = new Label("No items found!");
        label.getStyleClass().add("text-danger");
        tableView.setPlaceholder(label);

        category.setCellValueFactory(param -> param.getValue().categoryProperty());
        category.prefWidthProperty().bind(tableView.widthProperty().divide(6));

        description.setCellValueFactory(param -> param.getValue().descriptionProperty());
        description.prefWidthProperty().bind(tableView.widthProperty().divide(3));

        cost.setCellValueFactory(param -> CurrencyUtil.getStringProperty(param.getValue().getCost()));
        cost.prefWidthProperty().bind(tableView.widthProperty().divide(6));

        options.prefWidthProperty().bind(tableView.widthProperty().divide(3));
        options.setCellFactory(param -> new TableCell<RadiologyItem, String>(){
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                int index = getIndex();
                if (index >= 0 && index < tableView.getItems().size()) {
                    Button edit = new Button("Edit");
                    edit.getStyleClass().add("btn-info-outline");
                    edit.setOnAction(event -> {
                        editRadiologyItem(tableView.getItems().get(index));
                    });
                    edit.setDisable(!Main.userPermissions.get(Permission.EDIT_RADIOLOGY_ITEMS));

                    Button delete = new Button("Delete");
                    delete.getStyleClass().add("btn-danger-outline");
                    delete.setOnAction(event -> {
                        deleteRadiologyItem(tableView.getItems().get(index));
                    });
                    delete.setDisable(!Main.userPermissions.get(Permission.EDIT_RADIOLOGY_ITEMS));
                    HBox hBox = new HBox(5.0, edit, delete);
                    hBox.setAlignment(Pos.CENTER);
                    setGraphic(hBox);
                } else{
                    setGraphic(null);
                }
            }
        });
    }
    private void deleteRadiologyItem(RadiologyItem radiologyItem) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to delete the radiology item?",
                ButtonType.YES, ButtonType.CANCEL);
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.YES) {
            if (DBUtil.executeStatement("delete from radiology_items where id = " + radiologyItem.getId())) {
                AlertUtil.showAlert("", "Item successfully deleted", Alert.AlertType.INFORMATION);
                DBUtil.saveActivity("Deleted radiology item { Category : " + radiologyItem.getCategory() + ", " +
                        "Description : " + radiologyItem.getDescription() + " }");
                tableView.getItems().remove(radiologyItem);
            } else{
                AlertUtil.showGenericError();
            }
        }
    }

    private void editRadiologyItem(RadiologyItem radiologyItem) {
        newItem = false;
        this.radiologyItem = radiologyItem;
        editCost.setText(CurrencyUtil.formatCurrency(radiologyItem.getCost()));
        editDescription.setText(radiologyItem.getDescription());
        editCategoryChoiceBox.setValue(radiologyItem.getCategory());
        toggleEditBoxVisibility(true);

    }
    private void toggleEditBoxVisibility(boolean visible){
        editBox.setVisible(visible);
        editBox.setManaged(visible);
    }
    @FXML
    private void onSave() {
        if (validInput()) {
            if (radiologyItem == null) {
                radiologyItem = new RadiologyItem();
            }
            radiologyItem.setCost(CurrencyUtil.parseCurrency(editCost.getText()));
            radiologyItem.setCategory(editCategoryChoiceBox.getValue());
            radiologyItem.setDescription(editDescription.getText());
            if (DBUtil.saveRadiologyItem(radiologyItem)) {
                if (!newItem) {
                    AlertUtil.showAlert("", "Radiology item successfully updated!", Alert.AlertType.INFORMATION);
                    DBUtil.saveActivity("Updated radiology item { Category : " + radiologyItem.getCategory() + ", " +
                            "Description : " + radiologyItem.getDescription() + " }");
                } else{
                    AlertUtil.showAlert("", "Radiology item successfully added!", Alert.AlertType.INFORMATION);
                    DBUtil.saveActivity("Created newradiology item { Category : " + radiologyItem.getCategory() + "," +
                            " " +
                            "Description : " + radiologyItem.getDescription() + " }");
                    tableView.getItems().add(radiologyItem);
                }
            } else{
                AlertUtil.showGenericError();
            }
            toggleEditBoxVisibility(false);
            editDescription.clear();
            radiologyItem = null;
            newItem = true;
            editCost.clear();
            editCategoryChoiceBox.getSelectionModel().clearSelection();
        }
    }

    private boolean validInput() {
        String errorMsg = "";
        if (editCategoryChoiceBox.getValue() == null) {
            errorMsg += "Item category required!\n";
        }
        if (editDescription.getText() == null || editDescription.getText().isEmpty()) {
            errorMsg += "Description required!\n";
        }
        if (CurrencyUtil.parseCurrency(editCost.getText()) < 0) {
            errorMsg += "Invalid cost!";
        }
        if (errorMsg.isEmpty()) {
            return true;
        }
        AlertUtil.showAlert("", errorMsg, Alert.AlertType.ERROR);
        return false;
    }

    @FXML
    private void onAddItem() {
        toggleEditBoxVisibility(true);
    }
}

