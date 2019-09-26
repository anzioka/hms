package main.java.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import main.java.dao.BillingDao;
import main.java.dao.InsuranceDAO;
import main.java.dao.SettingsDao;
import main.java.dao.UsersDAO;
import main.java.model.*;
import main.java.util.*;

import java.time.LocalDate;
import java.time.LocalTime;

public class QueuePatientController {
    private Stage stage;
    private Patient patient;
    private PaymentMode paymentMode;
    private int queueId, billNumber;
    private boolean queuingSuccessful;
    @FXML
    private HBox selectDocHolder, feeHolder;
    @FXML
    private ChoiceBox<User> doctorChoiceBox;
    @FXML
    private ChoiceBox<ServiceType> serviceTypeChoiceBox;
    @FXML
    private GridPane insuranceInfo;
    @FXML
    private Label inpatientNo, firstName, surname, DoB, age, sex, paymentModeLabel, insuranceCo, insuranceNo;
    @FXML
    private TextField feeTextField;

    void setStage(Stage stage) {
        this.stage = stage;
    }

    @FXML
    private void initialize() {
        billNumber = BillingDao.getNextBillNumber();
        queueId = DBUtil.getNextAutoIncrementId("queues");

        Task<ObservableList<User>> task = new Task<ObservableList<User>>() {
            @Override
            protected ObservableList<User> call() {
                return UsersDAO.getUserObservableList("select * from users where UserCategory ='" + UserCategory.DOCTOR + "'");
            }
        };
        task.setOnSucceeded(event -> {
            doctorChoiceBox.setItems(task.getValue());
        });
        new Thread(task).start();
    }

    void setParameters(Patient patient, PaymentMode value) {
        this.patient = patient;
        this.paymentMode = value;

        serviceTypeChoiceBox.setItems(FXCollections.observableArrayList(ServiceType.values()));
        serviceTypeChoiceBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            for (HBox hBox : new HBox[]{feeHolder, selectDocHolder}) {
                hBox.setVisible(newValue == ServiceType.CONSULTATION || newValue == ServiceType.REVIEW);
                hBox.setManaged(newValue == ServiceType.CONSULTATION || newValue == ServiceType.REVIEW);
            }
            setFeeAmount();
            doctorChoiceBox.requestFocus();
        });
        serviceTypeChoiceBox.setValue(ServiceType.CONSULTATION);
        setPatientDetails();
    }

    private void setPatientDetails() {
        paymentModeLabel.setText("PAYMENT MODE : " + paymentMode.name());
        insuranceInfo.setManaged(paymentMode != PaymentMode.CASH);
        insuranceInfo.setVisible(paymentMode != PaymentMode.CASH);
        insuranceCo.setText(patient.getInsurer());
        insuranceNo.setText(patient.getInsuranceID());
        sex.setText(patient.getSexuality());
        age.setText(AgeUtil.getAge(patient.getDateOfBirth()));
        DoB.setText(DateUtil.formatDate(patient.getDateOfBirth()));
        surname.setText(patient.getLastName());
        firstName.setText(patient.getFirstName());
        inpatientNo.setText(patient.getPatientId());

    }

    private void setFeeAmount() {
        Setting setting = SettingsDao.getSettings();

        if (serviceTypeChoiceBox.getValue() == ServiceType.REVIEW) {
            feeTextField.setText("0");
        } else {
            if (paymentMode == PaymentMode.CASH) {
                feeTextField.setText(CurrencyUtil.formatCurrency(setting.getConsultationFee()));
            } else {
                Double fee = InsuranceDAO.getConsultationFee(patient.getInsurer());
                if (fee == null) {
                    feeTextField.setText(CurrencyUtil.formatCurrency(setting.getCorporateConsultationFee()));
                } else {
                    feeTextField.setText(CurrencyUtil.formatCurrency(fee));
                }
            }
        }
    }

    boolean isQueuingSuccessful() {
        return queuingSuccessful;
    }

    @FXML
    private void onQueue() {
        boolean error;
        if (validInput()) {
            error = !addToQueue() || !saveBill();
            if (error) {
                AlertUtil.showAlert("Error", "An error occurred while trying to save data", Alert.AlertType.ERROR);
            } else {
                AlertUtil.showAlert("Successful", "Patient has been successfully added to queue", Alert.AlertType.INFORMATION);
                queuingSuccessful = true;
                stage.close();
            }
        }

    }

    private boolean saveBill() {
        if (serviceTypeChoiceBox.getValue() == ServiceType.CONSULTATION && !feeTextField.getText().equals("0")) {
            Bill bill = new Bill();
            bill.setBillNumber(billNumber);

            if (paymentMode == PaymentMode.INSURANCE) {
                bill.setInsurer(patient.getInsurer());
                bill.setInsuranceId(patient.getInsuranceID());
            }
            bill.setAmount(CurrencyUtil.parseCurrency(feeTextField.getText()));
            bill.setPatientNumber(patient.getPatientId());
            bill.setCategory(Bill.Category.CONSULTATION);
            bill.setDescription("Consultation charges");

            bill.setDateCreated(LocalDate.now());
            bill.setQueueNumber(queueId);

            return DBUtil.saveBill(bill);
        } else {
            return true;
        }
    }

    private boolean addToQueue() {
        PatientQueue patientQueue = new PatientQueue();
        patientQueue.setQueueId(queueId);
        patientQueue.setTimeCreated(LocalTime.now());
        patientQueue.setDateCreated(LocalDate.now());
        if (serviceTypeChoiceBox.getValue() != ServiceType.CONSULTATION) {
            patientQueue.setDoctorId(0);
        } else {
            patientQueue.setDoctorId(doctorChoiceBox.getValue().getUserId());
        }
        if (serviceTypeChoiceBox.getValue() == ServiceType.PHARMACY) {
            patientQueue.setStatus(PatientQueue.Status.AWAITING_PRESCRIPTION);
        }
        if (serviceTypeChoiceBox.getValue() == ServiceType.LAB_TEST) {
            patientQueue.setStatus(PatientQueue.Status.AWAITING_LAB);
        }
        patientQueue.setBillNumber(billNumber);
        patientQueue.setPatientId(patient.getPatientId());
        patientQueue.setPaymentMode(paymentMode);
        patientQueue.setServiceType(serviceTypeChoiceBox.getValue());

        return DBUtil.addQueue(patientQueue);
    }

    private boolean validInput() {
        String error = "";
        ServiceType serviceType = serviceTypeChoiceBox.getValue();
        if (serviceType == null) {
            error += "Please select service.\n";
        } else if (serviceType == ServiceType.CONSULTATION) {
            if (doctorChoiceBox.getValue() == null) {
                error += "Please select doctor";
            }
        }
        if (CurrencyUtil.parseCurrency(feeTextField.getText()) == -1) {
            error += "Invalid fee amount!\n";
        }
        if (error.isEmpty()) {
            return true;
        }
        AlertUtil.showAlert("Error", error, Alert.AlertType.ERROR);
        return false;
    }
}
