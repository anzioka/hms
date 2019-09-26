package main.java.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import main.java.dao.MedicineDAO;
import main.java.model.Bill;
import main.java.model.Formulation;
import main.java.model.Medicine;
import main.java.model.Prescription;
import main.java.util.*;
import org.controlsfx.control.textfield.AutoCompletionBinding;
import org.controlsfx.control.textfield.TextFields;

import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Created by alfonce on 05/07/2017.
 */
public class PrescriptionController {
    private static final String COST = "cost";
    private static final String QUANTITY = "quantity";
    private static final String NAME = "name";
    private static final String DOSAGE = "dosage";
    private static final String DURATION = "duration";
    private static final String DATE = "date";
    private static final String USER = "user";
    private static final String ID = "id";
    private static final String FORMULATION = "formulation";
    private static final String STATUS = "status";

    @FXML
    private ChoiceBox<Prescription.Dosage> dosageOptions;
    @FXML
    private ChoiceBox<Formulation> formulationChoiceBox;
    @FXML
    private TextField duration, quantity, medicineName;

    @FXML
    private TableView<Map<String, String>> prescriptionTableView;

    @FXML
    private TableColumn<Map<String, String>, String> cost, quantityCol, formulationCol, medicineNameCol, dosageCol, durationCol, options, date, user;

    private String insurer, insuranceId, patientId;
    private int visitId, admissionNum, billNumber, requestId;
    private Medicine currentMedicine = null;
    private boolean outpatientMode;

