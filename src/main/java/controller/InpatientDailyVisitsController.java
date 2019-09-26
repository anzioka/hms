package main.java.controller;

import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import main.Main;
import main.java.dao.InpatientDao;
import main.java.model.InpatientVisit;
import main.java.model.TimePeriod;
import main.java.util.AlertUtil;
import main.java.util.DBUtil;
import main.java.util.DateUtil;
import main.java.util.TimePicker;

import java.io.IOException;
import java.time.LocalDate;

public class InpatientDailyVisitsController {
    @FXML
    private ScrollPane container;
    @FXML
    private Button takeVitalsButton;
    @FXML
    private DatePicker datePicker;
    @FXML
    private ChoiceBox<String> hourChoiceBox, minuteChoiceBox;
    @FXML
    private ChoiceBox<TimePeriod> timePeriodChoiceBox;
    @FXML
    private TextArea notes;
    @FXML
    private TableView<InpatientVisit> tableView;
    @FXML
    private TableColumn<InpatientVisit, String> date, time, nurse, options;

    private InpatientVisit inpatientVisit;
    private TimePicker timePicker;
    private InpatientVisit.Category category;

    @FXML
    private void initialize() {
        datePicker.setValue(LocalDate.now());
        datePicker.setConverter(DateUtil.getDatePickerConverter());

        timePicker = new TimePicker(hourChoiceBox, minuteChoiceBox, timePeriodChoiceBox);
        timePicker.configureTime();
    }

    private void setUpTable() {

        //col widths
        //placeholder
        Label label = new Label("No previous nurse visits");
        if (category == InpatientVisit.Category.DOCTOR) {
            label.setText("No previous doctor visits");
        }
        label.getStyleClass().add("text-danger");
        tableView.setPlaceholder(label);

        date.setCellValueFactory(param -> DateUtil.dateStringProperty(param.getValue().getDateCreated()));
        time.setCellValueFactory(param -> DateUtil.timeStringProperty(param.getValue().getTimeCreated()));
        nurse.setCellValueFactory(param -> param.getValue().userNameProperty());
        options.setCellFactory(param -> new TableCell<InpatientVisit, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                int index = getIndex();
                if (index >= 0 && index < tableView.getItems().size()) {
                    HBox hBox = new HBox(5);
                    hBox.setAlignment(Pos.CENTER);
                    Button button = new Button("View Notes");
                    button.getStyleClass().add("btn-primary-outline");
                    button.setOnAction(event -> {
                        viewNurseVisitNotes(tableView.getItems().get(index));
                    });
                    setGraphic(button);
                } else {
                    setGraphic(null);
                }
            }
        });
    }

    private void viewNurseVisitNotes(InpatientVisit inpatientVisit) {
        //TODO : enable viewing previous note
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource
                    ("main/resources/view/view_inpatient_visit.fxml"));
            Stage stage = new Stage();
            stage.setScene(new Scene(loader.load()));
            stage.initModality(Modality.WINDOW_MODAL);
            stage.setResizable(false);

            ViewDailyVisitNotesController controller = loader.getController();
            controller.setVisitCategory(getVisitCategory());
            controller.setStage(stage);
            controller.setNurseVisit(inpatientVisit);

            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onNewNote() {
        notes.clear();
        notes.requestFocus();
        timePicker.configureTime();
        this.inpatientVisit = null;
    }

    private void getData() {
        //get previous visits
        Task<ObservableList<InpatientVisit>> task = new Task<ObservableList<InpatientVisit>>() {
            @Override
            protected ObservableList<InpatientVisit> call() throws Exception {
                return InpatientDao.getVisitNotes(getVisitCategory(), ViewInpatientController.patient.getAdmissionNumber());
            }
        };
        task.setOnSucceeded(event -> {
            tableView.setItems(task.getValue());
        });
        new Thread(task).start();

    }

    @FXML
    private void onRecordVitals() {
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
            controller.setParameters(ViewInpatientController.patient.getAdmissionNumber(), true);
            controller.setStage(stage);

            //show
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onSave() {
        LocalDate date = datePicker.getValue() == null ? LocalDate.now() : datePicker.getValue();
        boolean newVisit = false;
        if (inpatientVisit == null) {
            newVisit = true;
            inpatientVisit = new InpatientVisit();
        }
        inpatientVisit.setAdmissionNum(ViewInpatientController.patient.getAdmissionNumber());
        inpatientVisit.setNotes(notes.getText());
        inpatientVisit.setUserId(Main.currentUser.getUserId());
        inpatientVisit.setUserName(Main.currentUser.getFirstName() + Main.currentUser.getLastName());
        inpatientVisit.setDateCreated(date);
        inpatientVisit.setCategory(getVisitCategory());
        inpatientVisit.setTimeCreated(timePicker.getSelectedTime());
        if (DBUtil.saveInpatientVisitNotes(inpatientVisit)) {
            AlertUtil.showAlert("Nurse Visit", "Notes have been successfully saved!", Alert.AlertType.INFORMATION);
            if (newVisit) {
                tableView.getItems().add(inpatientVisit);
            }
            onNewNote();
        } else {
            AlertUtil.showAlert("Error", "An error occurred while attempting to save notes", Alert.AlertType.ERROR);
        }
    }

    private InpatientVisit.Category getVisitCategory() {
        return category;
    }

    void setVisitCategory(InpatientVisit.Category category) {
        this.category = category;
        if (category == InpatientVisit.Category.DOCTOR) {
            takeVitalsButton.setVisible(false);
            takeVitalsButton.setManaged(false);
            nurse.setText("Doctor");
        }
        setUpTable();
        getData();
    }
}
