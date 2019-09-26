package main.java.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import main.java.model.PatientQueue;
import main.java.util.AgeUtil;
import main.java.util.DBUtil;
import main.java.util.DateUtil;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static main.java.util.StringUtil.getStringProperty;

/**
 * Created by alfonce on 17/07/2017.
 */
public class PatientQueueController {
    @FXML
    private TableView<List<String>> tableView;

    @FXML
    private TableColumn<List<String>, String> patientNo, patientName, age, serviceType, doctorName, status, options,
            timeCreated;

    @FXML
    private Label dateToday;

    @FXML
    public void initialize() {
        dateToday.setText(DateUtil.formatDateLong(LocalDate.now()));
        setUpTable();
        getPatientsOnQueue();
    }

    private void getPatientsOnQueue() {

        Task<ObservableList<List<String>>> task = new Task<ObservableList<List<String>>>() {
            @Override
            protected ObservableList<List<String>> call() {
                return getQueueHelper();
            }
        };

        task.setOnSucceeded(event -> {
            tableView.setItems(task.getValue());
        });
        new Thread(task).start();
    }

    private ObservableList<List<String>> getQueueHelper() {
        ObservableList<List<String>> queues = FXCollections.observableArrayList();
        String sql = "SELECT Queues.VisitID, Queues.PatientId, Queues.TimeCreated, Patients.FirstName, Patients" +
                ".LastName, " +
                "Patients.DateOfBirth, Queues.ServiceType , Users.LastName, Queues.Status " +
                "FROM ((Patients " +
                "INNER JOIN Queues on Queues.PatientId = Patients.PatientId) " +
                "LEFT JOIN Users on Users.Id = Queues.DoctorId) " +
                "WHERE Queues.Status != '" + PatientQueue.Status.DISCHARGED + "'";
        try {
            ResultSet resultSet = DBUtil.executeQuery(sql);
            if (resultSet != null) {
                while (resultSet.next()) {
                    List<String> entry = new ArrayList<>();
                    entry.add(resultSet.getString("VisitId"));
                    entry.add(resultSet.getString("PatientId"));
                    entry.add(resultSet.getString("FirstName") + " " + resultSet.getString("Patients.LastName"));
                    entry.add(AgeUtil.getAge(resultSet.getObject("DateOfBirth", LocalDate.class)));
                    entry.add(resultSet.getString("ServiceType"));
                    if (resultSet.getString("Users.LastName") == null) {
                        entry.add(null);
                    } else {
                        entry.add("Dr. " + resultSet.getString("Users.LastName"));
                    }
                    entry.add(resultSet.getString("Status"));
                    entry.add(resultSet.getString("TimeCreated"));

                    queues.add(entry);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return queues;
    }

    private void setUpTable() {

        //place holder
        Label label = new Label("No patients on queue!");
        label.getStyleClass().add("text-danger");
        tableView.setPlaceholder(label);

        //columns
        patientNo.setCellValueFactory(param -> getStringProperty(param.getValue().get(1)));
        patientName.setCellValueFactory(param -> getStringProperty(param.getValue().get(2)));
        age.setCellValueFactory(param -> getStringProperty(param.getValue().get(3)));
        serviceType.setCellValueFactory(param -> getStringProperty(param.getValue().get(4)));
        doctorName.setCellValueFactory(param -> getStringProperty(param.getValue().get(5)));
        status.setCellValueFactory(param -> getStringProperty(param.getValue().get(6)));
        timeCreated.setCellValueFactory(param -> getStringProperty(DateUtil.formatTime(LocalTime.parse(param.getValue
                ().get(7)))));

        options.setCellFactory(param -> new TableCell<List<String>, String>() {
            @Override
            public void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                int index = getIndex();
                if (index >= 0 && index < tableView.getItems().size()) {
                    VBox buttons = new VBox(5.0);
                    buttons.setAlignment(Pos.CENTER);

                    Button button = new Button("Change Doc");
                    button.getStyleClass().add("btn-info-outline");
                    button.setOnAction(event -> {
                        changeAssignedDoc(tableView.getItems().get(index));
                    });
                    if (tableView.getItems().get(index).get(5) == null) {
                        button.setDisable(true);
                    }
                    buttons.getChildren().add(button);

                    //delete button
                    button = new Button("Delete");
                    button.getStyleClass().add("btn-danger-outline");
                    button.setOnAction(event -> {
                        deleteFromQueue(tableView.getItems().get(index));
                    });

                    buttons.getChildren().add(button);
                    setGraphic(buttons);
                } else {
                    setGraphic(null);
                }
            }
        });
    }

    private void deleteFromQueue(List<String> entry) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Dequeue Patient");
        alert.setContentText("Are you sure you want to remove '" + entry.get(2) + "' from the queue?");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            String sql = "delete from queues where visitId = " + entry.get(0);
            if (DBUtil.executeStatement(sql)) {
                tableView.getItems().remove(entry);
            }
        }
    }

    private void changeAssignedDoc(List<String> entry) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource
                    ("main/resources/view/change_doc.fxml"));
            Parent node = loader.load();

            //stage
            Stage stage = new Stage();
            stage.setScene(new Scene(node));
            stage.setResizable(false);
            stage.initModality(Modality.WINDOW_MODAL);

            //controller
            ChangeDoctorController controller = loader.getController();
            controller.setCurrentQueueItem(entry);
            controller.setStage(stage);

            //show
            stage.showAndWait();

            if (controller.isOkSelected()) {
                entry.set(5, getAssignedDoc(entry.get(0)));
                tableView.refresh();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private String getAssignedDoc(String visitId) {
        String result = null;
        String sql = "select LastName from Users " +
                "INNER JOIN Queues on Queues.DoctorId = Users.ID " +
                "WHERE Queues.VisitId = '" + visitId + "'";
        try {
            ResultSet resultSet = DBUtil.executeQuery(sql);
            if (resultSet != null) {
                resultSet.next();
                result = "Dr. " + resultSet.getString("LastName");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

}
