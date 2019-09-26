package main.java.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import main.java.model.InpatientVisit;
import main.java.util.DateUtil;

public class ViewDailyVisitNotesController {
    @FXML
    private Label date, time, user, notes, userLabel;

    private Stage stage;

    void setStage(Stage stage) {
        this.stage = stage;
    }

    void setNurseVisit(InpatientVisit inpatientVisit) {
        time.setText(DateUtil.formatTime(inpatientVisit.getTimeCreated()));
        date.setText(DateUtil.formatDate(inpatientVisit.getDateCreated()));
        user.setText(inpatientVisit.getUserName());

        notes.setText(inpatientVisit.getNotes());
    }

    @FXML
    private void onClose() {
        stage.close();
    }

    void setVisitCategory(InpatientVisit.Category visitCategory) {
        if (visitCategory == InpatientVisit.Category.DOCTOR) {
            userLabel.setText("Doctor");
        }
    }
}
