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
import main.java.util.AlertUtil;
import main.java.util.CurrencyUtil;
import main.java.util.DBUtil;

import java.io.IOException;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static main.java.controller.BillingController.*;

public class InvoicesController {
    private static final String ALL_INSURANCE = "All Insurances";
    private ObservableList<Map<String, String>> allBills = FXCollections.observableArrayList();
    @FXML
    private ChoiceBox<String> insuranceChoiceBox;
    @FXML
    private ChoiceBox<PatientCategory> patientCategoryChoiceBox;
    @FXML
    private TextField searchField;
    @FXML
    private Button aggregateInvoicesBtn;
    @FXML
    private VBox container;
    @FXML
    private TableView<Map<String, String>> tableView;
    @FXML
    private TableColumn<Map<String, String>, String> billNo, patientNum, patientName, amountPaid, outstanding, insurance, options;

    @FXML
    private void initialize() {
        getData();
        getInsuranceNames();
        setUpTable();
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterByPatientName(newValue);
        });

        patientCategoryChoiceBox.setItems(FXCollections.observableArrayList(PatientCategory.values()));
        patientCategoryChoiceBox.getSelectionModel().select(PatientCategory.ALL);
        patientCategoryChoiceBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            filterByInsuranceAndPatientCategory();
        });

    }

    private void filterByInsuranceAndPatientCategory() {
        ObservableList<Map<String, String>> finalList = FXCollections.observableArrayList();
        ObservableList<Map<String, String>> insuranceFilter = filterByInsurance(), categoryFilter = filterByCategory();
        for (Map<String, String> entry : insuranceFilter) {
            if (categoryFilter.contains(entry)) {
                finalList.add(entry);
            }
        }
        tableView.setItems(finalList);
    }

    private ObservableList<Map<String, String>> filterByCategory() {
        PatientCategory category = patientCategoryChoiceBox.getValue();
        if (category == null || category == PatientCategory.ALL) {
            return allBills;
        }
        ObservableList<Map<String, String>> list = FXCollections.observableArrayList();
        for (Map<String, String> data : allBills) {
            if (data.get(CATEGORY).equals(category.toString())) {
                list.add(data);
            }
        }
        return list;
    }

    private ObservableList<Map<String, String>> filterByInsurance() {
        String insurance = insuranceChoiceBox.getValue();
        if (insurance == null || insurance.equals(ALL_INSURANCE)) {
            return allBills;
        }
        ObservableList<Map<String, String>> list = FXCollections.observableArrayList();
        for (Map<String, String> map : allBills) {
            if (map.get(INSURER) != null && map.get(INSURER).equals(insurance)) {
                list.add(map);
            }
        }

        return list;
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

    private void setUpTable() {
        Label label = new Label("No unpaid invoices found!");
        label.getStyleClass().add("text-danger");
        tableView.setPlaceholder(label);

        billNo.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().get(BILL_NUM)));
        patientNum.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().get(PATIENT_NUM)));
        patientName.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().get(PATIENT_NAME)));
        amountPaid.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().get(AMOUNT_PAID)));
        outstanding.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().get(OUTSTANDING_AMOUNT)));
        insurance.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().get(INSURER)));
        options.setCellFactory(param -> new TableCell<Map<String, String>, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                int index = getIndex();
                if (index >= 0 && index < tableView.getItems().size()) {
                    Button invoice = new Button("Invoice");
                    invoice.getStyleClass().add("btn-info-outline");
                    invoice.setOnAction(event -> {
                        new PrintInvoice().showInvoice(tableView.getItems().get(index));
                    });
                    invoice.setPrefWidth(105);

                    Button add_payment = new Button("Co-Pay");
                    add_payment.setPrefWidth(105);
                    add_payment.setOnAction(event -> {
                        copayBill(index);

                    });
                    add_payment.getStyleClass().add("btn-info-outline");
                    VBox vBox = new VBox(5.0, invoice, add_payment);
                    vBox.setAlignment(Pos.CENTER);
                    setGraphic(vBox);
                } else {
                    setGraphic(null);
                }
            }
        });
    }


    private void copayBill(int index) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("main/resources/view/add-payment.fxml"));
            Stage stage = new Stage();
            stage.setScene(new Scene(loader.load()));
            stage.setTitle("Invoice #" + tableView.getItems().get(index).get(BILL_NUM) + " : Co-Payment");
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(container.getScene().getWindow());

            Map<String, String> entry = tableView.getItems().get(index);
            AddPaymentController controller = loader.getController();
            controller.setStage(stage);
            controller.setParameters(entry.get(PATIENT_NAME), PaymentMode.INSURANCE.toString(),
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

    private void getData() {
        Task<ObservableList<Map<String, String>>> task = new Task<ObservableList<Map<String, String>>>() {
            @Override
            protected ObservableList<Map<String, String>> call() throws Exception {
                return BillingDao.getAllPatientBills(PaymentMode.INSURANCE);
            }
        };
        task.setOnSucceeded(event -> {
            tableView.setItems(task.getValue());
            allBills = task.getValue();
        });
        new Thread(task).start();
    }

    @FXML
    private void onGroupInvoices() {
        if (tableView.getItems().size() == 0) {
            AlertUtil.showAlert("Invoices", "No invoices for the selected insurance company!", Alert.AlertType.INFORMATION);

        } else{
            new GroupInvoices().showInvoices(tableView.getItems());
        }

    }

    private void getInsuranceNames() {
        Task<List<String>> task = new Task<List<String>>() {

            @Override
            protected List<String> call() throws Exception {
                List<String> list = new ArrayList<>();
                list.add(ALL_INSURANCE);
                String sql = "select distinct name from insurance";
                ResultSet resultSet = DBUtil.executeQuery(sql);
                if (resultSet != null) {
                    while (resultSet.next()) {
                        list.add(resultSet.getString("name"));
                    }
                }
                return list;
            }
        };
        task.setOnSucceeded(event -> {
            insuranceChoiceBox.setItems(FXCollections.observableArrayList(task.getValue()));
            insuranceChoiceBox.getSelectionModel().select(ALL_INSURANCE);
            insuranceChoiceBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
                filterByInsuranceAndPatientCategory();
                aggregateInvoicesBtn.setVisible(!newValue.equals(ALL_INSURANCE));
            });
        });
        new Thread(task).start();

    }

}
