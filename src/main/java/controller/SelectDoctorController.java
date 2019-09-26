package main.java.controller;

import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ChoiceBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import main.java.dao.UsersDAO;
import main.java.model.User;
import main.java.model.UserCategory;
import main.java.util.AlertUtil;

import java.io.IOException;

public class SelectDoctorController {
    @FXML
    private ChoiceBox<User> doctorChoiceBox;

    private String patientId;
    private Stage stage;
    private VBox container;

    @FXML
    private void initialize() {
        getDoctors();
    }

    private void getDoctors() {
        Task<ObservableList<User>> task = new Task<ObservableList<User>>() {
            @Override
            protected ObservableList<User> call() {
                return UsersDAO.getUserObservableList("select * from users where UserCategory = '" + UserCategory.DOCTOR + "'");
            }
        };
        task.setOnSucceeded(event -> {
            doctorChoiceBox.setItems(task.getValue());
        });
        new Thread(task).start();
    }

    @FXML
    private void onViewCalendar() {
        User doctor = doctorChoiceBox.getValue();
        if (doctor == null) {
            AlertUtil.showAlert("", "Please select a doctor from the list", Alert.AlertType.ERROR);
        } else{
            stage.close();
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("main/resources/view/appointment_scheduler.fxml"));
                Stage stage = new Stage();
                stage.setScene(new Scene(loader.load()));
                stage.initModality(Modality.WINDOW_MODAL);
                stage.setResizable(false);
                stage.initOwner(container.getScene().getWindow());

                AppointmentController controller = loader.getController();
                controller.setParameters(doctor.getUserId(), patientId);
                controller.setStage(stage);
                stage.show();

                Rectangle2D rectangle2D = Screen.getPrimary().getBounds();
                stage.setY(0);
                stage.setX((rectangle2D.getWidth() - stage.getWidth()) / 2);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void setContainer(VBox container) {
        this.container = container;
    }
}
