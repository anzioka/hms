package main.java.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import main.java.dao.BillingDao;
import main.java.dao.PatientDAO;
import main.java.model.*;
import main.java.util.*;
import org.controlsfx.control.textfield.AutoCompletionBinding;
import org.controlsfx.control.textfield.TextFields;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

/**
 * Created by alfonce on 23/04/2017.
 */
public class EditPatientController {

    @FXML
    private ChoiceBox<PaymentMode> paymentMode;

    @FXML
    private ChoiceBox<MaritalStatus> maritalStatus;

    @FXML
    private ChoiceBox<FamilyRelation> nextOfKinRelationship;

    @FXML
    private ChoiceBox<SearchParam> searchParamChoiceBox;

    @FXML
    private ChoiceBox<String> insuranceChoiceBox;
    @FXML
    private Label patientNumberLabel;
    @FXML
    private GridPane insuranceInfo;

    @FXML
    private VBox container;
    @FXML
    private TextField searchParamInput, patientNumber, firstName, surname, age, NHIFNumber,
            insuranceId, phoneNumber, nextOfKinFirstName, nextOfKinSurname, nextOfKinPhoneNumber,
            residence;
    @FXML
    private Button admitButton, queueButton, appointmentBtn;
    @FXML
    private RadioButton male, female;
    @FXML
    private DatePicker dateOfBirth;

    private ObservableList<Patient> patientObservableList;
    private ObservableList<String> existingPatientNumbers;
    private ToggleGroup patientSexToggleGroup;
    private AutoCompletionBinding<Patient> binding;
    private TextField[] textFields;
    private String sex = null;
    private Patient patient = null;
    private boolean inpatientMode;
    private InpatientManagementController context;

    @FXML
    public void initialize() {

        textFields = new TextField[]{firstName, surname, patientNumber, age, NHIFNumber,
                insuranceId, phoneNumber, nextOfKinFirstName, nextOfKinSurname,
                nextOfKinPhoneNumber};
        //male/female choice
        configureSexOptions();
        configurePaymentOptions();
        getPatients();
        configureInsurance();

        //next of kin
        nextOfKinRelationship.setItems(FXCollections.observableArrayList(FamilyRelation.values()));
        //marital status
        maritalStatus.setItems(FXCollections.observableArrayList(MaritalStatus.values()));

        //date picker

        dateOfBirth.setConverter(DateUtil.getDatePickerConverter());
        dateOfBirth.setPromptText("dd-mm-yyyy");
        dateOfBirth.setOnAction(event -> {
            LocalDate date = dateOfBirth.getValue();
            if (date != null) {
                if (date.isBefore(LocalDate.now())) {
                    age.setText(AgeUtil.getAge(date));
                }
            }
        });
        toggleButtonsVisibility(false);
    }

    private void configureInsurance() {
        Task<ObservableList<String>> task = new Task<ObservableList<String>>() {
            @Override
            protected ObservableList<String> call() throws SQLException {
                ObservableList<String> list = FXCollections.observableArrayList();
                ResultSet resultSet = DBUtil.executeQuery("select name from insurance");
                if (resultSet != null) {
                    while (resultSet.next()) {
                        list.add(resultSet.getString("name"));
                    }
                }
                return list;

            };
        };
        task.setOnSucceeded(event -> {
            insuranceChoiceBox.setItems(task.getValue());
        });
        new Thread(task).start();
    }

