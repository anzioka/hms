package main.java.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import main.java.dao.PatientDAO;
import main.java.model.Patient;
import main.java.util.DateUtil;
import main.java.util.StringUtil;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by alfonce on 26/07/2017.
 */
public class PatientRecordsController {
    @FXML
    private AnchorPane container;
    @FXML
    private TextField searchInput;
    @FXML
    private TableView<Patient> tableView;

    @FXML
    private TableColumn<Patient, String> name, age, sex, date_of_birth, options;

    @FXML
    private Pagination pagination;

    private ObservableList<Patient> allPatients = FXCollections.observableArrayList();
    private boolean editingAllowed;

    @FXML
    public void initialize() {
        setUpTable();
        searchInput.textProperty().addListener((observable, oldValue, newValue) -> filterResults(newValue.toLowerCase
                ()));
        getPatientsData();

    }

    private void getPatientsData() {
        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.schedule(() -> {
            Task<ObservableList<Patient>> task = new Task<ObservableList<Patient>>() {
                @Override
                protected ObservableList<Patient> call() {
                    return PatientDAO.getPatientObservableList();
                }
            };
            task.setOnSucceeded(event -> {
                allPatients = PatientDAO.getPatientObservableList();
                configurePagination(allPatients);
                Label label = new Label("No patient records found!");
                label.getStyleClass().add("text-danger");
                tableView.setPlaceholder(label);
            });
            new Thread(task).start();
        }, 2, TimeUnit.SECONDS);
    }


    private void configurePagination(ObservableList<Patient> list) {
        //give an option to display more
        int numRecordsPerPage = 25;
        int numPages = (int) Math.ceil((double) list.size() / numRecordsPerPage);
        if (numPages == 0) {
            numPages = 1;
        }
        pagination.setPageCount(numPages);
        pagination.setPageFactory(param -> {
            tableView.setItems(list);
            return tableView;
        });
    }

    private void filterResults(String filter) {
        if (filter == null || filter.isEmpty()) {
            configurePagination(allPatients);
            return;
        }
        ObservableList<Patient> filteredList = FXCollections.observableArrayList();
        for (Patient patient : allPatients) {
            if (patient.getFirstName().toLowerCase().contains(filter.toLowerCase()) || patient.getLastName().toLowerCase().contains(filter.toLowerCase())) {
                filteredList.add(patient);
            }
        }
        configurePagination(filteredList);
    }

    private void setUpTable() {
        Label label = new Label("Retrieving patient records...");
        label.getStyleClass().add("text-info");
        tableView.setPlaceholder(label);
        date_of_birth.setCellValueFactory(param -> DateUtil.dateStringProperty(param.getValue().getDateOfBirth()));
        age.setCellValueFactory(param -> StringUtil.getStringProperty(param.getValue().getPatientAge()));
        sex.setCellValueFactory(param -> param.getValue().sexualityProperty());
        name.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getFirstName() + " " + param.getValue().getLastName()));
        options.setCellFactory(param -> new TableCell<Patient, String>() {
            @Override
            public void updateItem(String item, boolean empty) {
                int index = getIndex();
                if (index >= 0 && index < tableView.getItems().size()) {
                    Button details = new Button("More Details");
                    details.getStyleClass().add("btn-info-outline");
                    details.setOnAction(event -> {
                        showDetails(tableView.getItems().get(index));
                    });

                    Button history = new Button("History");
                    history.getStyleClass().add("btn-info-outline");
                    history.setOnAction(event -> {
                        showHistory(tableView.getItems().get(index));
                    });
                    HBox hBox = new HBox(5.0, details, history);
                    hBox.setAlignment(Pos.CENTER);
                    setGraphic(hBox);
                } else {
                    setGraphic(null);
                }
            }
        });
    }

    private void showHistory(Patient patient) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("main/resources/view/condensed-patient-history.fxml"));

            Stage stage = new Stage();
            stage.setScene(new Scene(loader.load()));
            stage.initOwner(container.getScene().getWindow());
            stage.initModality(Modality.WINDOW_MODAL);
            stage.setResizable(false);

            CondensedHistoryController controller = loader.getController();
            controller.setPatient(patient);

            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showDetails(Patient patient) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource
                    ("main/resources/view/patient_details.fxml"));
            Parent node = loader.load();

            //stage
            Stage stage = new Stage();
            stage.initModality(Modality.WINDOW_MODAL);
            stage.setScene(new Scene(node));
            stage.setResizable(false);
            stage.initOwner(container.getScene().getWindow());
            //controller
            PatientDetailsController controller = loader.getController();
            controller.setStage(stage);
            controller.setPatient(patient);
            controller.setContext(this);

            //show
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void refreshTable() {
        tableView.refresh();
    }
}