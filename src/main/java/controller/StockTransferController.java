package main.java.controller;

import javafx.collections.FXCollections;
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
import main.java.dao.TransferDao;
import main.java.model.Permission;
import main.java.model.StockTransfer;
import main.java.util.DateUtil;

import java.io.IOException;
import java.time.LocalDate;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class StockTransferController {
    @FXML
    private Button newTransferBtn;
    @FXML
    private DatePicker startDate, endDate;
    @FXML
    private VBox container;
    @FXML
    private TableView<StockTransfer> stockTransferTableView;
    @FXML
    private TableColumn<StockTransfer, String> dateCreated, description, transferredBy, details;
    @FXML
    private TableColumn<StockTransfer, Integer> quantity, transferNo;
    private ObservableList<StockTransfer> stockTransfers = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        startDate.setConverter(DateUtil.getDatePickerConverter());
        endDate.setConverter(DateUtil.getDatePickerConverter());
        setUpTable();
        Executors.newSingleThreadScheduledExecutor().schedule(this::getData, 1, TimeUnit.SECONDS);
        newTransferBtn.setDisable(!Main.userPermissions.get(Permission.TRANSFER_DRUGS));
    }

    @FXML
    private void getData() {
        LocalDate start = startDate.getValue() != null ? startDate.getValue() : LocalDate.now().withDayOfMonth(1);
        startDate.setValue(start);

        LocalDate end = endDate.getValue() != null ? endDate.getValue() : LocalDate.now();
        endDate.setValue(end);
        Task<ObservableList<StockTransfer>> task = new Task<ObservableList<StockTransfer>>() {
            @Override
            protected ObservableList<StockTransfer> call() {
                return TransferDao.getTransfers(start, end);
            }
        };
        task.setOnSucceeded(event -> {
            stockTransfers = task.getValue();
            stockTransferTableView.setItems(stockTransfers);
            Label label = new Label("No stock transfers found!");
            label.getStyleClass().add("text-danger");
            stockTransferTableView.setPlaceholder(label);
        });
        new Thread(task).start();
    }

    private void setUpTable() {
        //table cols width
        for (TableColumn column : stockTransferTableView.getColumns()) {
            if (column == description) {
                column.prefWidthProperty().bind(stockTransferTableView.widthProperty().divide(3.5));
            } else {
                column.prefWidthProperty().bind(stockTransferTableView.widthProperty().divide(7));
            }
        }
        transferNo.setCellValueFactory(param -> param.getValue().transferNoProperty().asObject());
        dateCreated.setCellValueFactory(param -> param.getValue().dateCreatedProperty());
        description.setCellValueFactory(param -> param.getValue().descriptionProperty());
        quantity.setCellValueFactory(param -> param.getValue().quantityProperty().asObject());
        transferredBy.setCellValueFactory(param -> param.getValue().transferredByProperty());
        details.setCellFactory(param -> new TableCell<StockTransfer, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                int index = getIndex();
                if (index >= 0 && index < stockTransferTableView.getItems().size()) {
                    Button button = new Button("Details");
                    button.setOnAction(event -> {
                        viewStockTransferDetails(stockTransferTableView.getItems().get(index));
                    });
                    button.getStyleClass().add("btn-primary-outline");
                    setGraphic(button);
                } else {
                    setGraphic(null);
                }
            }
        });

        Label label = new Label("Retrieving data ...");
        label.getStyleClass().add("text-info");
        stockTransferTableView.setPlaceholder(label);
    }

    private void viewStockTransferDetails(StockTransfer stockTransfer) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource
                    ("main/resources/view/view-stock-transfer.fxml"));
            Stage stage = new Stage();
            stage.setScene(new Scene(loader.load()));
            stage.initModality(Modality.WINDOW_MODAL);
            stage.setResizable(false);
            stage.initOwner(container.getScene().getWindow());

            ViewStockTransferController controller = loader.getController();
            controller.setStage(stage);
            controller.setStockTransfer(stockTransfer);

            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onNewTransfer() {
        //TODO : show dialog box to create return
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource
                    ("main/resources/view/edit-stock-transfer.fxml"));
            Stage stage = new Stage();
            stage.setScene(new Scene(loader.load()));
            stage.setResizable(false);
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(container.getScene().getWindow());

            EditStockTransferController controller = loader.getController();
            controller.setStage(stage);

            stage.showAndWait();
            getData();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
