package main.java.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import main.java.model.PurchaseReturn;
import main.java.util.CurrencyUtil;
import main.java.util.DBUtil;
import main.java.util.DateUtil;
import main.java.util.StringUtil;

import java.sql.ResultSet;

public class ViewPurchaseReturnController {
    @FXML
    private TableView<PurchaseReturn> tableView;
    @FXML
    private TableColumn<PurchaseReturn, String> drugName, note, quantity, totalPrice, buyingPrice;
    @FXML
    private Label date, supplier;

    @FXML
    private void initialize() {
        setUpTable();
    }

    private void setUpTable() {
        for (TableColumn column : tableView.getColumns()) {
            if (column == drugName || column == note) {
                column.prefWidthProperty().bind(tableView.widthProperty().divide(3.5));
            } else {
                column.prefWidthProperty().bind(tableView.widthProperty().divide(7));
            }
        }
        quantity.setCellValueFactory(param -> StringUtil.getStringProperty(Integer.toString(param.getValue().getQuantity())));
        totalPrice.setCellValueFactory(param -> CurrencyUtil.getStringProperty(param.getValue().getBuyingPrice() * param.getValue().getQuantity()));
        buyingPrice.setCellValueFactory(param -> CurrencyUtil.getStringProperty(param.getValue().getBuyingPrice()));
        drugName.setCellValueFactory(param -> param.getValue().drugNameProperty());
        note.setCellFactory(param -> new TableCell<PurchaseReturn, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                int index = getIndex();
                if (index >= 0 && index < tableView.getItems().size()) {
                    Label label = new Label(tableView.getItems().get(index).getNote());
                    label.setWrapText(true);
                    setGraphic(label);
                } else {
                    setGraphic(null);
                }
            }
        });

    }

    void setPurchaseReturn(PurchaseReturn purchaseReturn) {
        date.setText(DateUtil.formatDateLong(purchaseReturn.getDate()));
        supplier.setText(purchaseReturn.getSupplier());
        getPurchaseReturns(purchaseReturn.getReturnId());
    }

    private void getPurchaseReturns(int returnId) {
        Task<ObservableList<PurchaseReturn>> task = new Task<ObservableList<PurchaseReturn>>() {
            @Override
            protected ObservableList<PurchaseReturn> call() throws Exception {
                ObservableList<PurchaseReturn> returns = FXCollections.observableArrayList();
                String sql = "select drugs.Name, quantity, buying_price, note " +
                        "from purchase_returns " +
                        "inner join drugs on drugs.DrugCode = purchase_returns.drug_id " +
                        "where Id = " + returnId;
                ResultSet resultSet = DBUtil.executeQuery(sql);
                if (resultSet != null) {
                    while (resultSet.next()) {
                        PurchaseReturn purchaseReturn = new PurchaseReturn();
                        purchaseReturn.setBuyingPrice(resultSet.getDouble("buying_price"));
                        purchaseReturn.setNote(resultSet.getString("note"));
                        purchaseReturn.setQuantity(resultSet.getInt("quantity"));
                        purchaseReturn.setDrugName(resultSet.getString("name"));
                        returns.add(purchaseReturn);
                    }
                }
                return returns;
            }
        };
        task.setOnSucceeded(event -> {
            tableView.setItems(task.getValue());
        });
        new Thread(task).start();
    }
}
