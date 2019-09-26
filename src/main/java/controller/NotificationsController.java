package main.java.controller;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import main.java.model.LabNotification;
import main.java.util.DBUtil;
import main.java.util.DateUtil;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class NotificationsController {
    @FXML
    private VBox container;
    private static Stage stage;

    public static void setStage(Stage stage) {
        NotificationsController.stage = stage;
    }

    void setNotifications(List<LabNotification> notifications) {
        container.getChildren().clear();
        if (notifications.isEmpty()) {
            Label label = new Label("No notifications found!");
            label.getStyleClass().add("text-danger");
            container.getChildren().add(label);
            container.setAlignment(Pos.CENTER);
        }
        for (LabNotification labNotification : notifications) {
            Task<String> task = new Task<String>() {
                @Override
                protected String call() {
                    ResultSet resultSet = DBUtil.executeQuery("select FirstName, LastName from patients " +
                            "inner join queues on queues.PatientId = patients.PatientId " +
                            "where VisitId = " + labNotification.getVisitId());
                    try {
                        if (resultSet != null && resultSet.next()) {
                            return resultSet.getString("FirstName") + " " + resultSet.getString("LastName");
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                    return null;
                }
            };
            task.setOnSucceeded(event -> {
                showNotification(task.getValue(), labNotification);
            });
            new Thread(task).start();
        }
    }

    private void showNotification(String name, LabNotification labNotification) {
        HBox hBox = new HBox();
        hBox.getStyleClass().add("notification");
        hBox.setAlignment(Pos.CENTER_LEFT);

        Label label = new Label("Lab test results for '" + name + "' posted at " + DateUtil.formatDateTime(labNotification.getLocalDateTime()));
        hBox.getChildren().add(label);

        Pane pane = new Pane();
        HBox.setHgrow(pane, Priority.ALWAYS);
        hBox.getChildren().add(pane);

        Button button = new Button("x");
        button.getStyleClass().remove("button");
        button.getStyleClass().add("btn");
        button.setOnAction(event -> {
            if (DBUtil.executeStatement("delete from lab_result_notifications where id = " + labNotification.getId())) {
                container.getChildren().remove(hBox);
            }
        });
        hBox.getChildren().add(button);
        container.getChildren().add(hBox);
    }
}
