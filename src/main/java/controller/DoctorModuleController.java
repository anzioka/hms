package main.java.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;
import main.Main;
import main.java.model.LabNotification;
import main.java.model.PatientQueue;
import main.java.model.UserCategory;
import main.java.util.DBUtil;
import main.java.util.DateUtil;
import main.java.util.NumberUtil;
import main.java.util.StringUtil;
import org.controlsfx.control.Notifications;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by alfonce on 23/04/2017.
 */
public class DoctorModuleController {
    //scheduler service;
    @FXML
    private TableView<List<String>> tableView;
    ;
    @FXML
    private TableColumn<List<String>, String> patientId, firstName, lastName, colorCode, vitalsStatus, sex,
            actionCol, timeRegistered;
    @FXML
    private VBox container;
    @FXML
    private HBox notifications;

    private ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
    private List<LabNotification> list = new ArrayList<>();

    @FXML
    public void initialize() {
        //service to read data from db
        setUpTable();
        onGetData();
        getAllNotifications();
        getUnreadNotifications();
        notifications.setVisible(false);
        notifications.setOnMouseClicked(event -> {
            viewAllNotifications();
        });
    }

    private void viewAllNotifications() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("main/resources/view/notifications.fxml"));
            Stage stage = new Stage();
            stage.setScene(new Scene(loader.load()));
            stage.initOwner(container.getScene().getWindow());
            stage.setResizable(false);
            stage.initModality(Modality.WINDOW_MODAL);
            stage.setTitle("Lab Notifications");
            NotificationsController controller = loader.getController();
            controller.setNotifications(list);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void getAllNotifications() {
        executorService.scheduleAtFixedRate(() -> {
            Task<List<LabNotification>> task = new Task<List<LabNotification>>() {
                @Override
                protected List<LabNotification> call() throws Exception {
                    List<LabNotification> list = new ArrayList<>();
                    String sql = "select * from lab_result_notifications where visit_id " +
                            "in (select VisitId from queues where Status != '" + PatientQueue.Status.DISCHARGED + "' and DoctorId = " + Main.currentUser.getUserId() + ")";
                    ResultSet resultSet = DBUtil.executeQuery(sql);
                    if (resultSet != null ) {
                        while (resultSet.next()) {
                            LabNotification notification = new LabNotification();
                            notification.setVisitId(resultSet.getInt("visit_id"));
                            notification.setLocalDateTime(resultSet.getObject("time", LocalDateTime.class));
                            notification.setId(resultSet.getInt("id"));
                            list.add(notification);
                        }
                    }
                    return list;
                }
            };
            task.setOnSucceeded(event -> {
                list = task.getValue();
                if (!list.isEmpty()) {
                    notifications.setVisible(true);
                    StackPane stackPane = (StackPane) notifications.getChildren().get(0);
                    Label countLabel = (Label) stackPane.getChildren().get(1);
                    countLabel.setText(Integer.toString(list.size()));
                } else{
                    notifications.setVisible(false);
                }
            });
            new Thread(task).start();
        }, 0, 5, TimeUnit.SECONDS);
    }

    private void getUnreadNotifications() {
        executorService.scheduleAtFixedRate(() -> {
            Task<LabNotification> task = new Task<LabNotification>() {
                @Override
                protected LabNotification call() throws Exception {
                    String sql = "select * from lab_result_notifications " +
                            "inner join queues on queues.VisitId = lab_result_notifications.visit_id " +
                            "where DoctorId = " + Main.currentUser.getUserId() + " " +
                            "and status != '" + PatientQueue.Status.DISCHARGED + "' " +
                            "and `read` = 0";
                    ResultSet resultSet = DBUtil.executeQuery(sql);
                    if (resultSet != null && resultSet.next()) {
                        LabNotification labNotification = new LabNotification();
                        labNotification.setId(resultSet.getInt("id"));
                        labNotification.setVisitId(resultSet.getInt("visit_id"));
                        return labNotification;
                    }
                    return null;
                }
            };
            task.setOnSucceeded(event -> {
                if (task.getValue() != null) {
                    DBUtil.executeStatement("update lab_result_notifications set `read` = 1 " +
                            "where id = " + task.getValue().getId());
                    showNotification(task.getValue());
                }
            });
            new Thread(task).start();
        }, 1, 10, TimeUnit.SECONDS);
    }

    private void showNotification(LabNotification labNotification) {
        Notifications.create()
                .title("Lab Results")
                .hideAfter(Duration.seconds(20))
                .position(Pos.TOP_CENTER)
                .text("Lab results for " + getPatientName(labNotification.getVisitId()) + " have been posted!")
                .showInformation();
    }

    private String getPatientName(int visitId) {
        ResultSet resultSet = DBUtil.executeQuery("select FirstName, LastName from patients " +
                "inner join queues on queues.PatientId = patients.PatientId " +
                "where VisitId = " + visitId);
        try {
            if (resultSet != null && resultSet.next()) {
                return resultSet.getString("FirstName") + " " + resultSet.getString("LastName");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @FXML
    private void onGetData() {
        Task<ObservableList<List<String>>> task = new Task<ObservableList<List<String>>>() {
            @Override
            protected ObservableList<List<String>> call() {
                return getPatientsFromQueue();
            }
        };
        task.setOnSucceeded(event -> {
            tableView.setItems(task.getValue());
        });
        new Thread(task).start();
    }

    private void setUpTable() {

        //place holder
        Label label = new Label("There are no patients on the queue!");
        label.getStyleClass().add("text-danger");
        tableView.setPlaceholder(label);

        //columns
        patientId.setCellValueFactory(param -> StringUtil.getStringProperty(param.getValue().get(1)));
        firstName.setCellValueFactory(param -> StringUtil.getStringProperty(param.getValue().get(2)));
        lastName.setCellValueFactory(param -> StringUtil.getStringProperty(param.getValue().get(3)));
        sex.setCellValueFactory(param -> StringUtil.getStringProperty(param.getValue().get(4)));
        vitalsStatus.setCellValueFactory(param -> StringUtil.getStringProperty(param.getValue().get(5)));
        timeRegistered.setCellValueFactory(param -> new SimpleStringProperty(DateUtil.formatTime(LocalTime.parse
                (param.getValue().get(7)))));

        actionCol.setCellFactory(param -> new TableCell<List<String>, String>() {
            @Override
            public void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                int index = getIndex();
                if (index >= 0 && index < tableView.getItems().size()) {
                    Button button = new Button("Open");
                    button.getStyleClass().add("btn-info-outline");
                    button.setOnAction(event -> {
                        showCurrentPatient(tableView.getItems().get(index)); //visit id
                    });
                    setGraphic(button);
                } else {
                    setGraphic(null);
                }
            }
        });
        colorCode.setCellFactory(param -> new TableCell<List<String>, String>() {
            @Override
            public void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                int index = getIndex();
                if (index >= 0 && index < tableView.getItems().size()) {
                    List<String> entry = tableView.getItems().get(index);
                    if (entry.get(6) != null) {
                        Button button = new Button();
                        button.setPrefWidth(40.0);
                        button.setStyle("-fx-background-color: " + entry.get(6));
                        setGraphic(button);
                    } else {
                        Button button = new Button("?");
                        setGraphic(button);
                    }
                } else {
                    setGraphic(null);
                }
            }
        });

    }

    private void showCurrentPatient(List<String> entry) {
        //update queue status
        String sql = "update queues set Status =  '" + PatientQueue.Status.CONSULTATION + "' " +
                "where queues.VisitId = '" + entry.get(0) + "'";

        DBUtil.executeStatement(sql);
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource
                    ("main/resources/view/treat-patient.fxml"));

            Stage stage = new Stage();
            stage.setScene(new Scene(loader.load()));
            stage.initOwner(container.getScene().getWindow());
            stage.initModality(Modality.WINDOW_MODAL);
            stage.setTitle("Patient Consultation");
            OutpatientExaminationController controller = loader.getController();
            controller.setQueueId(NumberUtil.stringToInt(entry.get(0)));
            controller.setPatientId(entry.get(1));
            controller.setStage(stage);

            stage.showAndWait();
            onGetData();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private ObservableList<List<String>> getPatientsFromQueue() {
        ObservableList<List<String>> queueItems = FXCollections.observableArrayList();
        String sql = "Select Queues.VisitId, Queues.PatientId, Queues.TimeCreated, Patients.FirstName, Patients" +
                ".LastName, Sex, Vitals" +
                ".ColorCode " +
                "FROM ((Queues " +
                "INNER JOIN Patients On Patients.PatientId = Queues.PatientId) " +
                "Left Join Vitals on Vitals.VisitId = Queues.VisitID) " +
                "WHERE NOT Queues.Status = 'Discharged' " +
                "AND NOT Queues.DoctorId = 0 ";
        if (Main.currentUser.getCategory() == UserCategory.DOCTOR) {
            sql += " AND Queues.DoctorId = " + Main.currentUser.getUserId();
        }

        try {
            ResultSet resultSet = DBUtil.executeQuery(sql);
            if (resultSet != null) {
                while (resultSet.next()) {
                    List<String> entry = new ArrayList<>();
                    entry.add(resultSet.getString("VisitID"));
                    entry.add(resultSet.getString("PatientId"));
                    entry.add(resultSet.getString("FirstName"));
                    entry.add(resultSet.getString("LastName"));
                    entry.add(resultSet.getString("Sex"));

                    if (resultSet.getString("ColorCode") == null) {
                        entry.add("NOT TAKEN");
                    } else {
                        entry.add("TAKEN");
                    }
                    entry.add(resultSet.getString("ColorCode"));
                    entry.add(resultSet.getString("TimeCreated"));
                    queueItems.add(entry);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return queueItems;
    }

    @FXML
    private void onViewAppointments() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("main/resources/view/appointment_scheduler.fxml"));
            Stage stage = new Stage();
            stage.setResizable(false);
            stage.setScene(new Scene(loader.load()));
            stage.initOwner(container.getScene().getWindow());
            stage.initModality(Modality.WINDOW_MODAL);
            AppointmentController controller = loader.getController();
            controller.setParameters(Main.currentUser.getUserId(), null);
            controller.setStage(stage);
            stage.show();
            Rectangle2D primScreenBounds = Screen.getPrimary().getBounds();
            stage.setX((primScreenBounds.getWidth() - stage.getWidth())/ 2);
            stage.setY(0);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
