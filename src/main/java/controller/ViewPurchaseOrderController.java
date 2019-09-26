package main.java.controller;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;
import main.java.dao.HospitalDAO;
import main.java.dao.PurchasesDao;
import main.java.dao.SupplierDAO;
import main.java.model.Hospital;
import main.java.model.PurchaseOrder;
import main.java.model.Supplier;
import main.java.util.CurrencyUtil;
import main.java.util.DateUtil;

public class ViewPurchaseOrderController {
    private Stage stage;
    @FXML
    private Label orderNumber, date, supplierName, supplierAddress, supplierPhone, hospitalName, hospitalAddress,
            hospitalPhone, orderTotal;
    @FXML
    private TableView<PurchaseOrder> purchaseOrderTableView;
    @FXML
    private TableColumn<PurchaseOrder, String> drugName;
    @FXML
    private TableColumn<PurchaseOrder, Integer> quantity, itemCode;
    @FXML
    private TableColumn<PurchaseOrder, Double> total, unitCost;
    private PurchaseOrder order;

    @FXML
    private void initialize() {
        setUpTable();
    }

    private void setUpTable() {
        for (TableColumn tableColumn : purchaseOrderTableView.getColumns()) {
            if (tableColumn == drugName) {
                tableColumn.prefWidthProperty().bind(purchaseOrderTableView.widthProperty().divide(3));
            } else {
                tableColumn.prefWidthProperty().bind(purchaseOrderTableView.widthProperty().divide(6));
            }
        }
        //data
        drugName.setCellValueFactory(param -> param.getValue().descriptionProperty());
        quantity.setCellValueFactory(param -> param.getValue().quantityProperty().asObject());
        unitCost.setCellValueFactory(param -> param.getValue().unitPriceProperty().asObject());
        total.setCellValueFactory(param -> new SimpleDoubleProperty(param.getValue().getUnitPrice() * param.getValue()
                .getQuantity()).asObject());
        itemCode.setCellValueFactory(param -> param.getValue().drugIdProperty().asObject());
    }

    private void setSupplierDetails(Supplier supplier) {
        if (supplier != null) {
            supplierName.setText(supplier.getName());
            supplierAddress.setText(supplier.getAddress());
            supplierPhone.setText(supplier.getPhoneNumber());

            supplierPhone.setVisible(supplier.getPhoneNumber() != null && !supplier.getPhoneNumber().isEmpty());
            supplierAddress.setVisible(supplier.getAddress() != null && !supplier.getAddress().isEmpty());
        }
    }

    private void setOrderTotal() {
        double totalCost = 0;
        for (PurchaseOrder order : purchaseOrderTableView.getItems()) {
            totalCost += total.getCellData(order);
        }
        orderTotal.setText("Ksh. " + CurrencyUtil.formatCurrency(totalCost));
    }

    void setStage(Stage stage) {
        this.stage = stage;
    }

    @FXML
    private void onPrint() {
        //TODO : figure out how to print
    }

    private void getSupplierInfo() {
        Task<Supplier> supplierTask = new Task<Supplier>() {
            @Override
            protected Supplier call() {
                return SupplierDAO.getSupplier(getOrder().getSupplierId());
            }
        };
        supplierTask.setOnSucceeded(event -> {
            setSupplierDetails(supplierTask.getValue());
        });
        new Thread(supplierTask).start();
    }

    private void getHospitalInfo() {
        Task<Hospital> task = new Task<Hospital>() {
            @Override
            protected Hospital call() {
                return HospitalDAO.getHospital();
            }
        };
        task.setOnSucceeded(event -> {
            setHospitalInfo(task.getValue());
        });
        new Thread(task).start();
    }

    private void setHospitalInfo(Hospital hospital) {
        hospitalAddress.setText(hospital.getAddress());
        hospitalName.setText(hospital.getName());
        hospitalPhone.setText(hospital.getPhoneNumber());
    }

    private void getOrderDetails() {
        Task<ObservableList<PurchaseOrder>> ordersTask = new Task<ObservableList<PurchaseOrder>>() {
            @Override
            protected ObservableList<PurchaseOrder> call() {
                return PurchasesDao.getOrderByID(getOrder().getOrderId());
            }
        };

        ordersTask.setOnSucceeded(event -> {
            purchaseOrderTableView.setItems(ordersTask.getValue());
            setOrderTotal();

        });
        new Thread(ordersTask).start();
    }

    PurchaseOrder getOrder() {
        return order;
    }

    void setOrder(PurchaseOrder order) {
        this.order = order;
        orderNumber.setText("ORDER NO. " + order.getOrderId());
        date.setText(DateUtil.formatDateLong(order.getOrderDate()));
        getOrderDetails();
        getHospitalInfo();
        getSupplierInfo();
    }
}
