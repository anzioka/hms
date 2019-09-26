package main.java.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import main.java.model.*;
import main.java.util.*;

import java.util.Map;

public class RadiologyResultController {
    @FXML
    private Label date, doctor, result, exam;
    public void setParameters(String date, String doctor, String result, String testCategory, String description) {
        exam.setText(testCategory + " (" + description + ")");
        this.result.setText(result);
        this.doctor.setText(doctor);
        this.date.setText(date);
    }
}
