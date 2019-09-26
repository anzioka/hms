package main.java.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import main.java.dao.MedicineDAO;
import main.java.model.Medicine;
import main.java.util.CurrencyUtil;

public class MedicineListController {
    @FXML
    private VBox container;
    @FXML
    private TableView<Medicine> tableView;
    @FXML
    private TableColumn<Medicine, Integer> code, storeQuantity, shopQuantity;
    @FXML
    private TableColumn<Medicine, String> name, sellingPrice;
    @FXML
    private TextField searchField;
    private ObservableList<Medicine> medicines;

    @FXML
    private void initialize() {
        setUpTable();
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterResults(newValue);
        });
        getMedicines();
    }

    private void filterResults(String newValue) {
        if (newValue == null || newValue.isEmpty()) {
            tableView.setItems(medicines);
        } else {
            ObservableList<Medicine> filtered = FXCollections.observableArrayList();
            for (Medicine medicine : medicines) {
                if (medicine.getName().toLowerCase().contains(newValue.toLowerCase())) {
                    filtered.add(medicine);
                }
            }
            tableView.setItems(filtered);
        }
    }

    private void getMedicines() {
        Task<ObservableList<Medicine>> task = new Task<ObservableList<Medicine>>() {
            @Override
            protected ObservableList<Medicine> call() {
                return MedicineDAO.getMedicineList();
            }
        };
        task.setOnSucceeded(event -> {
            medicines = task.getValue();
            tableView.setItems(medicines);
        });
        new Thread(task).start();
    }

    private void setUpTable() {
        Label label = new Label("No drugs found in system!");
        label.getStyleClass().add("text-danger");
        tableView.setPlaceholder(label);

        code.setCellValueFactory(param -> param.getValue().drugCodeProperty().asObject());
        name.setCellValueFactory(param -> param.getValue().nameProperty());
        sellingPrice.setCellValueFactory(param -> CurrencyUtil.getStringProperty(param.getValue().getSellingPrice()));
        storeQuantity.setCellValueFactory(param -> param.getValue().storeQuantityProperty().asObject());
        shopQuantity.setCellValueFactory(param -> param.getValue().shopQuantityProperty().asObject());
    }
}
