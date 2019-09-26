package main.java.controller;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import main.java.dao.InpatientDao;
import main.java.model.ClinicalSummary;
import main.java.util.AlertUtil;
import main.java.util.DBUtil;
import main.java.util.DateUtil;

import java.time.LocalDate;

public class InpatientClinicalSummaryController {
    private ClinicalSummary summary;
    @FXML
    private HBox lastModified;
    @FXML
    private TextArea editText;
    @FXML
    private Label date;

    @FXML
    private void initialize() {
        getSummary();
    }

    private void setLastModified() {
        if (summary != null && summary.getSummary() != null) {
            lastModified.setManaged(true);
            lastModified.setVisible(true);
            date.setText(DateUtil.formatDateLong(summary.getDateModified()));
        } else {
            lastModified.setVisible(false);
            lastModified.setManaged(false);
        }
    }

    private void getSummary() {
        Task<ClinicalSummary> task = new Task<ClinicalSummary>() {
            @Override
            protected ClinicalSummary call() throws Exception {
                return InpatientDao.getClinicalSummary(ViewInpatientController.patient.getAdmissionNumber());
            }
        };
        task.setOnSucceeded(event -> {
            summary = task.getValue();
            if (summary != null && summary.getSummary() != null) {
                editText.setText(summary.getSummary());
            }
            setLastModified();
        });
        new Thread(task).start();
    }

    @FXML
    private void onSave() {
        if (summary == null) {
            summary = new ClinicalSummary();
            summary.setAdmissionNum(ViewInpatientController.patient.getAdmissionNumber());
        }
        summary.setDateModified(LocalDate.now());
        summary.setSummary(editText.getText());
        if (DBUtil.saveClinicalSummary(summary)) {
            AlertUtil.showAlert("Clinical Summary", "Clinical summary has been successfully updated!", Alert.AlertType.INFORMATION);
            setLastModified();
        } else {
            AlertUtil.showAlert("Error", "An error occurred while attempting to update clinical summary.", Alert.AlertType.ERROR);
        }
    }
}
