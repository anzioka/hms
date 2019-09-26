package main.java.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.print.Paper;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import main.java.dao.HospitalDAO;
import main.java.dao.PatientDAO;
import main.java.model.Hospital;
import main.java.model.LabRequest;
import main.java.model.Patient;
import main.java.model.Specimen;
import main.java.util.AlertUtil;
import main.java.util.DBUtil;
import main.java.util.DateUtil;
import main.java.util.PrinterUtil;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by alfonce on 30/07/2017.
 */
public class ViewLabResultsController {
    @FXML
    private Button uploadLetterHeadBtn;

    @FXML
    private Label patientId, firstName, lastName, sex, age, dateOfBirth;

    @FXML
    private TableView<LabRequest> tableView;

    @FXML
    private TableColumn<LabRequest, String> testName, result, specimen;

    @FXML
    private ImageView letterHead;

    @FXML
    private Pane pane;

    private String visitId;

    private Hospital hospital;
    @FXML
    private HBox buttonBar;
    @FXML
    private VBox container;

    @FXML
    public void initialize() {
        hospital = HospitalDAO.getHospital();
//        if (hospital.getLetterHeadIO() != null) {
//            uploadLetterHeadBtn.setVisible(false);
//            uploadLetterHeadBtn.setManaged(false);
//
//            letterHead.setImage(new Image(hospital.getLetterHeadIO()));
//        } else {
//            letterHead.setVisible(false);
//            letterHead.setManaged(false);
//        }

        //table view
        testName.setCellValueFactory(param -> param.getValue().nameProperty());
        result.setCellValueFactory(param -> param.getValue().resultProperty());
        specimen.setCellValueFactory(param -> param.getValue().specimenStringProperty());
    }

    void setVisitId(String visitId) {
        this.visitId = visitId;

        tableView.setItems(getLabRequests());
    }

    private ObservableList<LabRequest> getLabRequests() {
        ObservableList<LabRequest> list = FXCollections.observableArrayList();

        String sql = "Select TestName, Specimen, Result from LabRequests " +
                "WHERE VisitId = " + visitId;
        try {
            ResultSet resultSet = DBUtil.executeQuery(sql);
            if (resultSet != null) {
                while (resultSet.next()) {
                    LabRequest request = new LabRequest();
                    request.setName(resultSet.getString("TestName"));
                    request.setResult(resultSet.getString("Result"));
                    if (resultSet.getString("Specimen") != null) {
                        request.setSpecimen(Specimen.valueOf(resultSet.getString("Specimen")));
                    }
                    list.add(request);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    void setPatientId(String id) {
        String sql = "select * from patients where patientId ='" + id + "'";
        Patient patient = PatientDAO.getPatient(sql);
        if (patient != null) {
            patientId.setText(patient.getPatientId());
            firstName.setText(patient.getFirstName());
            lastName.setText(patient.getLastName());
            age.setText(patient.getPatientAge());
            dateOfBirth.setText(DateUtil.formatDate(patient.getDateOfBirth()));
            sex.setText(patient.getSexuality());
        }
    }

    @FXML
    private void onPrintResults() {
        if (!letterHead.isVisible()) {
            //not uploaded letter head
            AlertUtil.showAlert("Letterhead Required", "Please upload your hospital's letterhead before " +
                    "proceeding", Alert.AlertType.INFORMATION);
            return;
        }

        buttonBar.setVisible(false);
        buttonBar.setManaged(false);
        boolean print = PrinterUtil.printNode(container, Paper.A4);
        if (!print) {
            AlertUtil.showAlert("Printer Error", "An error occurred while attempting to print", Alert.AlertType.ERROR);
        }
        buttonBar.setVisible(true);
        buttonBar.setManaged(false);

    }
}
