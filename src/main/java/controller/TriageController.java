package main.java.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import main.java.dao.PatientDAO;
import main.java.dao.PatientQueueDAO;
import main.java.model.Patient;
import main.java.model.PatientQueue;
import main.java.util.DateUtil;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by alfonce on 10/05/2017.
 */

public class TriageController {
    //scheduler service;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    @FXML
    private AnchorPane container;
    @FXML
    private TableView<PatientQueue> queueTableView;
    @FXML
    private TableColumn<PatientQueue, String> patientIdCol, firstNameCol, lastNameCol, timeCreated,
            options;
    private Map<Integer, Patient> visitIdPatientMap = new HashMap<>();

    @FXML
    public void initialize() {
        //set data for the table

        //service to read data from db
        scheduler.scheduleAtFixedRate(() -> {
            Task<ObservableList<PatientQueue>> task = new Task<ObservableList<PatientQueue>>() {
                @Override
                protected ObservableList<PatientQueue> call() {
                    ObservableList<PatientQueue> patientQueue = PatientQueueDAO.getPatientsForTriage();
                    if (patientQueue != null) {
                        for (PatientQueue queue : patientQueue) {
                            String sql = "select * from patients where patientId='" + queue.getPatientId() + "'";
                            visitIdPatientMap.put(queue.getQueueId(), PatientDAO.getPatient(sql));
                        }
                    }
                    return patientQueue;
                }
            };

            task.setOnSucceeded(event -> {
                queueTableView.setItems(task.getValue());
            });

            new Thread(task).start();

        }, 0, 2, TimeUnit.SECONDS);
        setUpTable();
    }

    private void setUpTable() {
        //place holder
        Label label = new Label("No patients on queue!");
        label.getStyleClass().add("text-danger");
        queueTableView.setPlaceholder(label);

        //columns
        patientIdCol.setCellValueFactory(param -> param.getValue().patientIdProperty());
        firstNameCol.setCellValueFactory(param -> visitIdPatientMap.get(param.getValue().getQueueId())
                .firstNameProperty());
        lastNameCol.setCellValueFactory(param -> visitIdPatientMap.get(param.getValue().getQueueId())
                .lastNameProperty());
        timeCreated.setCellValueFactory(param -> new SimpleStringProperty(DateUtil.formatTime(param.getValue().getTimeCreated())));
        options.setCellFactory(param -> new TableCell<PatientQueue, String>() {
            @Override
            public void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                int index = getIndex();
                if (index >= 0 && index < queueTableView.getItems().size()) {
                    Button button = new Button("Get Vitals");
                    button.getStyleClass().add("btn-info-outline");
                    button.setOnAction(event -> {
                        PatientQueue currentQueue = queueTableView.getItems().get(index);
                        if (currentQueue != null) {
                            getVitalsForQueuedPatient(currentQueue);

                        }
                    });
                    setGraphic(button);

                } else {
                    setGraphic(null);
                }
            }
        });
    }

    private void getVitalsForQueuedPatient(PatientQueue queueItem) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource
                    ("main/resources/view/patient_vitals.fxml"));
            VBox node = loader.load();

            //stage
            Stage stage = new Stage();
            stage.setScene(new Scene(node));
            stage.initModality(Modality.WINDOW_MODAL);
            stage.setResizable(false);
            stage.initOwner(container.getScene().getWindow());

            //controller
            PatientVitalsController controller = loader.getController();
            controller.setParameters(queueItem.getQueueId(), false);
            controller.setStage(stage);

            //show
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
