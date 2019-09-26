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
import main.java.dao.PurchasesDao;
import main.java.dao.SupplierDAO;
import main.java.model.Medicine;
import main.java.model.MedicineLocation;
import main.java.model.Purchase;
import main.java.model.Supplier;
import main.java.util.*;
import org.controlsfx.control.textfield.AutoCompletionBinding;
import org.controlsfx.control.textfield.TextFields;

import java.time.LocalDate;

public class ReceiveDrugsController {
    private Stage stage;
    private Purchase purchase;
    @FXML
    private TextField invoiceNo, searchField, quantity, buyingPrice, discount, batchNo;
    @FXML
    private ChoiceBox<MedicineLocation> pharmacyLocationChoiceBox;
    @FXML
    private ChoiceBox<Supplier> supplierChoiceBox;
    @FXML
    private DatePicker deliveryDate, expiryDate;
    @FXML
    private TableView<Purchase> purchaseTableView;
    @FXML
    private TableColumn<Purchase, String> name, totalCol, batchNoCol, expiryDateCol;
    @FXML
    private TableColumn<Purchase, Integer> quantityAfterColumn, quantityAddedCol;
    @FXML
    private TableColumn<Purchase, Double> buyingPriceCol, discountCol;
    @FXML
    private Label totalCostLabel, dialogTitle;

    private Medicine currentMedicine;
    private boolean receivingSuccessful = false;
    private int orderId;

    Stage getStage() {
        return stage;
    }

    void setStage(Stage stage) {
        this.stage = stage;
    }

    Purchase getPurchase() {
        return purchase;
    }

    private void setPurchase(Purchase purchase) {
        this.purchase = purchase;
    }

