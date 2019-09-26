package main.java.controller;

import javafx.beans.property.SimpleStringProperty;
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
import main.java.dao.BillingDao;
import main.java.model.PatientCategory;
import main.java.model.PaymentMode;
import main.java.util.CurrencyUtil;

import java.io.IOException;
import java.util.Map;

import static main.java.controller.BillingController.*;

public class UncollectedCashPaymentsController {
    private ObservableList<Map<String, String>> allBills = FXCollections.observableArrayList();
    @FXML
    private VBox container;
    @FXML
    private TableView<Map<String, String>> tableView;
    @FXML
    private TableColumn<Map<String, String>, String> billNo, patientNo, patientName, amountPaid, outstanding, options, date;
    @FXML
    private TextField searchField;

    @FXML
    private ChoiceBox<PatientCategory> categoryChoiceBox;

    @FXML
    private void initialize() {

        getBillsData();
        setUpTable();

        categoryChoiceBox.setItems(FXCollections.observableArrayList(PatientCategory.values()));
        categoryChoiceBox.getSelectionModel().select(PatientCategory.ALL);
        categoryChoiceBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            filterByCategory(newValue);
        });

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterByPatientName(newValue);
        });
    }

    private void filterByCategory(PatientCategory category) {
        if (category == PatientCategory.ALL) {
            tableView.setItems(allBills);
        } else {
            ObservableList<Map<String, String>> filtered = FXCollections.observableArrayList();
            for (Map<String, String> data : allBills) {
                if (data.get(CATEGORY).equals(category.toString())) {
                    filtered.add(data);
                }
            }
            tableView.setItems(filtered);
        }
    }

    private void setUpTable() {
        Label label = new Label("No bills found!");
        label.getStyleClass().add("text-danger");
        tableView.setPlaceholder(label);
        date.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().get(DATE)));
        billNo.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().get(BILL_NUM)));
        patientNo.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().get(PATIENT_NUM)));
        patientName.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().get(PATIENT_NAME)));
        amountPaid.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().get(AMOUNT_PAID)));
        outstanding.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().get(OUTSTANDING_AMOUNT)));
        options.setCellFactory(param -> new TableCell<Map<String, String>, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                int index = getIndex();
                if (index >= 0 && index < tableView.getItems().size()) {
                    Button button = new Button("Add Payment");
                    button.setOnAction(event -> {
                        addNewPayment(index);
                    });
                    button.getStyleClass().add("btn-info-outline");
                    setGraphic(button);
                } else {
                    setGraphic(null);
                }
            }
        });
    }

    private void addNewPayment(int index) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("main/resources/view/add-payment.fxml"));
            Stage stage = new Stage();
            stage.setScene(new Scene(loader.load()));
            stage.setTitle("New Payment");
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(container.getScene().getWindow());

            Map<String, String> entry = tableView.getItems().get(index);
            AddPaymentController controller = loader.getController();
            controller.setStage(stage);
            controller.setParameters(entry.get(PATIENT_NAME), PaymentMode.CASH.toString(),
                    entry.get(PATIENT_NUM), entry.get(BILL_NUM),
                    entry.get(OUTSTANDING_AMOUNT),
                    entry.get(CATEGORY).equals(PatientCategory.INPATIENT.toString()),
                    entry.get(DATE));
            stage.showAndWait();
            entry.put(AMOUNT_PAID, CurrencyUtil.formatCurrency(CurrencyUtil.parseCurrency(entry.get(AMOUNT_PAID)) + controller.getAmountPaid()));
            entry.put(OUTSTANDING_AMOUNT, CurrencyUtil.formatCurrency(CurrencyUtil.parseCurrency(entry.get(OUTSTANDING_AMOUNT)) - controller.getAmountPaid()));
            if (CurrencyUtil.parseCurrency(entry.get(OUTSTANDING_AMOUNT)) == 0) {
                tableView.getItems().remove(entry);
            }
            tableView.refresh();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void filterByPatientName(String searchStr) {
        if (searchStr == null || searchStr.isEmpty()) {
            tableView.setItems(allBills);
            return;
        }
        ObservableList<Map<String, String>> filteredResults = FXCollections.observableArrayList();
        for (Map<String, String> entry : tableView.getItems()) {
            if (entry.get(PATIENT_NAME).toLowerCase().contains(searchStr.toLowerCase())) {
                filteredResults.add(entry);
            }
        }
        tableView.setItems(filteredResults);
    }

    private void getBillsData() {

        Task<ObservableList<Map<String, String>>> task = new Task<ObservableList<Map<String, String>>>() {
            @Override
            protected ObservableList<Map<String, String>> call() throws Exception {
                return BillingDao.getAllPatientBills(PaymentMode.CASH);
            }

        };
        task.setOnSucceeded(event -> {
            allBills = task.getValue();
            tableView.setItems(task.getValue());
        });
        new Thread(task).start();
    }
}
