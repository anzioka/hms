package main.java.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import main.java.model.Bill;
import main.java.model.Inpatient;
import main.java.model.PaymentMode;
import main.java.util.*;

import java.sql.ResultSet;
import java.time.LocalDate;

public class InpatientMiscellaneousCharges {
    @FXML
    private TableView<Bill> tableView;
    @FXML
    private TableColumn<Bill, String> date, description, amount, options;
    @FXML
    private DatePicker datePicker;
    @FXML
    private TextField editDescription, editAmount;

    @FXML
    private void initialize() {
        datePicker.setConverter(DateUtil.getDatePickerConverter());
        datePicker.setValue(LocalDate.now());
        setUpTable();
        fetchData();
    }

    private void fetchData() {
        Task<ObservableList<Bill>> task = new Task<ObservableList<Bill>>() {
            @Override
            protected ObservableList<Bill> call() throws Exception {
                ObservableList<Bill> bills = FXCollections.observableArrayList();
                String sql = "select id, description, date_created, amount from billing " +
                        "where admission_num = " + ViewInpatientController.patient.getAdmissionNumber() + " " +
                        "and category = '" + Bill.Category.OTHER + "'";
                ResultSet resultSet = DBUtil.executeQuery(sql);
                if (resultSet != null) {
                    while (resultSet.next()) {
                        Bill bill = new Bill();
                        bill.setDateCreated(resultSet.getObject("date_created", LocalDate.class));
                        bill.setAmount(resultSet.getDouble("amount"));
                        bill.setDescription(resultSet.getString("description"));
                        bill.setId(resultSet.getInt("id"));
                        bills.add(bill);
                    }
                }
                return bills;
            }
        };
        task.setOnSucceeded(event -> {
            tableView.setItems(task.getValue());
        });
        new Thread(task).start();
    }

    @FXML
    private void onAddToBill() {
        if (validInput()) {
            Bill bill = createBill();
            if (DBUtil.saveBill(bill)) {
                AlertUtil.showAlert("Patient Charges", "New charge added to patient bill", Alert.AlertType.INFORMATION);
                tableView.getItems().add(bill);
                editDescription.clear();
                editAmount.clear();
            } else {
                AlertUtil.showAlert("Error", "An error occurred while attempting to save changes", Alert.AlertType.ERROR);
            }
        }
    }

    private Bill createBill() {
        Bill bill = new Bill();
        bill.setBillNumber(ViewInpatientController.patient.getBillNumber());
        bill.setAmount(NumberUtil.stringToDouble(editAmount.getText()));
        bill.setDescription(editDescription.getText());

        Inpatient patient = ViewInpatientController.patient;
        if (patient.getPaymentMode() == PaymentMode.INSURANCE) {
            bill.setInsurer(patient.getInsurer());
            bill.setInsuranceId(patient.getInsuranceID());
        }
        bill.setPatientNumber(patient.getPatientId());
        bill.setCategory(Bill.Category.OTHER);
        bill.setAdmissionNumber(patient.getAdmissionNumber());
        bill.setDateCreated(LocalDate.now());
        return bill;
    }

    private boolean validInput() {
        String errorMsg = "";
        if (editDescription.getText() == null || editDescription.getText().isEmpty()) {
            errorMsg += "Description required\n";
        }
        if (NumberUtil.stringToDouble(editAmount.getText()) == -1) {
            errorMsg += "Invalid amount\n";
        }
        if (errorMsg.isEmpty()) {
            return true;
        }
        AlertUtil.showAlert("Input Error(s)", errorMsg, Alert.AlertType.ERROR);
        return false;
    }

    private void setUpTable() {
        for (TableColumn column : tableView.getColumns()) {
            if (column == description) {
                column.prefWidthProperty().bind(tableView.widthProperty().divide(2));
            } else {
                column.prefWidthProperty().bind(tableView.widthProperty().divide(6));
            }
        }
        date.setCellValueFactory(param -> DateUtil.dateStringProperty(param.getValue().getDateCreated()));
        description.setCellValueFactory(param -> param.getValue().descriptionProperty());
        amount.setCellValueFactory(param -> CurrencyUtil.getStringProperty(param.getValue().getAmount()));
        options.setCellFactory(param -> new TableCell<Bill, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                int index = getIndex();
                if (index >= 0 && index < tableView.getItems().size()) {
                    Button button = new Button("Delete");
                    button.getStyleClass().add("btn-danger-outline");
                    button.setOnAction(event -> {
                        String sql = "delete from billing where id = " + tableView.getItems().get(index).getId();
                        if (DBUtil.executeStatement(sql)) {
                            tableView.getItems().remove(index);
                        }
                    });
                    setGraphic(button);
                } else {
                    setGraphic(null);
                }
            }
        });
    }
}