    @FXML
    private void initialize() {
        setUpTable();
        pharmacyLocationChoiceBox.setItems(FXCollections.observableArrayList(MedicineLocation.values()));
        pharmacyLocationChoiceBox.getSelectionModel().select(MedicineLocation.STORE);
        expiryDate.setConverter(DateUtil.getDatePickerConverter());
        deliveryDate.setConverter(DateUtil.getDatePickerConverter());
        deliveryDate.setValue(LocalDate.now());
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            resetFields();
        });
        getData();
    }

    private void resetFields() {
        quantity.clear();
        buyingPrice.setText("0");
        discount.setText("0");
        expiryDate.setValue(null);
        batchNo.clear();
    }

    private void getData() {
        Task<ObservableList<Supplier>> task = new Task<ObservableList<Supplier>>() {
            @Override
            protected ObservableList<Supplier> call() {
                return SupplierDAO.getSuppliers();
            }
        };
        task.setOnSucceeded(event -> {
            supplierChoiceBox.setItems(task.getValue());
        });
        new Thread(task).start();

        Task<ObservableList<Medicine>> medicineListTask = new Task<ObservableList<Medicine>>() {
            @Override
            protected ObservableList<Medicine> call() {
                return MedicineDAO.getMedicineList();
            }
        };
        medicineListTask.setOnSucceeded(event -> {
            AutoCompletionBinding<Medicine> binding = TextFields.bindAutoCompletion(searchField, medicineListTask
                    .getValue());
            binding.setOnAutoCompleted(autoCompletionEvent -> {
                quantity.requestFocus();
                currentMedicine = autoCompletionEvent.getCompletion();
                buyingPrice.setText(currentMedicine.getBuyingPrice() + "");
            });
        });
        new Thread(medicineListTask).start();
    }

    private void setUpTable() {
        //col widths
        for (TableColumn column : purchaseTableView.getColumns()) {
            if (column != name) {
                column.prefWidthProperty().bind(purchaseTableView.widthProperty().divide(7));
            } else {
                column.prefWidthProperty().bind(purchaseTableView.widthProperty().divide(3.5));
            }
        }
        //data
        name.setCellValueFactory(param -> param.getValue().drugNameProperty());
        totalCol.setCellValueFactory(param -> param.getValue().totalPriceProperty());
        quantityAfterColumn.setCellValueFactory(param -> new SimpleIntegerProperty(param.getValue().getQuantity() +
                param.getValue().getCurrentQty()).asObject());
        quantityAddedCol.setCellValueFactory(param -> param.getValue().quantityProperty().asObject());
        buyingPriceCol.setCellValueFactory(param -> param.getValue().unitPriceProperty().asObject());
        discountCol.setCellValueFactory(param -> param.getValue().discountProperty().asObject());
        expiryDateCol.setCellValueFactory(param -> param.getValue().expiryDateProperty());
        batchNoCol.setCellValueFactory(param -> param.getValue().batchNoProperty());
        //place holder
        Label label = new Label("No items added");
        label.getStyleClass().add("missing-content");
        purchaseTableView.setPlaceholder(label);
    }

    private void setTotalCostLabel() {
        double total = 0;
        for (Purchase purchase : purchaseTableView.getItems()) {
            total += purchase.getTotalPrice();
        }
        totalCostLabel.setText("Ksh. " + CurrencyUtil.formatCurrency(total));
    }

    @FXML
    private void onAddToPurchase() {
        if (currentMedicine == null) {
            AlertUtil.showAlert("Error", "Please enter a valid medicine name", Alert.AlertType.ERROR);
            return;
        }
        if (validInput()) {
            Purchase purchase = new Purchase();
            double discountVal = NumberUtil.stringToDouble(discount.getText());
            if (discountVal < 0) {
                discountVal = 0;
            }
            MedicineLocation location = pharmacyLocationChoiceBox.getValue();
            if (location == MedicineLocation.STORE) {
                purchase.setCurrentQty(currentMedicine.getStoreQuantity());
            } else {
                purchase.setCurrentQty(currentMedicine.getShopQuantity());
            }
            purchase.setDrugName(currentMedicine.getName());
            purchase.setDiscount(discountVal);
            purchase.setQuantity(NumberUtil.stringToInt(quantity.getText()));
            purchase.setUnitPrice(NumberUtil.stringToDouble(buyingPrice.getText()));
            purchase.setTotalPrice((100 - purchase.getDiscount()) / 100 * purchase.getQuantity() * purchase.getUnitPrice());
            purchase.setBatchNo(batchNo.getText());
            purchase.setExpiryDate(expiryDate.getValue());
            purchase.setDrugId(currentMedicine.getDrugCode());

            purchaseTableView.getItems().add(purchase);
            setTotalCostLabel();
            resetFields();
            searchField.clear();
            searchField.requestFocus();
        }
    }

    private boolean validInput() {
        String errorMsg = "";

        double buyingPriceVal = NumberUtil.stringToDouble(buyingPrice.getText());
        if (buyingPriceVal < 0) {
            errorMsg += "Invalid buying price value!\n";
        }

        int qty = NumberUtil.stringToInt(quantity.getText());
        if (qty < 0) {
            errorMsg += "Invalid quantity value!\n";
        }

        LocalDate expiry = expiryDate.getValue();
        if (expiry != null) {
            if (expiry.isBefore(LocalDate.now())) {
                errorMsg += "Expiry date cannot be before today!\n";
            }
        }
        if (errorMsg.isEmpty()) {
            return true;
        } else {
            AlertUtil.showAlert("Input Error", errorMsg, Alert.AlertType.ERROR);
            return false;
        }
    }

    @FXML
    private void onClose() {
        stage.close();
    }

    @FXML
    private void onSave() {

        if (purchaseTableView.getItems().isEmpty()) {
            return;
        }

        String errorMsg = "";
        String invoice = invoiceNo.getText();
        if (invoice == null || invoice.isEmpty()) {
            errorMsg += "Please enter a valid invoice number\n";
            invoiceNo.requestFocus();
        }
        Supplier supplier = supplierChoiceBox.getValue();
        if (supplier == null) {
            errorMsg += "Please select supplier\n";
            supplierChoiceBox.requestFocus();
        }

        if (!errorMsg.isEmpty()) {
            AlertUtil.showAlert("Error", errorMsg, Alert.AlertType.ERROR);
            return;
        }

        LocalDate dateDelivered = deliveryDate.getValue();
        if (dateDelivered == null) {
            dateDelivered = LocalDate.now();
        }
        MedicineLocation medicineLocation = pharmacyLocationChoiceBox.getValue();
        int purchaseId = PurchasesDao.getNextPurchaseId();
        for (Purchase purchase : purchaseTableView.getItems()) {
            purchase.setLocation(medicineLocation);
            purchase.setDateDelivered(dateDelivered);
            assert supplier != null;
            purchase.setSupplierId(supplier.getSupplierId());
            purchase.setPurchaseId(purchaseId);
            purchase.setInvoiceNumber(invoice);
            purchase.setOrderId(orderId);
        }

        //TODO entry in creditor_journal (later maybe)
        if (DBUtil.savePurchase(purchaseTableView.getItems())) {
            AlertUtil.showAlert("Purchase Saved", "Stock successfully updated", Alert.AlertType.INFORMATION);
            //for the table
            createSummaryPurchaseItem(purchaseId);
            receivingSuccessful = true;
            onClose();
        } else {
            AlertUtil.showAlert("Error", "An error occurred while trying to update stock", Alert.AlertType.ERROR);
        }
    }

    private void createSummaryPurchaseItem(int purchaseId) {
        Purchase purchase = new Purchase();
        purchase.setPurchaseId(purchaseId);
        purchase.setInvoiceNumber(invoiceNo.getText());
        purchase.setDateDelivered(deliveryDate.getValue());
        purchase.setReceivedBy(Main.currentUser.getFirstName());
        purchase.setTotalPrice(CurrencyUtil.parseCurrency(totalCostLabel.getText().split(" ")[1]));
        purchase.setLocation(pharmacyLocationChoiceBox.getValue());
        purchase.setSupplier(supplierChoiceBox.getValue().getName());
        setPurchase(purchase);
    }

    @FXML
    private void onVoidItem() {
        Purchase purchase = purchaseTableView.getSelectionModel().getSelectedItem();
        if (purchase != null) {
            purchaseTableView.getItems().remove(purchase);
            setTotalCostLabel();
        }
    }

    LocalDate getDateDelivered() {
        if (receivingSuccessful) {
            return purchaseTableView.getItems().get(0).getDateDelivered();
        } else {
            return null;
        }
    }

    void setOrderId(int orderId) {
        this.orderId = orderId;
        if (orderId != 0) {
            dialogTitle.setText("Receive Drugs : Order Id " + orderId);
        }
    }

}
