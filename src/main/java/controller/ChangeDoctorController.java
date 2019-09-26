package main.java.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import main.Main;
import main.java.model.User;
import main.java.model.UserCategory;
import main.java.util.DBUtil;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;

/**
 * Created by alfonce on 17/07/2017.
 */
public class ChangeDoctorController {
    @FXML
    private Label patientName, currentDoc;

    @FXML
    private ChoiceBox<User> doctorChoice;
    private List<String> currentQueueItem;
    private Stage stage;
    private boolean okSelected = false;

    @FXML
    public void initialize() {

        doctorChoice.setItems(getDoctors());

    }

    private ObservableList<User> getDoctors() {
        ObservableList<User> doctorList = FXCollections.observableArrayList();
        String sql = "Select Id, LastName from Users where UserCategory = '" + UserCategory.DOCTOR + "'";
        try {
            ResultSet resultSet = DBUtil.executeQuery(sql);
            if (resultSet != null) {
                while (resultSet.next()) {
                    User user = new User();
                    user.setUserId(resultSet.getInt("ID"));
                    user.setLastName(resultSet.getString("LastName"));
                    user.setCategory(UserCategory.DOCTOR);
                    doctorList.add(user);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            Main.LOGGER.logp(Level.SEVERE, ChangeDoctorController.class.getName(), "", "Error getting list of " +
                    "doctors");
        }
        return doctorList;
    }

    @FXML
    private void onSelectOk() {
        if (doctorChoice.getValue() != null) {
            String sql = "update queues set DoctorID =" + doctorChoice.getValue().getUserId() +
                    " WHERE VisitId = " + currentQueueItem.get(0);
            if (DBUtil.executeStatement(sql)) {
                okSelected = true;
                stage.close();
            }
        }
    }

    @FXML
    private void onCancel() {
        stage.close();

    }

    void setCurrentQueueItem(List<String> currentQueueItem) {
        this.currentQueueItem = currentQueueItem;
        patientName.setText(currentQueueItem.get(2));
        currentDoc.setText(currentQueueItem.get(5));
    }

    void setStage(Stage stage) {
        this.stage = stage;
    }

    boolean isOkSelected() {
        return okSelected;
    }
}



