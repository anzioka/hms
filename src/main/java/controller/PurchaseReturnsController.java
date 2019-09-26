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
import main.java.model.Permission;
import main.java.model.PurchaseReturn;
import main.java.util.DBUtil;
import main.java.util.DateUtil;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class PurchaseReturnsController {
    @FXML
    private VBox container;
    @FXML
    private DatePicker startDate, endDate;
    @FXML
    private TableView<PurchaseReturn> purchaseReturnTableView;
    @FXML
    private TableColumn<PurchaseReturn, String> supplier, details, invoice, dateReturned, returnedBy;
    @FXML
    private Button newReturn;

    @FXML
    private void initialize() {
        setUpTable();
        startDate.setConverter(DateUtil.getDatePickerConverter());
        endDate.setConverter(DateUtil.getDatePickerConverter());
        Executors.newSingleThreadScheduledExecutor().schedule(this::getData, 1, TimeUnit.SECONDS);
        newReturn.setDisable(!Main.userPermissions.get(Permission.RETURN_PURCHASES));
    }

    @FXML
    private void getData() {
        //TODO : get purchase returns and suppliers data. On fetching
        LocalDate start = startDate.getValue() != null ? startDate.getValue() : LocalDate.now().withDayOfMonth(1);
        startDate.setValue(start);

        LocalDate end = endDate.getValue() != null ? endDate.getValue() : LocalDate.now();
        endDate.setValue(end);
        Task<ObservableList<PurchaseReturn>> task = new Task<ObservableList<PurchaseReturn>>() {
            @Override
            protected ObservableList<PurchaseReturn> call() throws Exception {
                return getReturns(start, end);
            }
        };
        task.setOnSucceeded(event -> {
            purchaseReturnTableView.setItems(task.getValue());

            Label label = new Label("No purchase returns found!");
            label.getStyleClass().add("text-danger");
            purchaseReturnTableView.setPlaceholder(label);
        });
        new Thread(task).start();
    }

    private ObservableList<PurchaseReturn> getReturns(LocalDate start, LocalDate end) throws SQLException {
        ObservableList<PurchaseReturn> returns = FXCollections.observableArrayList();
        String sql = "select distinct purchase_returns.invoice, purchase_returns.Id, purchase_returns.date, " +
                "suppliers.name, users.firstName, users.LastName " +
                "from purchase_returns " +
                "inner join suppliers on suppliers.supplier_id = purchase_returns.supplier_id " +
                "inner join users on users.Id = purchase_returns.user_id " +
                "where purchase_returns.date between '" + start + "' and '" + end + "'";
        ResultSet resultSet = DBUtil.executeQuery(sql);
        if (resultSet != null) {
            while (resultSet.next()) {
                PurchaseReturn purchaseReturn = new PurchaseReturn();
                purchaseReturn.setReturnId(resultSet.getInt("Id"));
                purchaseReturn.setInvoiceNo(resultSet.getString("invoice"));
                purchaseReturn.setReturnedBy(resultSet.getString("FirstName") + " " + resultSet.getString("LastName"));
                purchaseReturn.setDate(resultSet.getObject("date", LocalDate.class));
                purchaseReturn.setSupplier(resultSet.getString("name"));
                returns.add(purchaseReturn);
            }
        }

        return returns;
    }

    private void setUpTable() {
        //column widths
        for (TableColumn column : purchaseReturnTableView.getColumns()) {
            if (column != supplier) {
                column.prefWidthProperty().bind(purchaseReturnTableView.widthProperty().divide(6));
            } else {
                column.prefWidthProperty().bind(purchaseReturnTableView.widthProperty().divide(3));
            }
        }
        //data
        invoice.setCellValueFactory(param -> param.getValue().invoiceNoProperty());
        supplier.setCellValueFactory(param -> param.getValue().supplierProperty());
        dateReturned.setCellValueFactory(param -> DateUtil.dateStringProperty(param.getValue().getDate()));
        returnedBy.setCellValueFactory(param -> param.getValue().returnedByProperty());
        details.setCellFactory(param -> new TableCell<PurchaseReturn, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                int index = getIndex();
                if (index >= 0 && index < purchaseReturnTableView.getItems().size()) {
                    Button button = new Button("Details");
                    button.getStyleClass().add("btn-info-outline");
                    button.setOnAction(event -> {
                        viewPurchaseReturnsDetails(purchaseReturnTableView.getItems().get(index));
                    });
                    setGraphic(button);
                } else {
                    setGraphic(null);
                }
            }
        });
        //place holder
        Label label = new Label("Retrieving data ...");
        label.getStyleClass().add("text-info");
        purchaseReturnTableView.setPlaceholder(label);
    }

    private void viewPurchaseReturnsDetails(PurchaseReturn purchaseReturn) {
        //TODO : show dialog box to show details of purchase return
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource
                    ("main/resources/view/view-purchase-return.fxml"));
            Stage stage = new Stage();
            stage.setScene(new Scene(loader.load()));
            stage.initModality(Modality.WINDOW_MODAL);
            stage.setResizable(false);
            stage.initOwner(container.getScene().getWindow());

            ViewPurchaseReturnController controller = loader.getController();
            controller.setPurchaseReturn(purchaseReturn);
            stage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onNewReturn() {
        //TODO : show diaog box to create return
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource
                    ("main/resources/view/edit-purchase-return.fxml"));
            Stage stage = new Stage();
            stage.setScene(new Scene(loader.load()));
            stage.initModality(Modality.WINDOW_MODAL);
            stage.setResizable(false);
            stage.initOwner(container.getScene().getWindow());

            EditPurchaseReturnController controller = loader.getController();
            controller.setStage(stage);
            stage.showAndWait();

            getData();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
