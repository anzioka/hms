package main.java.controller;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Orientation;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import main.Main;
import main.java.model.Appointment;
import main.java.util.AlertUtil;
import main.java.util.DBUtil;
import main.java.util.DateUtil;

import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class ViewAppointments {

    @FXML
    private VBox container;
    @FXML
    private Label title;
    private Integer userId, appointmentId;
    private LocalDate localDate;

    public void setParameters(LocalDate localDate, Integer appointmentId, int userId) {
        title.setText(localDate.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.getDefault()) + " " + DateUtil.formatDateLong(localDate));
        this.appointmentId = appointmentId;
        this.localDate = localDate;
        this.userId = userId;
        getAppointments();
    }

    private void getAppointments() {
        Task<List<Appointment>> task = new Task<List<Appointment>>() {
            @Override
            protected List<Appointment> call() throws Exception {
                List<Appointment> list = new ArrayList<>();
                String sql = "select appointments.id, note, time, FirstName, LastName from appointments " +
                        "inner join Patients on patients.PatientId = appointments.patient_id " +
                        "where date = '" + localDate + "' " +
                        "and doctor_id = " + userId;
                if (appointmentId != null) {
                    sql += " and appointments.id = " + appointmentId;
                }
                ResultSet resultSet = DBUtil.executeQuery(sql);
                if (resultSet != null) {
                    while (resultSet.next()) {
                        Appointment appointment = new Appointment();
                        appointment.setId(resultSet.getInt("id"));
                        appointment.setTime(resultSet.getObject("time", LocalTime.class));
                        appointment.setPatientName(resultSet.getString("FirstName") + " " + resultSet.getString("LastName"));
                        appointment.setNote(resultSet.getString("note"));
                        list.add(appointment);
                    }
                }
                return list;
            }
        };
        task.setOnSucceeded(event -> {
            showDetails(task.getValue());
        });
        new Thread(task).start();
    }

    private void showDetails(List<Appointment> appointments) {
        container.getChildren().clear();
        for (Appointment appointment : appointments) {
            HBox hBox = new HBox(5);
            hBox.setMinHeight(40);
            hBox.setPrefHeight(Control.USE_COMPUTED_SIZE);
            Label timeLabel = new Label(DateUtil.formatTime(appointment.getTime()));
            timeLabel.setMinWidth(Control.USE_PREF_SIZE);
            timeLabel.setOpacity(0.66);

            Pane pane = new Pane();
            HBox.setHgrow(pane, Priority.ALWAYS);

            VBox vBox = new VBox(3.0);
            vBox.getChildren().add(new Label("Patient : " + appointment.getPatientName()));
            if (!appointment.getNote().isEmpty()) {
                Label label = new Label("Note : " + appointment.getNote());
                label.setWrapText(true);
                vBox.setPrefHeight(Control.USE_COMPUTED_SIZE);
                vBox.getChildren().add(label);
            }

            Button edit = new Button();
            edit.getStyleClass().remove("button");
            ImageView imageView = new ImageView(new Image(getClass().getClassLoader().getResourceAsStream("main/resources/images/edit.png")));
            imageView.setFitHeight(20);
            imageView.setFitWidth(20);
            edit.setGraphic(imageView);
            edit.setOnAction(event -> {
                handleEdit(appointment);
            });

            Button delete = new Button();
            delete.getStyleClass().remove("button");
            imageView = new ImageView(new Image(getClass().getClassLoader().getResourceAsStream("main/resources/images/delete-forever.png")));
            imageView.setFitHeight(20);
            imageView.setFitWidth(20);
            delete.setGraphic(imageView);
            delete.setOnAction(event -> {
                handleDelete(appointment);
            });
            delete.setDisable(userId != Main.currentUser.getUserId());
            hBox.getChildren().addAll(timeLabel, new Label("-"), vBox, pane, edit, delete);
            container.getChildren().add(hBox);
            container.getChildren().add(new Separator(Orientation.HORIZONTAL));
        }
    }

    private void handleDelete(Appointment appointment) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to cancel appointment?", ButtonType.NO, ButtonType.YES);
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.YES) {
            if (DBUtil.executeStatement("delete from appointments where id = " + appointment.getId())) {
                AlertUtil.showAlert("", "Appointment successfully removed", Alert.AlertType.INFORMATION);
                getAppointments();
            } else {
                AlertUtil.showGenericError();
            }
        }
    }

    private void handleEdit(Appointment appointment) {
        AlertUtil.showAlert("Change Appointment", "Functionality not yet available", Alert.AlertType.INFORMATION);
    }
}
