package main.java.controller;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import main.java.dao.HospitalDAO;
import main.java.model.Hospital;
import main.java.util.AlertUtil;
import main.java.util.DBUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class HospitalInfoController {
    private static Stage stage;
    private Hospital hospital;
    @FXML
    private ImageView letterHead;
    @FXML
    private TextField nameField, addressField, phoneNumberField;

    void setStage(Stage stage) {
        HospitalInfoController.stage = stage;
    }

    @FXML
    private void initialize() {
        hospital = HospitalDAO.getHospital();
        nameField.setText(hospital.getName());
        addressField.setText(hospital.getAddress());
        phoneNumberField.setText(hospital.getPhoneNumber());

        try {
            InputStream inputStream = HospitalDAO.getHospitalLetterHeadInputStream();
            if (inputStream != null) {
                letterHead.setImage(new Image(HospitalDAO.getHospitalLetterHeadInputStream()));
                toggleLetterHeadView(true);
            } else {
                toggleLetterHeadView(false);

            }
        } catch (Exception e) {
            e.printStackTrace();
            toggleLetterHeadView(false);
        }
    }

    @FXML
    private void onSaveChanges() {

        hospital.setAddress(addressField.getText());
        hospital.setPhoneNumber(phoneNumberField.getText());
        hospital.setName(nameField.getText());

        if (DBUtil.addHospital(hospital)) {
            AlertUtil.showAlert("Success", "Hospital details have been saved", Alert.AlertType.INFORMATION);
            stage.close();
        } else {
            AlertUtil.showAlert("Error", "An error occurred while trying to save hospital details", Alert.AlertType.ERROR);
        }
    }

    private void toggleLetterHeadView(boolean visible) {
        letterHead.setVisible(visible);
        letterHead.setManaged(visible);
    }

    @FXML
    private void onUploadLetterHead() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Pictures", "*.jpeg", "*.jpg"));
        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            try {
                FileInputStream inputStream = new FileInputStream(file);
                letterHead.setImage(new Image(inputStream));
                //save to db
                DBUtil.saveHospitalLetterHead(file);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            toggleLetterHeadView(true);
        }
    }
}


