package main.java.controller;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import main.java.dao.MedicineDAO;
import main.java.dao.PurchasesDao;
import main.java.dao.SupplierDAO;
import main.java.model.Medicine;
import main.java.model.MedicineLocation;
import main.java.model.PurchaseOrder;
import main.java.model.Supplier;
import main.java.util.AlertUtil;
import main.java.util.CurrencyUtil;
import main.java.util.DBUtil;
import main.java.util.NumberUtil;
import org.controlsfx.control.textfield.AutoCompletionBinding;
import org.controlsfx.control.textfield.TextFields;

import java.time.LocalDate;

public class EditPurchaseOrderController {
    private Stage stage;
    private PurchaseOrder purchaseOrder;
    private ObservableList<Medicine> medicines = FXCollections.observableArrayList();
    @FXML
    private ChoiceBox<Supplier> supplierChoiceBox;
    @FXML
    private TextField medicine, quantity, unitCost;
    @FXML
    private TableView<PurchaseOrder> purchaseOrderTableView;
    @FXML
    private TableColumn<PurchaseOrder, String> nameCol, totalCostCol;
    @FXML
    private TableColumn<PurchaseOrder, Integer> storeQtyCol, shopQtyCol, quantityCol;
    @FXML
    private TableColumn<PurchaseOrder, Double> unitCostCol;
    @FXML
    private Label orderNumLabel, totalLabel;
    private Medicine currentMedicine = null;