    @FXML
    public void initialize() {
        //set up table
        dosageOptions.setItems(FXCollections.observableArrayList(Prescription.Dosage.values()));
        dosageOptions.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            formulationChoiceBox.requestFocus();
        });
        quantity.textProperty().addListener((observable, oldValue, newValue) -> {
            duration.setDisable(newValue != null && !newValue.isEmpty());
        });
        duration.textProperty().addListener((observable, oldValue, newValue) -> {
            quantity.setDisable(newValue != null && !newValue.isEmpty());
        });
        formulationChoiceBox.setItems(FXCollections.observableArrayList(Formulation.values()));
        formulationChoiceBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            quantity.requestFocus();
        });
        setUpAutocomplete();
    }

    private void setUpAutocomplete() {
        Task<ObservableList<Medicine>> task = new Task<ObservableList<Medicine>>() {
            @Override
            protected ObservableList<Medicine> call() {
                return MedicineDAO.getMedicineList();
            }
        };
        task.setOnSucceeded(event -> {
            AutoCompletionBinding<Medicine> binding = TextFields.bindAutoCompletion(medicineName, task.getValue());
            binding.setOnAutoCompleted(e -> {
                currentMedicine = e.getCompletion();
                dosageOptions.requestFocus();
            });
        });
        new Thread(task).start();
    }

    private void setUpTable() {
        //place holder text
        Label label = new Label("No prescription has been added");
        label.getStyleClass().add("text-danger");
        prescriptionTableView.setPlaceholder(label);

        if (outpatientMode) {
            prescriptionTableView.getColumns().remove(user);
            prescriptionTableView.getColumns().remove(date);
            prescriptionTableView.getColumns().remove(formulationCol);
        }

        //col widths
        for (TableColumn column : prescriptionTableView.getColumns()) {
            if (column == medicineNameCol) {
                if (outpatientMode) {
                    column.prefWidthProperty().bind(prescriptionTableView.widthProperty().divide(3.5));
                } else {
                    column.prefWidthProperty().bind(prescriptionTableView.widthProperty().divide(5));
                }
            } else {
                if (outpatientMode) {
                    column.prefWidthProperty().bind(prescriptionTableView.widthProperty().divide(7));
                } else {
                    column.prefWidthProperty().bind(prescriptionTableView.widthProperty().divide(10));
                }
            }
        }
        formulationCol.setCellValueFactory(param -> StringUtil.getStringProperty(param.getValue().get(FORMULATION)));
        cost.setCellValueFactory(param -> StringUtil.getStringProperty(param.getValue().get(COST)));
        quantityCol.setCellValueFactory(param -> StringUtil.getStringProperty(param.getValue().get(QUANTITY)));
        medicineNameCol.setCellValueFactory(param -> StringUtil.getStringProperty(param.getValue().get(NAME)));
        dosageCol.setCellValueFactory(param -> StringUtil.getStringProperty(param.getValue().get(DOSAGE)));
        durationCol.setCellValueFactory(param -> StringUtil.getStringProperty(param.getValue().get(DURATION)));
        date.setCellValueFactory(param -> StringUtil.getStringProperty(param.getValue().get(DATE)));
        user.setCellValueFactory(param -> StringUtil.getStringProperty(param.getValue().get(USER)));
        options.setCellFactory(param -> new TableCell<Map<String, String>, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                int index = getIndex();
                if (index >= 0 && index < prescriptionTableView.getItems().size()) {
                    Button button = new Button("Delete");
                    button.getStyleClass().add("btn-danger-outline");
                    button.setOnAction(event -> {
                        removePrescription(prescriptionTableView.getItems().get(index));
                    });
                    setGraphic(button);
                } else {
                    setGraphic(null);
                }
                ;
            }
        });

    }

    private void removePrescription(Map<String, String> prescription) {
        if (prescription.get(STATUS).equals(Prescription.Status.COMPLETED.name())) {
            AlertUtil.showAlert("Delete Error", "Medicine already dispensed. Cannot delete", Alert.AlertType.ERROR);
            return;
        }
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Prescription");
        alert.setContentText("Are you sure you want to remove prescription?");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            boolean error = false;
            String sql = "DELETE FROM Prescriptions where Id = " + prescription.get(ID);
            if (DBUtil.executeStatement(sql)) {
                sql = "Delete from billing " +
                        "where bill_number = " + billNumber + " " +
                        "and amount = " + CurrencyUtil.parseCurrency(prescription.get(COST)) + " and category = '" + Bill.Category.MEDICATION + "' " +
                        "and description = '" + prescription.get(NAME) + " (x" + prescription.get(QUANTITY) + ")' limit 1";
                if (!DBUtil.executeStatement(sql)) {
                    error = true;
                }
            } else {
                error = true;
            }
            if (error) {
                AlertUtil.showAlert("Delete Error", "An error occurred while attempting to remove procedure", Alert.AlertType.ERROR);
            } else {
                AlertUtil.showAlert("Delete Prescription", "Prescription successfully removed and patient bill updated!", Alert.AlertType.INFORMATION);
                prescriptionTableView.getItems().remove(prescription);
            }
        }
    }

    private void clearFields() {
        dosageOptions.getSelectionModel().clearSelection();
        formulationChoiceBox.getSelectionModel().clearSelection();
        currentMedicine = null;
        medicineName.setText(null);
        quantity.setText(null);
        duration.setText(null);
        medicineName.requestFocus();
    }

    private boolean validInput() {
        String errorMsg = "";
        if (currentMedicine == null) {
            errorMsg += "Please specify medicine!\n";
        }
        if (dosageOptions.getSelectionModel().getSelectedItem() == null) {
            errorMsg += "Please specify dosage!\n";
        }
        if (formulationChoiceBox.getValue() == null) {
            errorMsg += "Please specify formulation!\n";
        }
        int quantityReq = -1, days = -1;
        if (!quantity.isDisabled()) {
            quantityReq = NumberUtil.stringToInt(quantity.getText());
        } else if (!duration.isDisabled()) {
            days = NumberUtil.stringToInt(duration.getText());
        }
        if (quantityReq == -1 && days == -1) {
            errorMsg += "Please specify EITHER the number of days OR the quantity to dispense)\n";
        }

        if (errorMsg.isEmpty()) {
            return true;
        }
        AlertUtil.showAlert("Input Error(s)", errorMsg, Alert.AlertType.ERROR);
        return false;
    }

    @FXML
    private void onSubmitPrescriptionRequest() {
        Task<Integer> task = new Task<Integer>() {
            @Override
            protected Integer call() throws Exception {
                return 1 + Math.max(DBUtil.getNextAutoIncrementId("prescriptions"), DBUtil.getNextAutoIncrementId("billing"));
            }
        };
        task.setOnSucceeded(event -> {
            if (validInput()) {
                Prescription prescription = createPrescription(task.getValue());
                if (prescription != null) {
                    if ((DBUtil.savePrescription(prescription)) && DBUtil.saveBill(createBill(prescription.getQuantity(), task.getValue()))) {
                        AlertUtil.showAlert("Prescription Request", "Prescription request has been successfully submitted", Alert.AlertType.INFORMATION);
                        clearFields();
                        getPatientPrescriptions();
                    } else {
                        AlertUtil.showAlert("Prescription Request", "An error occurred while attempting to send prescription request.", Alert.AlertType.ERROR);

                    }
                }
            }
        });
        new Thread(task).start();

    }

    private Bill createBill(int quantity, int requestId) {
        Bill bill = new Bill();
        bill.setId(requestId);
        bill.setDescription(currentMedicine.getName() + " (x" + quantity + ")");
        bill.setAmount(quantity * currentMedicine.getSellingPrice());
        bill.setBillNumber(billNumber);
        bill.setInsurer(insurer);
        bill.setInsuranceId(insuranceId);
        bill.setCategory(Bill.Category.MEDICATION);
        bill.setPatientNumber(patientId);

        if (outpatientMode) {
            bill.setQueueNumber(visitId);
        } else {
            bill.setAdmissionNumber(admissionNum);
        }
        bill.setDateCreated(LocalDate.now());
        return bill;
    }

    private Prescription createPrescription(int requestId) {
        Prescription prescription = new Prescription();
        prescription.setId(requestId);
        prescription.setDosage(dosageOptions.getValue());
        prescription.setDateCreated(LocalDate.now());
        prescription.setTimeCreated(LocalTime.now());
        prescription.setDrugId(currentMedicine.getDrugCode());
        prescription.setFormulation(formulationChoiceBox.getValue());
        if (!quantity.isDisabled()) {
            int quantityReq = NumberUtil.stringToInt(quantity.getText());
            prescription.setQuantity(quantityReq);
            prescription.setDurationFromDosage();

        } else if (!duration.isDisabled()) {
            int numDays = NumberUtil.stringToInt(duration.getText());
            prescription.setDuration(numDays);
            prescription.setQuantityFromDosage();
        }
        if (prescription.getQuantity() > currentMedicine.getShopQuantity()) {
            AlertUtil.showAlert("Invalid Quantity", "The quantity of '" + currentMedicine.getName() + "' available for sale is only " + currentMedicine.getShopQuantity(), Alert.AlertType.ERROR);
            return null;
        }

        if (outpatientMode) {
            prescription.setVisitId(visitId);
        } else {
            prescription.setAdmissionNum(admissionNum);
        }
        return prescription;
    }

    void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    void setVisitId(int visitId) {
        this.visitId = visitId;
        getPatientPrescriptions();
        getPaymentDetails();
    }

    private void getPatientPrescriptions() {
        Task<ObservableList<Map<String, String>>> task = new Task<ObservableList<Map<String, String>>>() {
            @Override
            protected ObservableList<Map<String, String>> call() throws Exception {
                ObservableList<Map<String, String>> list = FXCollections.observableArrayList();
                String sql = "select prescriptions.Id, status, formulation, quantity, duration, dosage, prescriptions.date_created, users.FirstName, Users.LastName, Drugs.Name, Drugs.SellingPrice " +
                        "from prescriptions " +
                        "inner join drugs on drugs.DrugCode = prescriptions.drug_code " +
                        "inner join users on users.Id = prescriptions.user_id ";

                if (outpatientMode) {
                    sql += " where visit_id = " + visitId;
                } else {
                    sql += " where admission_num = " + admissionNum;
                }
                ResultSet resultSet = DBUtil.executeQuery(sql);
                if (resultSet != null) {
                    while (resultSet.next()) {
                        Map<String, String> map = new HashMap<>();
                        map.put(ID, resultSet.getString("id"));
                        map.put(STATUS, Prescription.Status.valueOf(resultSet.getString("status")).name());
                        map.put(QUANTITY, resultSet.getString("quantity"));
                        map.put(DURATION, resultSet.getString("duration"));
                        map.put(DOSAGE, Prescription.Dosage.valueOf(resultSet.getString("dosage")).toString());
                        map.put(DATE, DateUtil.formatDate(resultSet.getObject("date_created", LocalDate.class)));
                        map.put(USER, resultSet.getString("FirstName") + " " + resultSet.getString("LastName"));
                        map.put(COST, CurrencyUtil.formatCurrency(resultSet.getDouble("SellingPrice") * Integer.parseInt(map.get(QUANTITY))));
                        if (resultSet.getString("formulation") != null) {
                            map.put(FORMULATION, Formulation.valueOf(resultSet.getString("formulation")).toString());
                        } else {
                            map.put(FORMULATION, null);
                        }
                        map.put(NAME, resultSet.getString("Name"));
                        list.add(map);
                    }
                }
                return list;
            }
        };
        task.setOnSucceeded(event -> {
            prescriptionTableView.setItems(task.getValue());
        });
        new Thread(task).start();
    }

    private void getPaymentDetails() {
        Task task = new Task() {
            @Override
            protected Object call() throws Exception {
                String sql = "select InsuranceProvider, InsuranceId, Queues.bill_number from Queues " +
                        "inner join Patients on patients.PatientId = queues.PatientId " +
                        "where visitId = " + visitId;
                if (!outpatientMode) {
                    sql = "select InsuranceProvider, InsuranceId, inpatients.bill_number from inpatients " +
                            "inner join Patients on patients.patientId = inpatients.patient_id " +
                            "where admission_num = " + admissionNum;
                }
                ResultSet resultSet = DBUtil.executeQuery(sql);
                if (resultSet != null && resultSet.next()) {
                    insuranceId = resultSet.getString("InsuranceId");
                    billNumber = resultSet.getInt("bill_number");
                    insurer = resultSet.getString("insuranceProvider");
                }
                return null;
            }
        };
        new Thread(task).start();
    }

    void setOutpatientMode() {
        this.outpatientMode = true;
        setUpTable();
    }

    public void setAdmissionNumber(int admissionNumber) {
        this.admissionNum = admissionNumber;
        setUpTable();
        getPatientPrescriptions();
        getPaymentDetails();
    }
}
