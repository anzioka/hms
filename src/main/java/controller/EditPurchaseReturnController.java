package main.java.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import main.java.dao.PurchasesDao;
import main.java.model.MedicineLocation;
import main.java.model.Purchase;
import main.java.model.PurchaseReturn;
import main.java.util.*;
import org.controlsfx.control.textfield.AutoCompletionBinding;
import org.controlsfx.control.textfield.TextFields;

import java.time.LocalDate;
import java.util.Optional;

public class EditPurchaseReturnController {
    private Stage stage;
    private Purchase purchase;
    private String invoice;
    private ObservableList<Purchase> purchases = FXCollections.observableArrayList();
    @FXML
    private TableView<PurchaseReturn> tableView;
    @FXML
    private TableColumn<PurchaseReturn, String> drugName, totalPrice, note;
    @FXML
    private TableColumn<PurchaseReturn, Integer> quantityReturned;
    @FXML
    private TableColumn<PurchaseReturn, Double> buyingPrice;
    @FXML
    private Label totalLabel;
    @FXML
    private TextField nameSearchField, quantityField, invoiceSearchField, editNote;
    @FXML
    private ChoiceBox<MedicineLocation> medicineLocationChoiceBox;

    Stage getStage() {
        return stage;
    }

    void setStage(Stage stage) {
        this.stage = stage;
    }