    private void configurePaymentOptions() {
        //options for payment mode
        paymentMode.setItems(FXCollections.observableArrayList(PaymentMode.values()));
        paymentMode.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            insuranceInfo.setManaged(newValue == PaymentMode.INSURANCE);
            insuranceInfo.setVisible(newValue == PaymentMode.INSURANCE);
        });
        paymentMode.getSelectionModel().select(PaymentMode.CASH); //default cash
    }

    private void configureSexOptions() {
        patientSexToggleGroup = new ToggleGroup();
        patientSexToggleGroup.getToggles().addAll(male, female);
        patientSexToggleGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            RadioButton selected = (RadioButton) newValue;
            if (selected != null) {
                sex = selected.getText();
            }
        });
    }

    @FXML
    private void onSearchPatientRecord() {
        SearchParam searchParam = searchParamChoiceBox.getValue();
        if (searchParam == SearchParam.FIRST_NAME || searchParam == SearchParam.LAST_NAME) {
            return;
        }
        String searchString = searchParamInput.getText();
        Patient patient = null;
        if (searchParam == SearchParam.OUTPATIENT_NUM) {
            patient = PatientDAO.getPatient("select * from patients where PatientId = '" + searchString + "'");

        } else if (searchParam == SearchParam.INPATIENT_NUM) {
            patient = PatientDAO.getPatient("select patients.* from patients " +
                    "inner join inpatients on inpatients.patient_id = patients.PatientId " +
                    "where inpatient_num = '" + searchString + "'");
        } else if (searchParam == SearchParam.PHONE_NUMBER) {
            patient = PatientDAO.getPatient("select * from patients where PhoneNumber = '" + searchString + "'");
        }

        if (patient != null) {
            this.patient = patient;
            toggleButtonsVisibility(true);
            patientNumber.setEditable(false);
            showPatientDetails();
        } else {
            AlertUtil.showAlert("Patient Search", "No patient record found", Alert.AlertType.ERROR);
        }

    }

    private void getPatients() {
        patientObservableList = FXCollections.observableArrayList();
        existingPatientNumbers = FXCollections.observableArrayList();
        Task<ObservableList<Patient>> task = new Task<ObservableList<Patient>>() {
            @Override
            protected ObservableList<Patient> call() {
                String sql = "select PatientId, FirstName, LastName from Patients";
                ResultSet resultSet = DBUtil.executeQuery(sql);
                if (resultSet != null) {
                    try {
                        while (resultSet.next()) {
                            Patient patient = new Patient();
                            patient.setFirstName(resultSet.getString("FirstName"));
                            patient.setLastName(resultSet.getString("LastName"));
                            patient.setPatientId(resultSet.getString("PatientId"));
                            patientObservableList.add(patient);
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
                sql = "select distinct patientId from patients union  select inpatient_num from inpatients order by " +
                        "patientId";
                resultSet = DBUtil.executeQuery(sql);
                if (resultSet != null) {
                    try {
                        while (resultSet.next()) {
                            existingPatientNumbers.add(resultSet.getString("PatientId"));
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
                return null;
            }
        };
        task.setOnSucceeded(event -> {
            configureSearch();
            setNextPatientNumber();
        });
        new Thread(task).start();
    }

    private void configureSearch() {

        searchParamChoiceBox.setItems(FXCollections.observableArrayList(SearchParam.values()));
        searchParamChoiceBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue)
                -> {
            searchParamInput.requestFocus();
            if (binding != null) {
                binding.dispose();
            }
            if (newValue == SearchParam.FIRST_NAME || newValue == SearchParam.LAST_NAME) {
                binding = TextFields.bindAutoCompletion(searchParamInput, patientObservableList);
                binding.setOnAutoCompleted(autoCompletionEvent -> {
                    patient = autoCompletionEvent.getCompletion();
                    if (patient != null) {
                        patientNumber.setEditable(false);
                        getPatientDetails();

                        toggleButtonsVisibility(true);
                    }
                });

            }
        });
        searchParamChoiceBox.setValue(SearchParam.FIRST_NAME);
    }

    private void getPatientDetails() {
        Task<Patient> task = new Task<Patient>() {
            @Override
            protected Patient call() {
                String sql = "select * from patients where patientId = '" + patient.getPatientId() + "'";
                return PatientDAO.getPatient(sql);
            }
        };
        task.setOnSucceeded(event -> {
            this.patient = task.getValue();
            showPatientDetails();
        });
        new Thread(task).start();
    }

    private void toggleButtonsVisibility(boolean visible) {
        if (!visible) {
            for (Button button : new Button[]{queueButton, admitButton, appointmentBtn}) {
                button.setManaged(false);
                button.setVisible(false);
            }
        } else {
            if (getInpatientMode()) {
                admitButton.setVisible(true);
                admitButton.setManaged(true);
            } else {
                queueButton.setVisible(true);
                queueButton.setManaged(true);
            }
            appointmentBtn.setVisible(true);
            appointmentBtn.setManaged(true);
        }
    }

    private void setNextPatientNumber() {
        for (int i = existingPatientNumbers.size() - 1; i > -1; i--) {
            String currentId = existingPatientNumbers.get(i);
            if (NumberUtil.stringToInt(currentId) != -1) {
                patientNumber.setText(Integer.toString(NumberUtil.stringToInt(currentId) + 1));
                return;
            }
        }
        patientNumber.setText("1001");
    }

    private void resetForm() {
        for (TextField textField : textFields) {
            textField.setText(null);
        }
        searchParamInput.setText(null);
        residence.setText(null);
        patientSexToggleGroup.selectToggle(null);
        dateOfBirth.setValue(null);
        maritalStatus.setValue(null);
        nextOfKinRelationship.setValue(null);
        insuranceChoiceBox.getSelectionModel().clearSelection();
        paymentMode.getSelectionModel().select(PaymentMode.CASH);
    }

    private void showPatientDetails() {
        switch (patient.getSexuality()) {
            case "Male":
                patientSexToggleGroup.selectToggle(male);
                break;
            case "Female":
                patientSexToggleGroup.selectToggle(female);
        }
        if (getInpatientMode()) {
            String inpatientNo = getInpatientNumber();
            if (inpatientNo != null) {
                patientNumber.setText(inpatientNo);
                patientNumber.setEditable(false);
            } else {
                setNextPatientNumber();
                patientNumber.setEditable(true);
            }
        } else {
            patientNumber.setText(patient.getPatientId());
        }
        firstName.setText(patient.getFirstName());
        surname.setText(patient.getLastName());
        dateOfBirth.setValue(patient.getDateOfBirth());
        age.setText(patient.getPatientAge());
        maritalStatus.setValue(patient.getMaritalStatus());
        NHIFNumber.setText(patient.getNHIFNumber());
        phoneNumber.setText(patient.getTelephoneNumber());
        residence.setText(patient.getResidence());
        nextOfKinFirstName.setText(patient.getContactFirstName());
        nextOfKinSurname.setText(patient.getContactLastName());
        nextOfKinPhoneNumber.setText(patient.getContactTelephone());
        nextOfKinRelationship.setValue(patient.getContactRelationship());
        insuranceChoiceBox.setValue(patient.getInsurer());
        insuranceId.setText(patient.getInsuranceID());
    }

    private String getInpatientNumber() {
        String sql = "select inpatient_num from inpatients where patient_id ='" + patient.getPatientId() + "'";
        ResultSet resultSet = DBUtil.executeQuery(sql);
        try {
            if (resultSet != null && resultSet.next()) {
                return resultSet.getString("inpatient_num");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @FXML
    private void onClear() {
        resetForm();
        toggleButtonsVisibility(false);
        patient = null;
        setNextPatientNumber();
        patientNumber.setEditable(true);
    }

    @FXML
    private void onRegisterPatient() {
        if (registrationSuccessful()) {
            AlertUtil.showAlert("Registration", "Patient details have been successfully saved!", Alert.AlertType.INFORMATION);
            toggleButtonsVisibility(true);
        } else {
            AlertUtil.showGenericError();
        }

    }

    private boolean registrationSuccessful() {
        if (isValidInput()) {
            setPatientDetails();
            return DBUtil.addPatient(patient);
        }
        return false;
    }

    @FXML
    private void onCreateAppointment() {
        if (registrationSuccessful()) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("main/resources/view/select-doctor.fxml"));
                Stage stage = new Stage();
                stage.setScene(new Scene(loader.load()));
                stage.initOwner(container.getScene().getWindow());
                stage.initModality(Modality.WINDOW_MODAL);
                stage.setResizable(false);
                SelectDoctorController controller = loader.getController();
                controller.setPatientId(patient.getPatientId());
                controller.setStage(stage);
                controller.setContainer(container);
                stage.showAndWait();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void addPatientToQueue() {
        if (registrationSuccessful()) {
            if (patientAlreadyQueued()) {
                AlertUtil.showAlert("", "Patient has already been queued!", Alert.AlertType.ERROR);
            } else {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource
                            ("main/resources/view/queue-patient.fxml"));
                    Stage stage = new Stage();
                    stage.setScene(new Scene(loader.load()));
                    stage.setTitle("Queue Patient");
                    stage.setResizable(false);
                    stage.initOwner(container.getScene().getWindow());

                    QueuePatientController controller = loader.getController();
                    controller.setStage(stage);
                    controller.setParameters(patient, paymentMode.getValue());

                    container.setDisable(true);
                    stage.showAndWait();
                    container.setDisable(false);
                    if (controller.isQueuingSuccessful()) {
                        getPatients();
                        onClear();
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @FXML
    private void admitPatient() {
        if (registrationSuccessful()) {
            if (patientAlreadyAdmitted()) {
                AlertUtil.showAlert("", "Patient has already  been admitted!", Alert.AlertType.ERROR);
            } else {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource
                            ("main/resources/view/admit-patient.fxml"));

                    Stage stage = new Stage();
                    stage.setScene(new Scene(loader.load()));
                    stage.setTitle("Admit Patient");
                    stage.initOwner(container.getScene().getWindow());

                    Inpatient inpatient = new Inpatient();
                    inpatient.setInpatientNumber(patientNumber.getText());
                    inpatient.setPatientId(patient.getPatientId());

                    AdmitPatientController controller = loader.getController();
                    controller.setStage(stage);
                    controller.setParameters(patient, inpatient, paymentMode.getValue(), BillingDao.getNextBillNumber());
                    container.setDisable(true);
                    stage.showAndWait();
                    container.setDisable(false);
                    if (controller.isAdmissionSuccessful()) {
                        if (context != null) {
                            context.onRefreshData();
                            context.closeTab();
                        }
                        getPatients();
                        onClear();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private boolean duplicateInpatientNumber() {
        String sql = "select inpatient_num from inpatients where inpatient_num = '" + patientNumber.getText() + "'";
        ResultSet resultSet = DBUtil.executeQuery(sql);
        try {
            return (resultSet != null && resultSet.next());
        } catch (SQLException e) {
            e.printStackTrace();

        }
        return false;

    }

    private void setPatientDetails() {
        boolean newPatient = false;
        if (patient == null) {
            newPatient = true;
            patient = new Patient();
        }

        if (newPatient) {
            patient.setPatientId(patientNumber.getText());
        }
        if (insuranceChoiceBox.getSelectionModel().getSelectedItem() != null) {
            patient.setInsurer(insuranceChoiceBox.getSelectionModel().getSelectedItem().toString());
        }
        patient.setInsuranceID(insuranceId.getText());

        patient.setSexuality(sex);
        patient.setFirstName(firstName.getText());
        patient.setLastName(surname.getText());
        patient.setMaritalStatus(maritalStatus.getSelectionModel().getSelectedItem());
        patient.setResidence(residence.getText());
        patient.setContactFirstName(nextOfKinFirstName.getText());
        patient.setContactLastName(nextOfKinSurname.getText());
        patient.setContactTelephone(nextOfKinPhoneNumber.getText());
        patient.setContactRelationship(nextOfKinRelationship.getSelectionModel().getSelectedItem());
        patient.setDateOfBirth(dateOfBirth.getValue());
        patient.setNHIFNumber(NHIFNumber.getText());
        patient.setTelephoneNumber(phoneNumber.getText());
        patient.setDateCreated(LocalDate.now());

    }

    //TODO : perhaps add more required fields. will consult
    private boolean isValidInput() {
        String errorMsg = "";

        if (patientNumber.getText() == null || patientNumber.getText().isEmpty()) {
            errorMsg += "Patient number required!\n";

        } else if (patient == null && patientNumberAssigned() && !getInpatientMode()) {
            errorMsg += "The Patient number has already been used!\n";
        } else if (patient == null && duplicateInpatientNumber() && getInpatientMode()) {
            errorMsg += "Inpatient number has already been used!\n";
        }

        if (firstName.getText() == null || firstName.getText().isEmpty()) {
            errorMsg += "Patient's first name required!\n";
        }

        if (surname.getText() == null || surname.getText().isEmpty()) {
            errorMsg += "Patient's surname required!\n";
        }

        if (dateOfBirth.getValue() == null) {
            errorMsg += "Patient's date of birth required!\n";
        }

        if (sex == null) {
            errorMsg += "Patient's sex required!\n";
        }

        if (maritalStatus.getSelectionModel().getSelectedItem() == null) {
            errorMsg += "Patient's marital status required!\n";
        }

        if (residence.getText() == null || residence.getText().isEmpty()) {
            errorMsg += "Residence is required!\n";
        }

        if (paymentMode.getSelectionModel().getSelectedItem() == PaymentMode.INSURANCE) {
            if (insuranceChoiceBox.getSelectionModel().getSelectedItem() == null) {
                errorMsg += "Insurance provider required!\n";
            }
            if (insuranceId.getText() == null || insuranceId.getText().isEmpty()) {
                errorMsg += "Insurance membership number is required!\n";
            }
        }

        if (errorMsg.isEmpty()) {
            return true;
        }

        AlertUtil.showAlert("Input Errors", errorMsg, Alert.AlertType.ERROR);
        return false;
    }

    private boolean patientAlreadyQueued() {
        String sql = "select patientId from queues where patientId = '" + patientNumber.getText() + "' " +
                "and status != '" + PatientQueue.Status.DISCHARGED + "'";
        try {
            ResultSet resultSet = DBUtil.executeQuery(sql);
            return (resultSet != null && resultSet.next());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean patientAlreadyAdmitted() {
        String sql = "select inpatient_num from inpatients where inpatient_num = '" + patientNumber.getText() + "' " +
                "and status ='" + Inpatient.Status.ADMITTED + "'";
        try {
            ResultSet resultSet = DBUtil.executeQuery(sql);
            return (resultSet != null && resultSet.next());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean patientNumberAssigned() {
        String sql = "select PatientId from patients where patientId = '" + patientNumber.getText() + "'";
        try {
            ResultSet resultSet = DBUtil.executeQuery(sql);
            return (resultSet != null && resultSet.next());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    void setInpatientMode() {
        this.inpatientMode = true;
        patientNumberLabel.setText("Inpatient No.");
    }

    private boolean getInpatientMode() {
        return inpatientMode;
    }

    InpatientManagementController getContext() {
        return context;
    }

    void setContext(InpatientManagementController context) {
        this.context = context;
    }

    private enum SearchParam {
        INPATIENT_NUM("Inpatient Number"),
        OUTPATIENT_NUM("Outpatient Number"),
        FIRST_NAME("First Name"),
        LAST_NAME("Last Name"),
        PHONE_NUMBER("Phone Number");

        private String param;

        SearchParam(String param) {
            this.param = param;
        }

        @Override
        public String toString() {
            return this.param;
        }
    }
}

