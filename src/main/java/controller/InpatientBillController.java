package main.java.controller;

import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import main.java.dao.BillingDao;
import main.java.dao.InpatientDao;
import main.java.model.Bill;
import main.java.model.Inpatient;
import main.java.util.CurrencyUtil;
import main.java.util.DateUtil;
import main.java.util.StringUtil;

import java.time.LocalDate;

public class InpatientBillController {
    @FXML
    private TableView<Bill> tableView;
    @FXML
    private TableColumn<Bill, String> description, category, pendingAmount, paidAmount;
    @FXML
    private Label inpatientNo, name, dateAdmitted, paymentMode, ward, dateDischarged, totalAmount, totalNHIFRebate;
    private Inpatient patient;

    @FXML
    private void initialize() {
        setUpTable();
    }

    private void setUpTable() {
        for (TableColumn column : tableView.getColumns()) {
            if (column == description) {
                column.prefWidthProperty().bind(tableView.widthProperty().divide(2.5));
            } else {
                column.prefWidthProperty().bind(tableView.widthProperty().divide(5));
            }
        }

        description.setCellValueFactory(param -> param.getValue().descriptionProperty());
        category.setCellValueFactory(param -> StringUtil.getStringProperty(param.getValue().getCategory().toString()));
        paidAmount.setCellValueFactory(param -> CurrencyUtil.getStringProperty(param.getValue().getAmountPaid()));
        pendingAmount.setCellValueFactory(param -> CurrencyUtil.getStringProperty(param.getValue().getAmount()));
    }

    public void setAdmissionNumber(int admissionNumber) {
        getPatientDetails(admissionNumber);
    }

    private void getBill(int billNumber) {
        Task<ObservableList<Bill>> task = new Task<ObservableList<Bill>>() {
            @Override
            protected ObservableList<Bill> call() throws Exception {
                return BillingDao.getPatientBill(Integer.toString(billNumber), true);
            }
        };
        task.setOnSucceeded(event -> {
            tableView.setItems(task.getValue());
            setRebateTotal(billNumber);
            setTotalAmount();
        });
        new Thread(task).start();
    }

    private void setTotalAmount() {
        double total = 0;
        for (Bill bill : tableView.getItems()) {
            total += bill.getAmount();
        }
        totalAmount.setText("Ksh. " + CurrencyUtil.formatCurrency(total));
    }

    private void setRebateTotal(int billNumber) {
        Task<Double> task = new Task<Double>() {
            @Override
            protected Double call() throws Exception {
                double dailyRebate = BillingDao.getRebate(Integer.toString(billNumber));
                LocalDate dateDischarged = patient.getDateDischarged() != null ? patient.getDateDischarged() : LocalDate.now();
                return dailyRebate * DateUtil.getNumDaysDiff(patient.getDateAdmitted(), dateDischarged);
            }
        };
        task.setOnSucceeded(event -> {
            totalNHIFRebate.setText("Ksh. " + CurrencyUtil.formatCurrency(task.getValue()));
        });
        new Thread(task).start();
    }

    private void getPatientDetails(int admissionNumber) {
        Task<Inpatient> task = new Task<Inpatient>() {
            @Override
            protected Inpatient call() throws Exception {
                return InpatientDao.getPatient(admissionNumber);
            }
        };
        task.setOnSucceeded(event -> {

            if (task.getValue() != null) {
                this.patient = task.getValue();
                setPatientDetails(task.getValue());
                getBill(task.getValue().getBillNumber());
            }
        });
        new Thread(task).start();
    }

    private void setPatientDetails(Inpatient inpatient) {
        inpatientNo.setText(inpatient.getInpatientNumber());
        name.setText(inpatient.getFirstName() + " " + inpatient.getLastName());
        dateAdmitted.setText(DateUtil.formatDateLong(inpatient.getDateAdmitted()));
        paymentMode.setText(inpatient.getPaymentMode().toString());
        ward.setText(inpatient.getAssignedWard());
        if (inpatient.getDateDischarged() != null) {
            dateDischarged.setText(DateUtil.formatDateLong(inpatient.getDateDischarged()));
        }
    }
}
