package main.java.controller;

import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import main.Main;
import main.java.model.Inpatient;
import main.java.model.Permission;
import main.java.model.Ward;
import main.java.util.AlertUtil;
import main.java.util.CurrencyUtil;
import main.java.util.DBUtil;
import main.java.util.NumberUtil;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class EditWardController {
    private Ward ward;
    private Map<Integer, String> bedNumberPatientMap = new HashMap<>();
    @FXML
    private ChoiceBox<Integer> numColumnsChoiceBox;
    @FXML
    private FlowPane flowPane;
    @FXML
    private TextField wardName, numBeds, admissionCharge, dailyRate, corporateAdmissionCharge, corporateDailyRate,
            nurseVisitCharge, corporateNurseVisitCharge, doctorVisitCharge, corporateDoctorVisitCharge;
    private WardManagementController parent;

    @FXML
    private void initialize() {
        numColumnsChoiceBox.setItems(FXCollections.observableArrayList(1, 2, 3));
        numColumnsChoiceBox.getSelectionModel().select(1); //2 columns by default
        for (TextField textField : new TextField[] {wardName, numBeds, admissionCharge, dailyRate, corporateAdmissionCharge, corporateDailyRate, corporateDoctorVisitCharge, corporateNurseVisitCharge, nurseVisitCharge, doctorVisitCharge}) {
            textField.setEditable(Main.userPermissions.get(Permission.EDIT_WARD));
        }
    }

    private void getAdmissionData() {
        Task<Map<Integer, String>> task = new Task<Map<Integer, String>>() {
            @Override
            protected Map<Integer, String> call() {
                Map<Integer, String> map = new HashMap<>();
                String sql = "select patients.FirstName, patients.LastName, inpatients.bed_id " +
                        "from inpatients " +
                        "inner join patients on inpatients.patient_id = patients.PatientId " +
                        "where inpatients.status = '" + Inpatient.Status.ADMITTED + "' " +
                        "and ward_id = " + ward.getId();
                ResultSet resultSet = DBUtil.executeQuery(sql);
                if (resultSet != null) {
                    try {
                        while (resultSet.next()) {
                            map.put(resultSet.getInt("bed_id"), resultSet.getString("FirstName") + " " + resultSet
                                    .getString("LastName"));
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
                return map;
            }
        };

        task.setOnSucceeded(event -> {
            bedNumberPatientMap = task.getValue();
            prepareBedChart();
        });
        new Thread(task).start();
    }

    private void setWardDetails() {
        wardName.setText(ward.getName());
        numBeds.setText(ward.getNumBeds() + "");
        admissionCharge.setText(CurrencyUtil.formatCurrency(ward.getAdmissionCharge()));
        dailyRate.setText(CurrencyUtil.formatCurrency(ward.getRate()));
        corporateAdmissionCharge.setText(CurrencyUtil.formatCurrency(ward.getCorporateAdmissionCharge()));
        corporateDailyRate.setText(CurrencyUtil.formatCurrency(ward.getCorporateRate()));
        nurseVisitCharge.setText(CurrencyUtil.formatCurrency(ward.getNurseCharge()));
        corporateNurseVisitCharge.setText(CurrencyUtil.formatCurrency(ward.getCorporateNurseCharge()));
        doctorVisitCharge.setText(CurrencyUtil.formatCurrency(ward.getDoctorCharge()));
        corporateDoctorVisitCharge.setText(CurrencyUtil.formatCurrency(ward.getCorporateDoctorCharge()));
    }

    Ward getWard() {
        if (ward == null) {
            ward = new Ward();
            ward.setId(DBUtil.getNextAutoIncrementId("wards"));
        }
        ward.setBedsPerRow(numColumnsChoiceBox.getValue());
        ward.setName(wardName.getText());
        ward.setNumBeds(Integer.parseInt(numBeds.getText()));
        ward.setRate(CurrencyUtil.parseCurrency(dailyRate.getText()));
        ward.setCorporateRate(CurrencyUtil.parseCurrency(corporateDailyRate.getText()));
        ward.setAdmissionCharge(CurrencyUtil.parseCurrency(admissionCharge.getText()));
        ward.setCorporateAdmissionCharge(CurrencyUtil.parseCurrency(corporateAdmissionCharge.getText()));
        ward.setNurseCharge(CurrencyUtil.parseCurrency(nurseVisitCharge.getText()));
        ward.setCorporateNurseCharge(CurrencyUtil.parseCurrency(corporateNurseVisitCharge.getText()));
        ward.setDoctorCharge(CurrencyUtil.parseCurrency(doctorVisitCharge.getText()));
        ward.setCorporateDoctorCharge(CurrencyUtil.parseCurrency(corporateDoctorVisitCharge.getText()));
        return ward;
    }

    void setWard(Ward ward) {
        this.ward = ward;
        setWardDetails();
        numColumnsChoiceBox.setValue(ward.getBedsPerRow());
        getAdmissionData();
    }

    @FXML
    private void prepareBedChart() {
        flowPane.getChildren().clear();
        int beds = NumberUtil.stringToInt(numBeds.getText());
        if (beds == -1) {
            AlertUtil.showAlert("Invalid Value", "Invalid number of beds", Alert.AlertType.ERROR);
        } else {
            int numColumns = numColumnsChoiceBox.getValue();
            int hBoxWidth = 250;
            int hBoxHeight = 50;
            int flowPaneHorizontalSpacing = 20;
            int flowPanePrefWrapLen = hBoxWidth * numColumns + (numColumns - 1) * flowPaneHorizontalSpacing;
            flowPane.setPrefWrapLength(flowPanePrefWrapLen);

            for (int i = 0; i < beds; i++) {
                HBox hBox = new HBox(10);
                hBox.getStyleClass().add("bed");
                hBox.setPrefSize(hBoxWidth, hBoxHeight);

                HBox numHbox = new HBox();
                numHbox.setPrefSize(50, 50);
                numHbox.getStyleClass().add("bed_number");
                numHbox.getChildren().add(new Label(Integer.toString(i + 1)));

                hBox.getChildren().add(numHbox);
                Label label = new Label("Unoccupied");
                label.getStyleClass().add("");
                if (ward == null) {
                    label.setStyle("-fx-text-fill: #ff4444");
                } else {
                    String patientName = bedNumberPatientMap.get(i + 1);
                    if (patientName == null) {
                        label.setStyle("-fx-text-fill: #ff4444");
                    } else {
                        label.setText(patientName);
                    }
                }
                hBox.getChildren().add(label);
                flowPane.getChildren().add(hBox);
            }
        }
        //update info about ward
        Task task = new Task() {
            @Override
            protected Object call() {
                if (ward != null) {
                    String sql = "update wards set beds_per_row = " + numColumnsChoiceBox.getValue() + " where " +
                            "ward_id = " + ward.getId();
                    DBUtil.executeStatement(sql);
                }
                return null;
            }
        };
        task.setOnSucceeded(event -> {
            parent.getData();
        });
        new Thread(task).start();
    }

    private boolean validInput() {
        String errorMsg = "";
        if (wardName.getText() == null || wardName.getText().isEmpty()) {
            errorMsg += "Ward name is required.\n";
        } else if (parent.duplicateWard(wardName.getText(), ward)) {
            errorMsg += "Ward name is already taken!";
        }
        if (NumberUtil.stringToInt(numBeds.getText()) == -1) {
            errorMsg += "Invalid number of beds in ward.\n";
        }

        if (CurrencyUtil.parseCurrency(admissionCharge.getText()) == -1) {
            errorMsg += "Invalid admission charge.\n";
        }
        if (CurrencyUtil.parseCurrency(corporateAdmissionCharge.getText()) == -1) {
            errorMsg += "Invalid corporate admission charge.\n";
        }
        if (CurrencyUtil.parseCurrency(dailyRate.getText()) == -1) {
            errorMsg += "Invalid daily rate.\n";
        }
        if (CurrencyUtil.parseCurrency(corporateDailyRate.getText()) == -1) {
            errorMsg += "Invalid corporate daily rate.\n";
        }
        if (CurrencyUtil.parseCurrency(nurseVisitCharge.getText()) == -1) {
            errorMsg += "Invalid daily nurse visit charge.\n";
        }
        if (CurrencyUtil.parseCurrency(corporateNurseVisitCharge.getText()) == -1) {
            errorMsg += "Invalid corporate daily nurse visit charge.\n";
        }
        if (CurrencyUtil.parseCurrency(doctorVisitCharge.getText()) == -1) {
            errorMsg += "Invalid daily doctor visit charge.\n";
        }
        if (CurrencyUtil.parseCurrency(corporateDoctorVisitCharge.getText()) == -1) {
            errorMsg += "Invalid corporate daily doctor visit charge";
        }
        if (errorMsg.isEmpty()) {
            return true;
        }
        AlertUtil.showAlert("Input Errors", errorMsg, Alert.AlertType.ERROR);
        return false;
    }

    @FXML
    private void onSaveChanges() {
        if (validInput()) {
            if (DBUtil.saveWardInfo(getWard())) {
                AlertUtil.showAlert("Save Ward", "Ward details successfully saved!", Alert
                        .AlertType.INFORMATION);
                prepareBedChart();
                parent.getData();
            } else {
                AlertUtil.showAlert("Error", "An error occurred while attempting to save ward details", Alert.AlertType.ERROR);
            }
        }
    }

    WardManagementController getParent() {
        return parent;
    }

    void setParent(WardManagementController parent) {
        this.parent = parent;
    }
}