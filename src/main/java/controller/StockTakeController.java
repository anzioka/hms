package main.java.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import main.Main;
import main.java.dao.StockTakeDao;
import main.java.model.Permission;
import main.java.model.StockTake;
import main.java.util.CurrencyUtil;
import main.java.util.DateUtil;

import java.io.IOException;
import java.time.LocalDate;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class StockTakeController {
    @FXML
    private DatePicker startDate, endDate;
    @FXML
    private VBox container;
    @FXML
    private TableView<StockTake> stockTakeTableView;
    @FXML
    private TableColumn<StockTake, String> date, user, details, medicineLocation, valueChange;
    @FXML
    private TableColumn<StockTake, Integer> quantityChange, code;
    @FXML
    private Button newStockTakeBtn;
    @FXML
    private void initialize() {
        startDate.setConverter(DateUtil.getDatePickerConverter());
        endDate.setConverter(DateUtil.getDatePickerConverter());

        setUpTable();
        Executors.newSingleThreadScheduledExecutor().schedule(this::getData, 1, TimeUnit.SECONDS);
        newStockTakeBtn.setDisable(!Main.userPermissions.get(Permission.STOCK_TAKE));
    }

    @FXML
    private void getData() {
        LocalDate start = startDate.getValue() != null ? startDate.getValue() : LocalDate.now().withDayOfMonth(1);
        startDate.setValue(start);

        LocalDate end = endDate.getValue() != null ? endDate.getValue() : LocalDate.now();
        endDate.setValue(end);
        Task<ObservableList<StockTake>> task = new Task<ObservableList<StockTake>>() {
            @Override
            protected ObservableList<StockTake> call() {
                return StockTakeDao.getStockTakes(start, end);
            }
        };
        task.setOnSucceeded(event -> {
            stockTakeTableView.setItems(task.getValue());
            Label label = new Label("No stock-take records found!");
            label.getStyleClass().add("text-danger");
            stockTakeTableView.setPlaceholder(label);
        });
        new Thread(task).start();
    }

    private void setUpTable() {
        valueChange.setCellValueFactory(param -> new SimpleStringProperty(CurrencyUtil.formatCurrency(param.getValue()
                .getValueChange())));
        quantityChange.setCellValueFactory(param -> param.getValue().qtyChangeProperty().asObject());
        code.setCellValueFactory(param -> param.getValue().stockTakeIdProperty().asObject());
        date.setCellValueFactory(param -> DateUtil.dateStringProperty(param.getValue().getDateCreated()));
        user.setCellValueFactory(param -> param.getValue().userNameProperty());
        medicineLocation.setCellValueFactory(param -> param.getValue().medicineLocationProperty());
        details.setCellFactory(param -> new TableCell<StockTake, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                int index = getIndex();
                if (index >= 0 && index < stockTakeTableView.getItems().size()) {
                    Button button = new Button("Details");
                    button.getStyleClass().add("btn-info-outline");
                    button.setOnAction(event -> {
                        showStockTakeDetails(stockTakeTableView.getItems().get(index));
                    });
                    setGraphic(button);
                } else {
                    setGraphic(null);
                }
            }
        });

        Label label = new Label("Retrieving records...");
        label.getStyleClass().add("text-info");
        stockTakeTableView.setPlaceholder(label);
    }

    private void showStockTakeDetails(StockTake stockTake) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource
                    ("main/resources/view/view_stock_take.fxml"));
            Stage stage = new Stage();
            stage.setScene(new Scene(loader.load()));
            stage.setResizable(false);
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(container.getScene().getWindow());

            ViewStockTakeController controller = loader.getController();
            controller.setStage(stage);
            controller.setStockTake(stockTake);

            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onStockTake() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource
                    ("main/resources/view/edit_stock_take.fxml"));
            Stage stage = new Stage();
            stage.setScene(new Scene(loader.load()));
            stage.setResizable(true);
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(container.getScene().getWindow());

            //controller
            EditStockTakeController controller = loader.getController();
            controller.setStage(stage);
            stage.showAndWait();
            getData();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
