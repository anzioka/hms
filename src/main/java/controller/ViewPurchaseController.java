package main.java.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;
import main.java.dao.PurchasesDao;
import main.java.model.Purchase;
import main.java.util.DateUtil;

public class ViewPurchaseController {
    private Stage stage;
    private Purchase purchase;
    @FXML
    private Label invoiceNoLabel, supplierNameLabel, userLabel, dateDeliveredLabel, totalLabel;
    @FXML
    private TableView<Purchase> purchaseTableView;
    @FXML
    private TableColumn<Purchase, String> drugName, batchNo, total, expiryDate;
    @FXML
    private TableColumn<Purchase, Integer> quantity, drugId;
    @FXML
    private TableColumn<Purchase, Double> unitCost, discount;

    @FXML
    private void initialize() {
        setUpTable();
    }

    private void setUpTable() {
        for (TableColumn tableColumn : purchaseTableView.getColumns()) {
            if (tableColumn == drugName) {
                tableColumn.prefWidthProperty().bind(purchaseTableView.widthProperty().divide(4.5));
            } else {
                tableColumn.prefWidthProperty().bind(purchaseTableView.widthProperty().divide(9));
            }
        }
        drugName.setCellValueFactory(param -> param.getValue().drugNameProperty());
        quantity.setCellValueFactory(param -> param.getValue().quantityProperty().asObject());
        batchNo.setCellValueFactory(param -> param.getValue().batchNoProperty());
        expiryDate.setCellValueFactory(param -> new SimpleStringProperty(DateUtil.formatDate(param.getValue().getExpiryDate())));
        unitCost.setCellValueFactory(param -> param.getValue().unitPriceProperty().asObject());
        discount.setCellValueFactory(param -> param.getValue().discountProperty().asObject());
        drugId.setCellValueFactory(param -> param.getValue().drugIdProperty().asObject());
        total.setCellValueFactory(param -> param.getValue().totalPriceProperty());
    }

    Stage getStage() {
        return stage;
    }

    void setStage(Stage stage) {
        this.stage = stage;
    }

    private void getPurchaseDetails() {
        Task<ObservableList<Purchase>> task = new Task<ObservableList<Purchase>>() {
            @Override
            protected ObservableList<Purchase> call() {
                return PurchasesDao.getPurchasesById(purchase.getPurchaseId());
            }
        };
        task.setOnSucceeded(event -> {
            purchaseTableView.setItems(task.getValue());
        });
        new Thread(task).start();
    }

    @FXML
    private void onClose() {
        stage.close();
    }

    Purchase getPurchase() {
        return purchase;
    }

    void setPurchase(Purchase purchase) {
        this.purchase = purchase;
        userLabel.setText(purchase.getReceivedBy());
        supplierNameLabel.setText(purchase.getSupplier());
        invoiceNoLabel.setText(purchase.getInvoiceNumber());
        dateDeliveredLabel.setText(DateUtil.formatDateLong(purchase.getDateDelivered()));
        totalLabel.setText("Ksh. " + purchase.totalPriceProperty().get());
        getPurchaseDetails();
    }
}
