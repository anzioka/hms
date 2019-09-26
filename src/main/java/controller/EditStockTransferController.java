package main.java.controller;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import main.Main;
import main.java.dao.MedicineDAO;
import main.java.dao.TransferDao;
import main.java.model.Medicine;
import main.java.model.MedicineLocation;
import main.java.model.StockTransfer;
import main.java.util.AlertUtil;
import main.java.util.DBUtil;
import main.java.util.NumberUtil;
import org.controlsfx.control.textfield.AutoCompletionBinding;
import org.controlsfx.control.textfield.TextFields;

import java.time.LocalDate;

public class EditStockTransferController {
    private Stage stage;
    private StockTransfer stockTransfer;
    @FXML
    private ChoiceBox<MedicineLocation> retailUnitChoiceBox;
    @FXML
    private TextField medicineNameField, quantityField;
    @FXML
    private TableView<StockTransfer> stockTransferTableView;
    @FXML
    private TableColumn<StockTransfer, String> drugName;
    @FXML
    private TableColumn<StockTransfer, Integer> quantityTransferred, storeQty, shopQty, drugId;
    private Medicine currentMedicine;
    private ObservableList<Medicine> medicines = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        getData();
        setUpTable();
        retailUnitChoiceBox.setItems(FXCollections.observableArrayList(MedicineLocation.values()));
        retailUnitChoiceBox.getSelectionModel().select(MedicineLocation.STORE);
        retailUnitChoiceBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue)
                -> {
            stockTransferTableView.getItems().clear();
            currentMedicine = null;
            quantityField.clear();
            medicineNameField.clear();
            medicineNameField.requestFocus();
        });
    }

    private void getData() {
        Task<ObservableList<Medicine>> task = new Task<ObservableList<Medicine>>() {
            @Override
            protected ObservableList<Medicine> call() {
                return MedicineDAO.getMedicineList();
            }
        };
        task.setOnSucceeded(event -> {
            medicines = task.getValue();
            AutoCompletionBinding<Medicine> binding = TextFields.bindAutoCompletion(medicineNameField, task.getValue());
            binding.setOnAutoCompleted(autoCompletionEvent -> {
                currentMedicine = autoCompletionEvent.getCompletion();
                quantityField.requestFocus();
            });
        });
        new Thread(task).start();
    }

    private void setUpTable() {
        for (TableColumn column : stockTransferTableView.getColumns()) {
            if (column == drugName) {
                column.prefWidthProperty().bind(stockTransferTableView.widthProperty().divide(3));
            } else {
                column.prefWidthProperty().bind(stockTransferTableView.widthProperty().divide(6));
            }
        }
        drugName.setCellValueFactory(param -> param.getValue().drugNameProperty());
        quantityTransferred.setCellValueFactory(param -> param.getValue().quantityProperty().asObject());
        storeQty.setCellValueFactory(param -> new SimpleIntegerProperty(MedicineDAO.getQuantity(medicines, param
                .getValue().getDrugId(), MedicineLocation.STORE)).asObject());
        shopQty.setCellValueFactory(param -> new SimpleIntegerProperty(MedicineDAO.getQuantity(medicines, param
                .getValue().getDrugId(), MedicineLocation.SHOP)).asObject());
        drugId.setCellValueFactory(param -> param.getValue().drugIdProperty().asObject());
    }

    Stage getStage() {
        return stage;
    }

    void setStage(Stage stage) {
        this.stage = stage;
    }

    @FXML
    private void onDeleteEntry() {
        StockTransfer stockTransfer = stockTransferTableView.getSelectionModel().getSelectedItem();
        if (stockTransfer != null) {
            stockTransferTableView.getItems().remove(stockTransfer);
        }
    }

    @FXML
    private void onClose() {
        stage.close();
    }

    @FXML
    private void onSaveTransfer() {
        if (stockTransferTableView.getItems().isEmpty()) {
            return;
        }
        int transferId = TransferDao.getNextTransferId();
        for (StockTransfer stockTransfer : stockTransferTableView.getItems()) {
            stockTransfer.setDateCreated(LocalDate.now());
            stockTransfer.setTransferNo(transferId);
            stockTransfer.setTransferredBy(Main.currentUser.getFirstName());
            stockTransfer.setOrigin(retailUnitChoiceBox.getValue());
        }

        if (DBUtil.saveStockTransfer(stockTransferTableView.getItems())) {
            AlertUtil.showAlert("Stock Transfer", "Changes have been successfully saved", Alert.AlertType.INFORMATION);
            onClose();
        } else {
            AlertUtil.showAlert("Stock Transfer", "An error occurred while attempting to save transfer", Alert
                    .AlertType.ERROR);
        }
    }

    @FXML
    private void onAddToTransfer() {
        if (currentMedicine == null) {
            AlertUtil.showAlert("Error", "Please enter valid name of medicine to transfer", Alert.AlertType.ERROR);
            return;
        }
        if (validInput()) {
            StockTransfer stockTransfer = new StockTransfer();
            stockTransfer.setDrugName(currentMedicine.getName());
            stockTransfer.setDrugId(currentMedicine.getDrugCode());
            stockTransfer.setQuantity(NumberUtil.stringToInt(quantityField.getText()));

            stockTransferTableView.getItems().add(stockTransfer);
            quantityField.clear();
            medicineNameField.clear();
            medicineNameField.requestFocus();
            currentMedicine = null;
        }
    }

    private boolean validInput() {
        String errorMsg = "";
        MedicineLocation retailUnit = retailUnitChoiceBox.getValue();
        int qty = NumberUtil.stringToInt(quantityField.getText());
        if (qty < 0) {
            errorMsg += "Invalid quantity value!\n";
        } else if (retailUnit == MedicineLocation.STORE && qty > MedicineDAO.getQuantity(medicines, currentMedicine
                .getDrugCode(), MedicineLocation.STORE)) {
            errorMsg += "The quantity to transfer should be less than the quantity in the store\n";
        } else if (retailUnit == MedicineLocation.SHOP && qty > MedicineDAO.getQuantity(medicines, currentMedicine
                .getDrugCode(), MedicineLocation.SHOP)) {
            errorMsg += "The quantity to transfer should be less than the quantity in the shop";
        }
        if (errorMsg.isEmpty()) {
            return true;
        } else {
            AlertUtil.showAlert("Error", errorMsg, Alert.AlertType.ERROR);
            return false;
        }
    }
}
