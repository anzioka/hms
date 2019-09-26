package main.java.controller;

import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import main.Main;
import main.java.dao.HospitalDAO;
import main.java.model.Hospital;
import main.java.model.UserModule;
import main.java.util.DateUtil;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class MainController {
    private static final String APP_VERSION = "1.0.1";
    @FXML
    private MenuButton userButton;
    @FXML
    private MenuItem logoutButton;
    @FXML
    private Label logInTimeLabel, version;
    @FXML
    private VBox sideNavContainer;
    @FXML
    private HBox patientRegistration, nursingDepartment, doctorModule, homeScreen, reports,
            labDepartment, administrative, pharmacyDepartment, inventory, patientRecords, bedManagement,
            inpatientManagement, billing, radiology, accounting;
    //header
    @FXML
    private Label hospitalName;
    @FXML
    private Label hospitalAddress;
    private HBox selectedSideNavItem;
    private Main mainApp;

    @FXML
    public void initialize() {

        for (Node navHeader : sideNavContainer.getChildren()) {
            navHeader.setOnMouseClicked(this::handleClick);
        }
        showEnabledModules();

        logoutButton.setOnAction(event -> {
            mainApp.logOutUser();
        });
        version.setText("Version " + APP_VERSION);
        logInTimeLabel.setText("Logged in since " + DateUtil.getTimeFromLocalDateTime(LocalDateTime.now()));
    }

    private void showEnabledModules() {
//        clear everything first
        for (Node hBox : sideNavContainer.getChildren()) {
            hBox.setVisible(false);
            hBox.setManaged(false);
        }

        List<HBox> modules = new ArrayList<>();
        modules.add(homeScreen);
        for (UserModule module : Main.userModules) {
            if (module.isAllowed()) {
                switch (module.getModule()) {
//                    case ACCOUNTING:
//                        modules.add(accounting);
//                        break;
                    //show accounting
                    case DOCTOR:
                        modules.add(doctorModule);
                        break;
                    case PHARMACY:
                        modules.add(pharmacyDepartment);
                        break;
                    case REGISTRATION:
                        modules.add(patientRegistration);
                        break;
                    case LAB:
                        modules.add(labDepartment);
                        break;
                    case BILLING:
                        modules.add(billing);
                        break;
                    case REPORTS:
                        modules.add(reports);
                        break;
                    case INVENTORY:
                        modules.add(inventory);
                        break;
                    case TRIAGE_STATION:
                        modules.add(nursingDepartment);
                        break;
                    case ADMIN_DASHBOARD:
                        modules.add(administrative);
                        break;
                    case PATIENT_RECORDS:
                        modules.add(patientRecords);
                        break;
                    case RADIOLOGY:
                        modules.add(radiology);
                        break;
                    case BED_AND_WARD:
                        modules.add(bedManagement);
                        break;
                    case INPATIENT_MANAGEMENT:
                        modules.add(inpatientManagement);
                }
            }
        }

        for (HBox module : modules) {
            module.setVisible(true);
            module.setManaged(true);
        }
    }

    private void handleClick(Event event) {
        if (selectedSideNavItem != null) {
            selectedSideNavItem.getStyleClass().remove("bg-darker3");
            selectedSideNavItem.getStyleClass().add("bg-normal");
        }
        HBox source = (HBox) event.getSource();
        source.getStyleClass().remove("bg-normal");
        source.getStyleClass().add("bg-darker3");
        selectedSideNavItem = source;

        mainApp.loadModule(source.getId());

    }

    public void setMainApp(Main mainApp) {
        this.mainApp = mainApp;
        userButton.setText(Main.currentUser.getLoginName());

    }

    public void setApplicationInfo() {
        Hospital hospital = HospitalDAO.getHospital();
        hospitalName.setText(hospital.getName().toUpperCase());
        hospitalAddress.setText(hospital.getAddress());
    }
}


