package main.java.controller;

import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;
import main.java.dao.TransferDao;
import main.java.model.MedicineLocation;
import main.java.model.StockTransfer;
import main.java.util.DateUtil;

public class ViewStockTransferController {
    @FXML
    private Label dateCreatedLabel, userLabel, transferIdLabel, title;
    @FXML
    private TableView<StockTransfer> stockTransferTableView;
    @FXML
    private TableColumn<StockTransfer, Integer> drugId, quantity;
    @FXML
    private TableColumn<StockTransfer, String> drugName;
    private Stage stage;

    @FXML
    private void initialize() {
        initTable();
    }

    private void initTable() {
        drugName.prefWidthProperty().bind(stockTransferTableView.widthProperty().divide(2));
        drugId.prefWidthProperty().bind(stockTransferTableView.widthProperty().divide(4));
        quantity.prefWidthProperty().bind(stockTransferTableView.widthProperty().divide(4));

        drugId.setCellValueFactory(param -> param.getValue().drugIdProperty().asObject());
        drugName.setCellValueFactory(param -> param.getValue().drugNameProperty());
        quantity.setCellValueFactory(param -> param.getValue().quantityProperty().asObject());
    }

    Stage getStage() {
        return stage;
    }

    void setStage(Stage stage) {
        this.stage = stage;
    }

    void setStockTransfer(StockTransfer stockTransfer) {
        if (stockTransfer.getOrigin() == MedicineLocation.STORE) {
            title.setText(title.getText() + " (Store to Shop)");
        } else {
            title.setText(title.getText() + " (Shop to Store)");
        }
        dateCreatedLabel.setText(DateUtil.formatDateLong(stockTransfer.getDateCreated()));
        transferIdLabel.setText(stockTransfer.getTransferNo() + "");
        userLabel.setText(stockTransfer.getTransferredBy());
        getTransferItems(stockTransfer.getTransferNo());
    }

    private void getTransferItems(int transferNo) {
        Task<ObservableList<StockTransfer>> task = new Task<ObservableList<StockTransfer>>() {
            @Override
            protected ObservableList<StockTransfer> call() throws Exception {
                return TransferDao.getStockTransfer(transferNo);
            }
        };
        task.setOnSucceeded(event -> {
            stockTransferTableView.setItems(task.getValue());
        });
        new Thread(task).start();
    }

    @FXML
    private void onClose() {
        stage.close();
    }
}
