package main.java.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import main.java.dao.BillingDao;
import main.java.dao.InpatientDao;
import main.java.dao.InsuranceDAO;
import main.java.dao.UsersDAO;
import main.java.model.*;
import main.java.util.*;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AdmitPatientController {
    private boolean admissionSuccessful = false;
    private Stage stage;
    @FXML
    private GridPane insuranceInfo;
    @FXML
    private Label inpatientNo, paymentMode, firstName, surname, DoB, age, sex, insuranceCo, insuranceNo;
    @FXML
    private TextField NHIFNumber;
    @FXML
    private ChoiceBox<NHIFApplicability> nhifApplicabilityChoiceBox;
    @FXML
    private HBox setBedLayoutBox;
    @FXML
    private ScrollPane scrollPane;
    @FXML
    private ChoiceBox<Integer> numColumnsChoiceBox;
    @FXML
    private ChoiceBox<String> hourChoiceBox, minuteChoiceBox;
    @FXML
    private ChoiceBox<TimePeriod> timePeriodChoiceBox;
    @FXML
    private ChoiceBox<User> doctorChoiceBox;
    @FXML
    private ChoiceBox<Ward> wardChoiceBox;
    @FXML
    private DatePicker datePicker;
    @FXML
    private FlowPane flowPane;
    private Set<Integer> assignedBeds = new HashSet<>();
    private HBox selectedBed = null;
    private Patient patient;
    private Inpatient inpatient;
    private PaymentMode paymentModeVal;
    private Integer billNumber;
    private int admissionNumber;
    private TimePicker timePicker;

    @FXML
    private void initialize() {
        datePicker.setConverter(DateUtil.getDatePickerConverter());
        datePicker.setValue(LocalDate.now());

        nhifApplicabilityChoiceBox.setItems(FXCollections.observableArrayList(NHIFApplicability.values()));
        nhifApplicabilityChoiceBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue,
                                                                                           newValue) -> {
            NHIFNumber.setDisable(newValue == NHIFApplicability.NON_APPLICABLE);
        });
        nhifApplicabilityChoiceBox.getSelectionModel().select(NHIFApplicability.NON_APPLICABLE);

        numColumnsChoiceBox.setItems(FXCollections.observableArrayList(1, 2, 3));
        numColumnsChoiceBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            prepareBedChart();
        });
        toggleBedVisibility(false);

        timePicker = new TimePicker(hourChoiceBox, minuteChoiceBox, timePeriodChoiceBox);
        timePicker.configureTime();
        getData();
    }

    private void getData() {
        //wards
        Task<ObservableList<Ward>> task = new Task<ObservableList<Ward>>() {
            @Override
            protected ObservableList<Ward> call() {
                return InpatientDao.getWards();
            }
        };
        task.setOnSucceeded(event -> {
            wardChoiceBox.setItems(task.getValue());
            wardChoiceBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue != null) {
                    numColumnsChoiceBox.setValue(newValue.getBedsPerRow());
                    getBedsInfoForWard();
                }
            });
        });
        new Thread(task).start();

        //doctors
        Task<ObservableList<User>> doctorListTask = new Task<ObservableList<User>>() {
            @Override
            protected ObservableList<User> call() {
                String sql = "select * from users where UserCategory ='" + UserCategory.DOCTOR + "'";
                ;
                return UsersDAO.getUserObservableList(sql);
            }
        };
        doctorListTask.setOnSucceeded(event -> {
            doctorChoiceBox.setItems(doctorListTask.getValue());
        });

        new Thread(doctorListTask).start();

        Task task1 = new Task() {
            @Override
            protected Object call() {
                admissionNumber = DBUtil.getNextAutoIncrementId("inpatients");
                if (billNumber == null) {
                    billNumber = BillingDao.getNextBillNumber();
                }
                return null;
            }
        };
        new Thread(task1).start();
    }

    private void getBedsInfoForWard() {
        //get assigned beds
        Task<Set<Integer>> assignedBedsTask = new Task<Set<Integer>>() {
            @Override
            protected Set<Integer> call() {
                Set<Integer> assignedBeds = new HashSet<>();
                Ward selectedWard = wardChoiceBox.getValue();
                String sql = "select bed_id from inpatients where ward_id =" + selectedWard.getId() + " " +
                        "and status = '" + Inpatient.Status.ADMITTED + "'";
                ResultSet resultSet = DBUtil.executeQuery(sql);
                if (resultSet != null) {
                    try {
                        while (resultSet.next()) {
                            assignedBeds.add(resultSet.getInt("bed_id"));
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
                return assignedBeds;
            }
        };

        assignedBedsTask.setOnSucceeded(event -> {
            assignedBeds = assignedBedsTask.getValue();
            toggleBedVisibility(true);
            prepareBedChart();
        });

        new Thread(assignedBedsTask).start();
        //after getting data set toggleBedVisibility(true)
    }

    private void toggleBedVisibility(boolean visible) {
        setBedLayoutBox.setVisible(visible);
        scrollPane.setVisible(visible);
    }

    void setParameters(Patient patient, Inpatient inpatient, PaymentMode paymentModeVal, Integer billNumber) {
        this.patient = patient;
        this.inpatient = inpatient;
        this.paymentModeVal = paymentModeVal;
        this.billNumber = billNumber;
        firstName.setText(patient.getFirstName());
        inpatientNo.setText(inpatient.getInpatientNumber());
        paymentMode.setText("PAYMENT MODE : " + paymentModeVal.name());
        surname.setText(patient.getLastName());
        DoB.setText(DateUtil.formatDateLong(patient.getDateOfBirth()));
        age.setText(AgeUtil.getAge(patient.getDateOfBirth()));
        sex.setText(patient.getSexuality());
        insuranceCo.setText(patient.getInsurer());
        insuranceNo.setText(patient.getInsuranceID());
        NHIFNumber.setText(patient.getNHIFNumber());

        if (patient.getNHIFNumber() != null && !patient.getNHIFNumber().isEmpty()) {
            nhifApplicabilityChoiceBox.getSelectionModel().select(NHIFApplicability.APPLICABLE);
        }
        insuranceInfo.setVisible(paymentModeVal == PaymentMode.INSURANCE);
        insuranceInfo.setManaged(paymentModeVal == PaymentMode.INSURANCE);
    }

    boolean isAdmissionSuccessful() {
        return admissionSuccessful;
    }

    private void setAdmissionSuccessful() {
        this.admissionSuccessful = true;
    }

    private void prepareBedChart() {

        selectedBed = null;
        flowPane.getChildren().clear();
        int bedsPerRow = numColumnsChoiceBox.getValue();
        Ward ward = wardChoiceBox.getValue();
        int hBoxWidth = 200;
        int hBoxHeight = 40;
        int flowPaneHGap = 20;
        flowPane.setPrefWrapLength(hBoxWidth * bedsPerRow + (bedsPerRow - 1) * flowPaneHGap);

        for (int i = 0; i < ward.getNumBeds(); i++) {
            HBox hBox = new HBox();
            hBox.setPrefSize(hBoxWidth, hBoxHeight);
            hBox.setId(i + 1 + "");
            hBox.getStyleClass().add("border-light-3");
            hBox.setAlignment(Pos.CENTER);
            Label label = new Label(i + 1 + "");
            if (assignedBeds.contains(i + 1)) {
                hBox.setDisable(true);
                hBox.getStyleClass().add("bg-light-6");
            } else {
                label.getStyleClass().addAll("fw-500", "h6_5", "color-unique");
                hBox.setStyle("-fx-background-color: white");
                hBox.getStyleClass().add("bg-light-6");
                hBox.setOnMouseClicked(event -> {
                    selectBed(false);
                    selectedBed = (HBox) event.getSource();
                    selectBed(true);
                    ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
                    executorService.schedule(() -> {
                        //scroll to bottom
                    }, 1, TimeUnit.SECONDS);
                });
            }
            hBox.getChildren().add(label);
            flowPane.getChildren().add(hBox);
        }
    }

    private void selectBed(boolean select) {
        if (selectedBed != null) {
            Node label = selectedBed.getChildren().get(0);
            if (select) {
                selectedBed.setStyle("-fx-background-color: color-unique");
                selectedBed.getStyleClass().removeAll("border-light-3", "bg-light-6");

                label.getStyleClass().add("color-white");
                label.getStyleClass().remove("color-unique");
            } else {
                selectedBed.setStyle("-fx-background-color: white");
                selectedBed.getStyleClass().addAll("border-light-3", "bg-light-6");
                label = selectedBed.getChildren().get(0);
                label.getStyleClass().remove("color-white");
                label.getStyleClass().add("color-unique");
            }
        }

    }

    @FXML
    private void onAdmitPatient() {
        //bill

        //save to inpatients
        if (validInput()) {
            if (saveInPatient()) {
                if (saveBill()) {
                    AlertUtil.showAlert("Admit Patient", "Patient has been successfully admitted", Alert.AlertType
                            .INFORMATION);
                    setAdmissionSuccessful();
                    stage.close();
                } else {
                    AlertUtil.showAlert("Error", "An error occurred while attempting to save information", Alert
                            .AlertType.ERROR);
                }
            } else {
                AlertUtil.showAlert("Error", "An unknown error occurred.", Alert.AlertType.ERROR);
            }
        }
    }

    private boolean validInput() {
        String errorMsg = "";

        if (wardChoiceBox.getValue() == null) {
            errorMsg += "Select ward!\n";
        }
        if (selectedBed == null) {
            errorMsg += "Select bed!\n";
        }
        if (nhifApplicabilityChoiceBox.getValue() == NHIFApplicability.APPLICABLE) {
            if (NHIFNumber.getText() == null || NHIFNumber.getText().isEmpty()) {
                errorMsg += "NHIF number required!\n";
            }
        }
        if (doctorChoiceBox.getValue() == null) {
            errorMsg += "Select doctor!\n";
        }

        if (errorMsg.isEmpty()) {
            return true;
        }
        AlertUtil.showAlert("Error", errorMsg, Alert.AlertType.ERROR);
        return false;
    }

    private boolean saveBill() {
        Ward ward = wardChoiceBox.getValue();
        Bill bill = new Bill();
        bill.setBillNumber(billNumber);
        bill.setCategory(Bill.Category.ADMISSION);
        bill.setDateCreated(inpatient.getDateAdmitted());
        bill.setPatientNumber(patient.getPatientId());
        bill.setDescription("Admission charges");

        if (paymentModeVal == PaymentMode.CASH) {
            bill.setAmount(ward.getAdmissionCharge());
        } else {
            Double fee = InsuranceDAO.getConsultationFee(patient.getInsurer());
            if (fee != null) {
                bill.setAmount(fee);
            } else {
                bill.setAmount(ward.getCorporateAdmissionCharge());
            }
            bill.setInsuranceId(patient.getInsuranceID());
            bill.setInsurer(patient.getInsurer());
        }
        bill.setAdmissionNumber(admissionNumber);

        return DBUtil.saveBill(bill);
    }

    private boolean saveInPatient() {
        inpatient.setAdmissionNumber(admissionNumber);
        inpatient.setAssignedBed(selectedBed.getId());
        inpatient.setBedId(Integer.parseInt(selectedBed.getId()));
        inpatient.setPaymentMode(paymentModeVal);
        inpatient.setBillNumber(billNumber);
        inpatient.setWardId(wardChoiceBox.getValue().getId());
        if (nhifApplicabilityChoiceBox.getValue().equals(NHIFApplicability.APPLICABLE)) {
            inpatient.setNhifApplicable(true);
            if (!NHIFNumber.isDisabled()) {
                String sql = "update patients set NHIFNumber = '" + NHIFNumber.getText() + "' " +
                        "where PatientId =" + patient.getPatientId();
                DBUtil.executeStatement(sql);
            }
        }

        inpatient.setAssignedWard(wardChoiceBox.getValue().getName());
        LocalDate localDate = datePicker.getValue() == null ? LocalDate.now() : datePicker.getValue();
        inpatient.setDateAdmitted(localDate);
        inpatient.setTimeAdmitted(timePicker.getSelectedTime());
        inpatient.setDoctorId(doctorChoiceBox.getValue().getUserId());
        return DBUtil.admitPatient(inpatient);
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }
}
