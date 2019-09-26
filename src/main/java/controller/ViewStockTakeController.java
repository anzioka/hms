package main.java.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;
import main.java.dao.StockTakeDao;
import main.java.model.StockTake;
import main.java.util.CurrencyUtil;
import main.java.util.DateUtil;

public class ViewStockTakeController {
    private Stage stage;
    @FXML
    private Label dateLabel, userLabel, countLabel, valueChangeLabel, locationLabel;
    @FXML
    private TableView<StockTake> tableView;
    @FXML
    private TableColumn<StockTake, String> name, valueDiff;
    @FXML
    private TableColumn<StockTake, Integer> systemCount, physicalCount, countDiff, drugCode;

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @FXML
    private void initialize() {
        setUpTable();
    }

    void setStockTake(StockTake stockTake) {
        dateLabel.setText(DateUtil.formatDateLong(stockTake.getDateCreated()));
        userLabel.setText(stockTake.getUserName());
        valueChangeLabel.setText(CurrencyUtil.formatCurrency(stockTake.getValueChange()));
        locationLabel.setText(stockTake.getMedicineLocation().toString());
        getStockTakeItems(stockTake);
    }

    private void getStockTakeItems(StockTake stockTake) {
        Task<ObservableList<StockTake>> task = new Task<ObservableList<StockTake>>() {
            @Override
            protected ObservableList<StockTake> call() throws Exception {
                return StockTakeDao.getStockTakesById(stockTake.getStockTakeId());
            }
        };
        task.setOnSucceeded(event -> {
            tableView.setItems(task.getValue());
            countLabel.setText(task.getValue().size() + "");
        });
        new Thread(task).start();
    }

    private void setUpTable() {
        //col widths
        for (TableColumn column : tableView.getColumns()) {
            if (column == name) {
                column.prefWidthProperty().bind(tableView.widthProperty().divide(3.5));
            } else {
                column.prefWidthProperty().bind(tableView.widthProperty().divide(7));
            }
        }
        name.setCellValueFactory(param -> param.getValue().medicineNameProperty());
        systemCount.setCellValueFactory(param -> param.getValue().qtyOnHandProperty().asObject());
        physicalCount.setCellValueFactory(param -> param.getValue().countedQtyProperty().asObject());
        countDiff.setCellValueFactory(param -> param.getValue().qtyChangeProperty().asObject());
        valueDiff.setCellValueFactory(param -> new SimpleStringProperty(CurrencyUtil.formatCurrency(param.getValue()
                .getValueChange())));
        drugCode.setCellValueFactory(param -> param.getValue().drugIdProperty().asObject());

    }

    @FXML
    private void onExit() {
        stage.close();
    }

    @FXML
    private void onPrint() {
        //TODO : print report
    }

    @FXML
    private void onExportToExcel() {
        //TODO : export to excel
    }

    @FXML
    private void onExportToPdf() {
        //TODO : export to pdf
    }
}
