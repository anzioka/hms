package main.java.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

public class AdminDashboardController {

    @FXML
    private Button settingsBtn, hospitalBtn, proceduresBtn, insuranceBtn, diagnosisList;
    @FXML
    private Label numNotificationsLabel, numDoctorsLabel, numNursesLabel, numPharmacistsLabel,
            numLabTechniciansLabel, numCashiersLabel, numPatientsLabel, numMedicineLabel, numInsuranceLabel,
            numWardsLabel, numBedsLabel;
    @FXML
    private BarChart<String, Double> cashCollectionChart;
    @FXML
    private CategoryAxis cashCollectionXAxis, patientVisitsXAxis;

    @FXML
    private NumberAxis cashCollectionYAxis, patientVisitsYAxis;

    @FXML
    private PieChart insuranceChart;

    @FXML
    private AreaChart<String, Double> patientVisitsChart;

    @FXML
    private TabPane tabPane;

    @FXML
    private void initialize() {
        for (Button button : new Button[]{settingsBtn, hospitalBtn, proceduresBtn, insuranceBtn, diagnosisList}) {
            button.setOnAction(event -> {
                openWindow(button);
            });
        }
    }

    @FXML
    private void manageUsers() {
        addTab("settings_users", "System Users");
    }


    private void openWindow(Button button) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("main/resources/view/" + button.getId()
                    + ".fxml"));
            Parent node = loader.load();

            //stage
            Stage stage = new Stage();
            stage.setScene(new Scene(node));
            stage.initOwner(tabPane.getScene().getWindow());
            stage.initModality(Modality.WINDOW_MODAL);

            if (button == hospitalBtn) {
                HospitalInfoController controller = loader.getController();
                controller.setStage(stage);
            }
            //show
            stage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void addTab(String fxmlFileName, String tabTitle) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("main/resources/view/" +
                    fxmlFileName + ".fxml"));
            Parent node = loader.load();

            //close all tabs except main;
            for (int i = 1; i < tabPane.getTabs().size(); i++) {
                tabPane.getTabs().remove(i);
            }

            Tab tab = new Tab(tabTitle);
            tab.setContent(node);
            tab.setClosable(true);

            tabPane.getTabs().add(tab);
            tabPane.getSelectionModel().select(tab);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