    @FXML
    private void initialize() {
        setUpTable();
        getData();
        supplierChoiceBox.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) ->
        {
            if (newValue != null) {
                medicine.requestFocus();
            }
        });
    }

    private void getData() {
        Task task = new Task() {
            @Override
            protected Object call() throws Exception {
                return PurchasesDao.getNextPurchaseOrderId();
            }
        };
        task.setOnSucceeded(event -> {
            orderNumLabel.setText("Order No. " + task.getValue());
        });
        new Thread(task).start();

        Task<ObservableList<Supplier>> suppliersTask = new Task<ObservableList<Supplier>>() {
            @Override
            protected ObservableList<Supplier> call() throws Exception {
                return SupplierDAO.getSuppliers();
            }
        };
        suppliersTask.setOnSucceeded(event -> {
            supplierChoiceBox.setItems(suppliersTask.getValue());
        });
        new Thread(suppliersTask).start();

        Task<ObservableList<Medicine>> medicineListTask = new Task<ObservableList<Medicine>>() {
            @Override
            protected ObservableList<Medicine> call() {
                return MedicineDAO.getMedicineList();
            }
        };
        medicineListTask.setOnSucceeded(event -> {
            medicines = medicineListTask.getValue();
            AutoCompletionBinding<Medicine> binding = TextFields.bindAutoCompletion(medicine, medicines);
            binding.setOnAutoCompleted(autoCompletionEvent -> {
                currentMedicine = autoCompletionEvent.getCompletion();
                unitCost.setText(currentMedicine.getBuyingPrice() + "");
                unitCost.selectAll();
                quantity.requestFocus();
            });
        });
        new Thread(medicineListTask).start();
    }

    private void resetFields() {
        currentMedicine = null;
        quantity.clear();
        unitCost.clear();
        medicine.clear();
        medicine.requestFocus();

    }

    private void setUpTable() {
        for (TableColumn tableColumn : purchaseOrderTableView.getColumns()) {
            if (tableColumn != nameCol) {
                tableColumn.prefWidthProperty().bind(purchaseOrderTableView.widthProperty().divide(7));
            } else {
                tableColumn.prefWidthProperty().bind(purchaseOrderTableView.widthProperty().divide(3.5));
            }
        }
        //data
        nameCol.setCellValueFactory(param -> param.getValue().descriptionProperty());
        totalCostCol.setCellValueFactory(param -> CurrencyUtil.getStringProperty(param.getValue().getTotalPrice()));
        storeQtyCol.setCellValueFactory(param -> new SimpleIntegerProperty(MedicineDAO.getQuantity(medicines, param
                        .getValue()
                        .getDrugId(),
                MedicineLocation.STORE
        )).asObject());
        shopQtyCol.setCellValueFactory(param -> new SimpleIntegerProperty(MedicineDAO.getQuantity(medicines, param
                        .getValue()
                        .getDrugId(),
                MedicineLocation
                        .SHOP
        )).asObject());
        quantityCol.setCellValueFactory(param -> param.getValue().quantityProperty().asObject());
        unitCostCol.setCellValueFactory(param -> param.getValue().unitPriceProperty().asObject());
    }

    private void setTotalCostLabel() {
        double total = 0;
        for (PurchaseOrder purchaseOrder : purchaseOrderTableView.getItems()) {
            total += purchaseOrder.getUnitPrice() * purchaseOrder.getQuantity();
        }
        totalLabel.setText("Ksh. " + CurrencyUtil.formatCurrency(total));
    }

    Stage getStage() {
        return stage;
    }

    void setStage(Stage stage) {
        this.stage = stage;
    }

    PurchaseOrder getPurchaseOrder() {
        return purchaseOrder;
    }

    @FXML
    private void onAddOrder() {
        if (validInput()) {
            //check if already added
            for (PurchaseOrder purchaseOrder : purchaseOrderTableView.getItems()) {
                if (purchaseOrder.getDescription().equals(currentMedicine.getName())) {
                    purchaseOrder.setQuantity(purchaseOrder.getQuantity() + NumberUtil.stringToInt(quantity.getText()));
                    purchaseOrder.setTotalPrice(purchaseOrder.getUnitPrice() * purchaseOrder.getQuantity());
                    purchaseOrderTableView.refresh();
                    setTotalCostLabel();
                    resetFields();
                    return;
                }
            }
            PurchaseOrder purchaseOrder = new PurchaseOrder();
            purchaseOrder.setQuantity(NumberUtil.stringToInt(quantity.getText()));
            purchaseOrder.setUnitPrice(NumberUtil.stringToDouble(unitCost.getText()));
            purchaseOrder.setDescription(currentMedicine.getName());
            purchaseOrder.setDrugId(currentMedicine.getDrugCode());
            purchaseOrder.setTotalPrice(purchaseOrder.getUnitPrice() * purchaseOrder.getQuantity());
            purchaseOrderTableView.getItems().add(purchaseOrder);
            setTotalCostLabel();
            resetFields();
        }
    }

    private boolean validInput() {
        String errorMsg = "";
        if (medicine.getText() == null || medicine.getText().isEmpty()) {
            errorMsg += "Medicine name field cannot be blank!\n";
        }
        if (NumberUtil.stringToInt(quantity.getText()) < 0) {
            errorMsg += "Invalid quantity value!\n";
        }
        if (NumberUtil.stringToDouble(unitCost.getText()) < 0) {
            errorMsg += "Invalid unit cost value";
        }
        if (errorMsg.isEmpty()) {
            return true;
        } else {
            AlertUtil.showAlert("Error", errorMsg, Alert.AlertType.ERROR);
            return false;
        }
    }

    @FXML
    private void onDeleteItem() {
        PurchaseOrder purchaseOrder = purchaseOrderTableView.getSelectionModel().getSelectedItem();
        if (purchaseOrder != null) {
            purchaseOrderTableView.getItems().remove(purchaseOrder);
            setTotalCostLabel();
        }
    }

    @FXML
    private void onSaveOrder() {
        if (purchaseOrderTableView.getItems().isEmpty()) {
            return;
        }
        Supplier supplier = supplierChoiceBox.getValue();
        if (supplier == null) {
            AlertUtil.showAlert("Error", "Please select supplier", Alert.AlertType.ERROR);
            return;
        }
        for (PurchaseOrder purchaseOrder : purchaseOrderTableView.getItems()) {
            purchaseOrder.setOrderDate(LocalDate.now());
            purchaseOrder.setOrderId(NumberUtil.stringToInt(orderNumLabel.getText().split(" ")[2]));
            purchaseOrder.setSupplierId(supplier.getSupplierId());

        }
        if (DBUtil.savePurchaseOrder(purchaseOrderTableView.getItems())) {
            AlertUtil.showAlert("Purchase Order", "Purchase order has been successfully created and saved", Alert
                    .AlertType.INFORMATION);
            createSummaryOrder();
            onClose();
        } else {
            AlertUtil.showAlert("Error", "An error occurred while attempting to create purchase order", Alert.AlertType.ERROR);
        }
    }

    private void createSummaryOrder() {
        purchaseOrder = new PurchaseOrder();
        purchaseOrder.setSupplierName(supplierChoiceBox.getValue().getName());
        purchaseOrder.setOrderDate(LocalDate.now());
        purchaseOrder.setTotalPrice(CurrencyUtil.parseCurrency(totalLabel.getText().split(" ")[1]));
        purchaseOrder.setOrderId(purchaseOrderTableView.getItems().get(0).getOrderId());
        purchaseOrder.setSupplierId(supplierChoiceBox.getValue().getSupplierId());
    }

    @FXML
    private void onClose() {
        stage.close();
    }
}
