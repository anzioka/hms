package main.java.controller;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import main.java.dao.PatientDAO;
import main.java.model.Inpatient;
import main.java.model.InpatientVisit;
import main.java.model.Patient;
import main.java.model.PatientCategory;
import main.java.util.AgeUtil;
import main.java.util.DateUtil;

import java.io.IOException;

public class ViewInpatientController {
    public static Inpatient patient;
    @FXML
    private VBox container;
    @FXML
    private Label inpatientNo, sex, firstName, lastName, dateOfBirth, age;
    @FXML
    private TabPane tabPane;
    @FXML
    private FlowPane controlPanel;
    @FXML
    private Button admissionHistory, allergies, clinicalSummary, diagnosis, dischargePatient,
            dischargeSummary, doctorVisits, interimBill, otherCharges, nurseVisits, operationNotes,
            procedures, radiologyRequests, treatmentChart, labRequests, vitalsHistory, admissionDetails, NHIFRebate;
    private InpatientManagementController context;

    @FXML
    private void initialize() {
        for (Node node : controlPanel.getChildren()) {
            node.setOnMouseClicked(event -> {
                handleClick((Button) event.getSource());
            });
        }
    }

    private void handleClick(Button button) {

        if (button == NHIFRebate) {
            showModalWindow("nhif-rebate", null);
        } else if (button == dischargeSummary) {
            new PrintDischargeSummary().showSummary(patient.getAdmissionNumber(), patient.getInpatientNumber());
        } else if (button == interimBill) {
            showModalWindow("inpatient_bill", ControlPanelItem.BILL);
        } else if (button == admissionDetails) {
            showModalWindow("view-admission-details", null);
        } else if (button == dischargePatient) {
            showModalWindow("discharge-patient", ControlPanelItem.DISCHARGE_PATIENT);
        } else if (button == clinicalSummary) {
            showModalWindow("inpatient_clinical_summary", null);
        } else if (button == diagnosis) {
            showModalWindow("diagnosis", ControlPanelItem.DIAGNOSIS);
        } else if (button == allergies) {
            showModalWindow("allergies", ControlPanelItem.ALLERGIES);
        } else if (button == procedures) {
            showModalWindow("patient_procedures", ControlPanelItem.PROCEDURES);
        } else if (button == radiologyRequests) {
            showModalWindow("create_radiology_request", ControlPanelItem.RADIOLOGY_REQUESTS);
        } else if (button == admissionHistory){
            showPatientHistory();
        } else {
            loadTab(button);
        }
    }
    @FXML
    private void showPatientHistory() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("main/resources/view/medical_records.fxml"));
            Stage stage = new Stage();
            stage.setScene(new Scene(loader.load()));
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(container.getScene().getWindow());

            MedicalRecordsController controller = loader.getController();
            controller.setPatientId(patient.getPatientId());
            stage.showAndWait();
            stage.setResizable(false);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showModalWindow(String resourceFileName, ControlPanelItem panelItem) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("main/resources/view/" + resourceFileName + ".fxml"));
            Stage stage = new Stage();
            stage.setScene(new Scene(loader.load()));
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(container.getScene().getWindow());

            if (panelItem == ControlPanelItem.PROCEDURES) {
                PatientProceduresController controller = loader.getController();
                controller.setAdmissionNum(patient.getAdmissionNumber());
            } else if (panelItem == ControlPanelItem.BILL) {
                InpatientBillController controller = loader.getController();
                controller.setAdmissionNumber(patient.getAdmissionNumber());
            } else if (panelItem == ControlPanelItem.DISCHARGE_PATIENT) {
                DischargePatientController controller = loader.getController();
                controller.setContext(this);
                controller.setStage(stage);
            } else if (panelItem == ControlPanelItem.DIAGNOSIS) {
                DiagnosisController controller = loader.getController();
                controller.setParameters(patient.getAdmissionNumber(), PatientCategory.INPATIENT);
            } else if (panelItem == ControlPanelItem.ALLERGIES) {
                AllergiesController controller = loader.getController();
                controller.setPatientId(patient.getPatientId());
                stage.setResizable(false);
            } else if (panelItem == ControlPanelItem.RADIOLOGY_REQUESTS) {
                CreateRadiologyRequestController controller = loader.getController();
                controller.setPatientInfo(patient.getAdmissionNumber(), patient.getPatientId(), false);
            }
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadTab(Button button) {
        ControlPanelItem controlPanelItem = null;
        if (button == admissionHistory) {
            controlPanelItem = ControlPanelItem.ADMISSION_HISTORY;
        } else if (button == allergies) {
            controlPanelItem = ControlPanelItem.ALLERGIES;
        } else if (button == clinicalSummary) {
            controlPanelItem = ControlPanelItem.CLINICAL_SUMMARY;
        } else if (button == diagnosis) {
            controlPanelItem = ControlPanelItem.DIAGNOSIS;
        } else if (button == doctorVisits) {
            controlPanelItem = ControlPanelItem.DOCTOR_VISIT;
        } else if (button == otherCharges) {
            controlPanelItem = ControlPanelItem.MISCELLANEOUS_CHARGES;
        } else if (button == nurseVisits) {
            controlPanelItem = ControlPanelItem.NURSE_VISITS;
        } else if (button == operationNotes) {
            controlPanelItem = ControlPanelItem.OPERATION_NOTES;
        } else if (button == treatmentChart) {
            controlPanelItem = ControlPanelItem.TREATMENT_CHART;
        } else if (button == labRequests) {
            controlPanelItem = ControlPanelItem.LAB_REQUESTS;
        } else if (button == vitalsHistory) {
            controlPanelItem = ControlPanelItem.VITALS_HISTORY;
        }

        if (controlPanelItem != null) {
            loadTabHelper(controlPanelItem.getResourceFile(), button);
        }

    }

    private void loadTabHelper(String resourceFile, Button button) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("main/resources/view/" +
                    resourceFile + ".fxml"));
            Node node = loader.load();

            if (button == labRequests) {
                LabRequestController controller = loader.getController();
                controller.setParameters(patient.getAdmissionNumber(), patient.getPatientId(), true);
            }
            if (button == treatmentChart) {
                PrescriptionController controller = loader.getController();
                controller.setPatientId(patient.getPatientId());
                controller.setAdmissionNumber(patient.getAdmissionNumber());
            }

            if (button == doctorVisits) {
                InpatientDailyVisitsController controller = loader.getController();
                controller.setVisitCategory(InpatientVisit.Category.DOCTOR);
            }
            if (button == nurseVisits) {
                InpatientDailyVisitsController controller = loader.getController();
                controller.setVisitCategory(InpatientVisit.Category.NURSE);
            }

            if (tabPane.getTabs().size() == 2) {
                tabPane.getTabs().remove(1);
            }
            Tab tab = new Tab(button.getText());
            tab.setContent(node);
            tab.setClosable(true);
            tabPane.getTabs().add(tab);
            tabPane.getSelectionModel().select(tab);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void dischargeCurrentPatient() {

    }

    private void getPatientDetails() {
        Task<Patient> task = new Task<Patient>() {
            @Override
            protected Patient call() {
                String sql = "select * from patients " +
                        "inner join inpatients on patients.PatientId = inpatients.patient_id " +
                        "where admission_num = " + patient.getAdmissionNumber();
                return PatientDAO.getPatient(sql);
            }
        };
        task.setOnSucceeded(event -> {
            setPatientDetails(task.getValue());
        });
        new Thread(task).start();
    }

    private void setPatientDetails(Patient patient) {
        sex.setText(patient.getSexuality());
        age.setText(AgeUtil.getAge(patient.getDateOfBirth()));
        firstName.setText(patient.getFirstName());
        lastName.setText(patient.getLastName());
        dateOfBirth.setText(DateUtil.formatDateLong(patient.getDateOfBirth()));
    }

    Inpatient getPatient() {
        return patient;
    }

    void setPatient(Inpatient patient) {
        ViewInpatientController.patient = patient;
        inpatientNo.setText(patient.getInpatientNumber());
        getPatientDetails();
    }

    void removePatient() {
        context.closeTab();
        context.onRefreshData();
    }

    public InpatientManagementController getContext() {
        return context;
    }

    public void setContext(InpatientManagementController context) {
        this.context = context;
    }

    private enum ControlPanelItem {
        DISCHARGE_PATIENT("discharge"),
        DISCHARGE_SUMMARY("discharge_summary"),
        ALLERGIES("inpatient_allergies"),
        CLINICAL_SUMMARY("inpatient_clinical_summary"),
        DIAGNOSIS("inpatient_diagnosis"),
        DOCTOR_VISIT("inpatient_visits"),
        INTERIM_BILL("inpatient_interim_bill"),
        MISCELLANEOUS_CHARGES("inpatient_other_charges"),
        NURSE_VISITS("inpatient_visits"),
        OPERATION_NOTES("inpatient_operation"),
        PRESCRIPTION_REQUESTS("inpatient_medication"),
        RADIOLOGY_REQUESTS("inpatient_radiology"),
        TREATMENT_CHART("prescription"),
        LAB_REQUESTS("lab_requests"),
        VITALS_HISTORY("vitals_history"),
        PROCEDURES("procedures"),
        BILL("bill"),
        ADMISSION_HISTORY("inpatient_admission_history");

        private String resourceFile;

        ControlPanelItem(String resourceFile) {
            this.resourceFile = resourceFile;
        }

        private String getResourceFile() {
            return this.resourceFile;
        }
    }
}
