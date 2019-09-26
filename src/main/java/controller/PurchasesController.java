package main.java.controller;

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
import main.java.dao.PurchasesDao;
import main.java.model.Permission;
import main.java.model.Purchase;
import main.java.util.CurrencyUtil;
import main.java.util.DateUtil;

import java.io.IOException;
import java.time.LocalDate;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class PurchasesController {

    @FXML
    private VBox container;
    @FXML
    private Button newPurchaseButton;
    @FXML
    private DatePicker startDate, endDate;
    @FXML
    private TableView<Purchase> tableView;
    @FXML
    private TableColumn<Purchase, String> invoiceNo, supplier, date, totalCost, receivedBy, options;

    @FXML
    private void initialize() {
        startDate.setConverter(DateUtil.getDatePickerConverter());
        endDate.setConverter(DateUtil.getDatePickerConverter());
        setUpTable();
        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.schedule(this::searchPurchases, 1, TimeUnit.SECONDS);
        newPurchaseButton.setDisable(!Main.userPermissions.get(Permission.RECEIVE_PURCHASES));
    }

    private void setUpTable() {
        for (TableColumn column : tableView.getColumns()) {
            if (column == supplier) {
                supplier.prefWidthProperty().bind(tableView.widthProperty().divide(3.5));
            } else {
                column.prefWidthProperty().bind(tableView.widthProperty().divide(7));
            }
        }
        Label label = new Label("Searching purchases...");
        label.getStyleClass().add("text-info");
        tableView.setPlaceholder(label);

        supplier.setCellValueFactory(param -> param.getValue().supplierProperty());
        invoiceNo.setCellValueFactory(param -> param.getValue().invoiceNumberProperty());
        date.setCellValueFactory(param -> DateUtil.dateStringProperty(param.getValue().getDateDelivered()));
        totalCost.setCellValueFactory(param -> CurrencyUtil.getStringProperty(param.getValue().getTotalPrice()));
        receivedBy.setCellValueFactory(param -> param.getValue().receivedByProperty());
        options.setCellFactory(param -> new TableCell<Purchase, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                int index = getIndex();
                if (index >= 0 && index < tableView.getItems().size()) {
                    Button button = new Button("View Details");
                    button.getStyleClass().add("btn-info-outline");
                    button.setOnAction(event -> {
                        viewPurchaseDetails(tableView.getItems().get(index));
                    });
                    setGraphic(button);
                } else {
                    setGraphic(null);
                }
            }
        });
    }

    private void viewPurchaseDetails(Purchase purchase) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource
                    ("main/resources/view/view-purchase.fxml"));
            Stage stage = new Stage();
            stage.setScene(new Scene(loader.load()));
            stage.initModality(Modality.WINDOW_MODAL);
            stage.setResizable(false);
            stage.initOwner(container.getScene().getWindow());

            //controller
            ViewPurchaseController controller = loader.getController();
            controller.setPurchase(purchase);
            controller.setStage(stage);

            stage.showAndWait();
            searchPurchases();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void searchPurchases() {
        LocalDate start = startDate.getValue() != null ? startDate.getValue() : LocalDate.now().withDayOfMonth(1);
        startDate.setValue(start);

        LocalDate end = endDate.getValue() != null ? endDate.getValue() : LocalDate.now();
        endDate.setValue(end);

        Task<ObservableList<Purchase>> task = new Task<ObservableList<Purchase>>() {
            @Override
            protected ObservableList<Purchase> call() throws Exception {
                return PurchasesDao.getPurchasesByDate(start, end);
            }
        };
        task.setOnSucceeded(event -> {
            tableView.setItems(task.getValue());

            Label label = new Label("No purchases found");
            label.getStyleClass().add("text-danger");
            tableView.setPlaceholder(label);
        });
        new Thread(task).start();
    }

    @FXML
    private void onNewPurchase() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource
                    ("main/resources/view/receive-goods.fxml"));
            Stage stage = new Stage();
            stage.setScene(new Scene(loader.load()));
            stage.initModality(Modality.WINDOW_MODAL);
            stage.setResizable(false);
            stage.initOwner(container.getScene().getWindow());

            //controller
            ReceiveDrugsController controller = loader.getController();
            controller.setStage(stage);
            controller.setOrderId(0);

            stage.showAndWait();
            searchPurchases();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