    @FXML
    private void initialize() {
        setUpTable();
        medicineLocationChoiceBox.setItems(FXCollections.observableArrayList(MedicineLocation.values()));
        medicineLocationChoiceBox.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> {
            tableView.getItems().clear();
            reset();
        });
    }

    private void setUpTable() {
        for (TableColumn tableColumn : tableView.getColumns()) {
            if (tableColumn == drugName || tableColumn == note) {
                tableColumn.prefWidthProperty().bind(tableView.widthProperty().divide(3.5));
            } else {
                tableColumn.prefWidthProperty().bind(tableView.widthProperty().divide(7));
            }
        }
        quantityReturned.setCellValueFactory(param -> param.getValue().quantityProperty().asObject());
        drugName.setCellValueFactory(param -> param.getValue().drugNameProperty());
        totalPrice.setCellValueFactory(param -> CurrencyUtil.getStringProperty(param.getValue().getBuyingPrice() * param.getValue().getQuantity()));
        buyingPrice.setCellValueFactory(param -> param.getValue().buyingPriceProperty().asObject());
        note.setCellValueFactory(param -> param.getValue().noteProperty());
    }

    private void setTotalPrice() {
        double total = 0;
        for (PurchaseReturn purchaseReturn : tableView.getItems()) {
            total += purchaseReturn.getBuyingPrice() * purchaseReturn.getQuantity();
        }
        totalLabel.setText("Ksh. " + CurrencyUtil.formatCurrency(total));
    }

    @FXML
    private void onExit() {
        stage.close();
    }

    @FXML
    private void onRemoveItem() {
        PurchaseReturn purchaseReturn = tableView.getSelectionModel().getSelectedItem();
        if (purchaseReturn != null) {
            tableView.getItems().remove(purchaseReturn);
            setTotalPrice();
        }
    }

    @FXML
    private void onSave() {
        if (tableView.getItems().isEmpty()) {
            AlertUtil.showAlert("Purchase Returns", "No items to return", Alert.AlertType.ERROR);
        } else if (medicineLocationChoiceBox.getValue() == null) {
            AlertUtil.showAlert("Purchase Returns", "Specify location of the drugs you intent to return.", Alert.AlertType.ERROR);
        } else {
            int returnId = PurchasesDao.getNextPurchaseReturnId();
            for (PurchaseReturn purchaseReturn : tableView.getItems()) {
                purchaseReturn.setReturnId(returnId);
                purchaseReturn.setDate(LocalDate.now());
                purchaseReturn.setLocation(medicineLocationChoiceBox.getValue());
            }
            if (DBUtil.savePurchaseReturns(tableView.getItems())) {
                AlertUtil.showAlert("Purchase Returns", "Purchase return has been successfully recorded and quantities updated!", Alert.AlertType.INFORMATION);
                stage.close();
            }
        }
    }

    private void reset() {
        purchase = null;
        quantityField.clear();
        nameSearchField.clear();
        nameSearchField.requestFocus();
        editNote.clear();
    }

    @FXML
    private void onAddItemToReturns() {
        if (validInput()) {
            PurchaseReturn purchaseReturn = createPurchaseReturn();
            if (!alreadyAdded(purchaseReturn.getDrugName())) {
                tableView.getItems().add(purchaseReturn);
            }
            reset();
            setReturnsTotal();
        }
    }

    private boolean alreadyAdded(String name) {
        for (PurchaseReturn purchaseReturn : tableView.getItems()) {
            if (purchaseReturn.getDrugName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    private void setReturnsTotal() {
        double total = 0;
        for (PurchaseReturn purchaseReturn : tableView.getItems()) {
            total += purchaseReturn.getBuyingPrice() * purchaseReturn.getQuantity();
        }
        totalLabel.setText("Ksh. " + CurrencyUtil.formatCurrency(total));
    }

    private PurchaseReturn createPurchaseReturn() {
        PurchaseReturn purchaseReturn = new PurchaseReturn();
        purchaseReturn.setQuantity(NumberUtil.stringToInt(quantityField.getText()));
        purchaseReturn.setDrugName(purchase.getDrugName());
        purchaseReturn.setSupplierId(purchase.getSupplierId());
        purchaseReturn.setNote(editNote.getText());
        purchaseReturn.setBuyingPrice((100 - purchase.getDiscount()) / 100 * purchase.getUnitPrice());
        purchaseReturn.setInvoiceNo(invoice);
        purchaseReturn.setDrugId(purchase.getDrugId());
        return purchaseReturn;
    }

    private boolean validInput() {
        String errorMsg = "";
        if (purchase == null) {
            errorMsg += "Please enter name of drug to return.\n";
        } else if (NumberUtil.stringToInt(quantityField.getText()) < 0) {
            errorMsg += "Invalid quantity!";
        }

        if (errorMsg.isEmpty()) {
            return true;
        }
        AlertUtil.showAlert("Error(s)", errorMsg, Alert.AlertType.ERROR);
        return false;
    }

    @FXML
    private void onSearchPurchase() {
        invoice = invoiceSearchField.getText();

        if (invoice == null || invoice.isEmpty()) {
            AlertUtil.showAlert("Purchase Search", "Please enter the invoice number accompanying your purchase", Alert.AlertType.WARNING);
            return;
        }
        Task<ObservableList<Purchase>> task = new Task<ObservableList<Purchase>>() {
            @Override
            protected ObservableList<Purchase> call() {
                return PurchasesDao.getPurchasesByInvoice(invoiceSearchField.getText());
            }
        };
        task.setOnSucceeded(event -> {
            purchases = task.getValue();
            if (purchases.isEmpty()) {
                AlertUtil.showAlert("Search Error", "No purchase matching invoice number '" + invoiceSearchField
                        .getText() + "'", Alert.AlertType.ERROR);
            } else {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Search Result");
                alert.setContentText("Purchase found : " + purchases.size() + " drug(s) purchased " +
                        "from " +
                        "" + purchases.get(0).getSupplier() + " on " + DateUtil.formatDateLong(purchases.get(0)
                        .getDateDelivered()) + ". Is this correct? ");
                Optional<ButtonType> result = alert.showAndWait();
                if (result.isPresent() && result.get() == ButtonType.OK) {
                    medicineLocationChoiceBox.requestFocus();
                    tableView.getItems().clear();
                    AutoCompletionBinding<Purchase> binding = TextFields.bindAutoCompletion(nameSearchField, purchases);
                    binding.setOnAutoCompleted(autoCompletionEvent -> {
                        purchase = autoCompletionEvent.getCompletion();
                        quantityField.requestFocus();
                    });
                }
            }
        });
        new Thread(task).start();
    }

}
