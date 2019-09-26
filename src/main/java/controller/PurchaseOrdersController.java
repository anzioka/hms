package main.java.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import main.Main;
import main.java.dao.OrderDAO;
import main.java.model.OrderStatus;
import main.java.model.Permission;
import main.java.model.PurchaseOrder;
import main.java.util.AlertUtil;
import main.java.util.DBUtil;
import main.java.util.DateUtil;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by alfonce on 08/07/2017.
 */
public class PurchaseOrdersController {
    @FXML
    private Button createOrderBtn;
    @FXML
    private VBox container;
    @FXML
    private DatePicker startDate, endDate;
    @FXML
    private TableView<PurchaseOrder> tableView;
    @FXML
    private TableColumn<PurchaseOrder, Integer> orderNumber;
    @FXML
    private TableColumn<PurchaseOrder, String> dateOrdered, supplier, status, options, dateDelivered;
    private ObservableList<PurchaseOrder> orders = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        startDate.setConverter(DateUtil.getDatePickerConverter());
        endDate.setConverter(DateUtil.getDatePickerConverter());
        setUpTable();
        Executors.newSingleThreadScheduledExecutor().schedule(this::getData, 1, TimeUnit.SECONDS);
        createOrderBtn.setDisable(!Main.userPermissions.get(Permission.CREATE_PURCHASE_ORDERS));
    }

    @FXML
    private void getData() {
        LocalDate start = startDate.getValue() != null ? startDate.getValue() : LocalDate.now().withDayOfMonth(1);
        startDate.setValue(start);

        LocalDate end = endDate.getValue() != null ? endDate.getValue() : LocalDate.now();
        endDate.setValue(end);
        Task<ObservableList<PurchaseOrder>> task = new Task<ObservableList<PurchaseOrder>>() {
            @Override
            protected ObservableList<PurchaseOrder> call() {
                return OrderDAO.getOrders(start, end);
            }
        };
        task.setOnSucceeded(event -> {
            orders = task.getValue();
            tableView.setItems(orders);

            Label label = new Label("No orders found!");
            label.getStyleClass().add("text-danger");
            tableView.setPlaceholder(label);
        });
        new Thread(task).start();
    }

    private void setUpTable() {
        Label label = new Label("Searching records ...");
        label.getStyleClass().add("text-info");
        tableView.setPlaceholder(label);

        //table columns
        orderNumber.setCellValueFactory(param -> param.getValue().orderIdProperty().asObject());
        dateOrdered.setCellValueFactory(param -> DateUtil.dateStringProperty(param.getValue().getOrderDate()));
        supplier.setCellValueFactory(param -> param.getValue().supplierNameProperty());
        dateDelivered.setCellValueFactory(param -> DateUtil.dateStringProperty(param.getValue().getDateReceived()));

        options.setCellFactory(param -> new TableCell<PurchaseOrder, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                int index = getIndex();
                if (index >= 0 && index < tableView.getItems().size()) {
                    Button viewBtn = new Button("View");
                    viewBtn.getStyleClass().add("btn-info-outline");
                    viewBtn.setOnAction(event -> {
                        new PrintPurchaseOrder().viewOrder(tableView.getItems().get(index));
                    });

                    Button receiveGoodsBtn = new Button("Receive");
                    receiveGoodsBtn.getStyleClass().add("btn-info-outline");
                    receiveGoodsBtn.setOnAction(event -> {
                        onReceiveGoods(tableView.getItems().get(index).getOrderId());
                    });
                    receiveGoodsBtn.setDisable(!Main.userPermissions.get(Permission.RECEIVE_PURCHASES) || tableView.getItems().get(index).getOrderStatus() == OrderStatus.DELIVERED);

                    Button cancelOrderBtn = new Button("Cancel");
                    cancelOrderBtn.getStyleClass().add("btn-danger-outline");
                    cancelOrderBtn.setDisable(!Main.userPermissions.get(Permission.CANCEL_PURCHASE_ORDERS) || tableView.getItems().get(index).getOrderStatus() == OrderStatus.DELIVERED);
                    cancelOrderBtn.setOnAction(event -> {
                        cancelOrder(tableView.getItems().get(index));
                    });

                    VBox buttons = new VBox(5.0);
                    buttons.setAlignment(Pos.CENTER);
                    buttons.getChildren().addAll(viewBtn, receiveGoodsBtn, cancelOrderBtn);

                    setGraphic(buttons);
                } else {
                    setGraphic(null);
                }
            }
        });
        status.setCellFactory(param -> new TableCell<PurchaseOrder, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                int index = getIndex();
                if (index >= 0 && index < tableView.getItems().size()) {
                    OrderStatus orderStatus = tableView.getItems().get(index)
                            .getOrderStatus();
                    Label status = new Label(orderStatus.toString());
                    status.getStyleClass().add("color-white");
                    status.setStyle("-fx-padding: 1 4 1 4");
                    if (orderStatus == OrderStatus.PENDING) {
                        status.getStyleClass().add("bg-warning");
                    } else {
                        status.getStyleClass().add("bg-success");
                    }

                    setGraphic(status);
                } else {
                    setGraphic(null);
                }
            }
        });
    }

    private void cancelOrder(PurchaseOrder purchaseOrder) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to cancel order?", ButtonType.OK, ButtonType.CANCEL);
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (DBUtil.executeStatement("delete from orders where order_id = " + purchaseOrder.getOrderId())) {
                AlertUtil.showAlert("", "Purchaser order successfully deleted!", Alert.AlertType.INFORMATION);
                tableView.getItems().remove(purchaseOrder);
            } else{
                AlertUtil.showGenericError();
            }
        }
    }

    private void onReceiveGoods(int currentOrderId) {
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
            controller.setOrderId(currentOrderId);

            stage.showAndWait();

            getData();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void viewPurchaseOrderDetails(PurchaseOrder purchaseOrder) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource
                    ("main/resources/view/view-purchase-order.fxml"));
            Stage stage = new Stage();
            stage.setScene(new Scene(loader.load()));
            stage.initModality(Modality.WINDOW_MODAL);
            stage.setResizable(false);
            stage.initOwner(container.getScene().getWindow());

            //controller
            ViewPurchaseOrderController controller = loader.getController();
            controller.setStage(stage);
            controller.setOrder(purchaseOrder);

            stage.showAndWait();

            getData();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onNewOrder() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource
                    ("main/resources/view/edit-purchase-order.fxml"));
            Stage stage = new Stage();
            stage.initModality(Modality.WINDOW_MODAL);
            stage.setResizable(false);
            stage.setScene(new Scene(loader.load()));
            stage.initOwner(container.getScene().getWindow());

            //controller
            EditPurchaseOrderController controller = loader.getController();
            controller.setStage(stage);

            stage.showAndWait();
            if (controller.getPurchaseOrder() != null) {
                tableView.getItems().add(controller.getPurchaseOrder());
            }
            getData();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
