package main.java.controller;

import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import main.java.dao.SupplierDAO;
import main.java.model.Supplier;
import main.java.util.AlertUtil;
import main.java.util.DBUtil;

public class SupplierController {
    @FXML
    private VBox editBox;

    @FXML
    private TableView<Supplier> supplierTableView;
    @FXML
    private TableColumn<Supplier, String> nameCol, addressCol, phoneNumberCol, emailCol, contactCol;
    @FXML
    private GridPane gridPane;
    @FXML
    private TextField name, address, phoneNumber, email, contactPerson;
    @FXML
    private Label editTitleLabel;
    private Supplier currentSupplier = null;

    @FXML
    private void initialize() {
        toggleEditing(false);
        setUpTable();
        getData();
    }

    private void getData() {
        Task<ObservableList<Supplier>> task = new Task<ObservableList<Supplier>>() {
            @Override
            protected ObservableList<Supplier> call() {
                return SupplierDAO.getSuppliers();
            }
        };
        task.setOnSucceeded(event -> {
            supplierTableView.setItems(task.getValue());
        });
        new Thread(task).start();
    }

    private void setUpTable() {
        nameCol.setCellValueFactory(param -> param.getValue().nameProperty());
        addressCol.setCellValueFactory(param -> param.getValue().addressProperty());
        phoneNumberCol.setCellValueFactory(param -> param.getValue().phoneNumberProperty());
        emailCol.setCellValueFactory(param -> param.getValue().emailProperty());
        contactCol.setCellValueFactory(param -> param.getValue().contactPersonProperty());

        //place holder
        Label label = new Label("No suppliers found!");
        label.getStyleClass().add("missing-content");
        supplierTableView.setPlaceholder(label);
    }

    private void toggleEditing(boolean edit) {
        editBox.setVisible(edit);
        editBox.setManaged(edit);

    }

    private boolean validInput() {
        String errorMsg = "";
        if (name.getText() == null || name.getText().isEmpty()) {
            errorMsg += "Name field is required!\n";
        }
        if (errorMsg.isEmpty()) {
            return true;
        } else {
            AlertUtil.showAlert("Error", errorMsg, Alert.AlertType.ERROR);
            return false;
        }
    }

    @FXML
    private void onEdit() {
        currentSupplier = supplierTableView.getSelectionModel().getSelectedItem();
        if (currentSupplier != null) {
            setSupplierDetails();
            editTitleLabel.setText("Edit Supplier '" + currentSupplier.getName() + "'");
            toggleEditing(true);
        } else {
            AlertUtil.showAlert("Select Supplier", "Please select supplier to edit", Alert.AlertType.WARNING);
        }
    }

    private void setSupplierDetails() {
        name.setText(currentSupplier.getName());
        phoneNumber.setText(currentSupplier.getPhoneNumber());
        address.setText(currentSupplier.getAddress());
        contactPerson.setText(currentSupplier.getContactPerson());
        email.setText(currentSupplier.getEmail());
    }

    @FXML
    private void onNewSupplier() {
        for (Node node : gridPane.getChildren()) {
            if (node instanceof TextField) {
                ((TextField) node).clear();
            }
        }
        editTitleLabel.setText("New Supplier Registration");
        toggleEditing(true);
        currentSupplier = null;
    }

    @FXML
    private void onCloseEditBox() {
        currentSupplier = null;
        toggleEditing(false);
    }

    @FXML
    private void onSave() {
        if (validInput()) {
            int newSupplierId = DBUtil.getNextAutoIncrementId("suppliers");
            if (currentSupplier == null) {
                currentSupplier = new Supplier(name.getText(), address.getText(), phoneNumber.getText(), email.getText
                        (), contactPerson.getText());
                currentSupplier.setSupplierId(newSupplierId);
            } else {
                currentSupplier.setName(name.getText());
                currentSupplier.setAddress(address.getText());
                currentSupplier.setPhoneNumber(phoneNumber.getText());
                currentSupplier.setEmail(email.getText());
                currentSupplier.setContactPerson(contactPerson.getText());
            }
            if (DBUtil.addSupplier(currentSupplier)) {
                if (currentSupplier.getSupplierId() == newSupplierId) {
                    supplierTableView.getItems().add(currentSupplier);
                } else {
                    //supplier already on table.
                    supplierTableView.refresh();
                }
                toggleEditing(false);
                currentSupplier = null;
                AlertUtil.showAlert("Success", "Details have been successfully saved!", Alert.AlertType.INFORMATION);
            } else {
                AlertUtil.showAlert("Error", "An error occurred while attempting to save details\n", Alert.AlertType.ERROR);
            }
        }
    }
}
