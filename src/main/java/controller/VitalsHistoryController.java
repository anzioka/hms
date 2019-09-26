package main.java.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;
import main.java.model.Inpatient;
import main.java.model.PatientVitals;
import main.java.util.DBUtil;
import main.java.util.DateUtil;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

/**
 * Created by alfonce on 18/07/2017.
 */
public class VitalsHistoryController {

    @FXML
    private TableView<PatientVitals> tableView;

    @FXML
    private TableColumn<PatientVitals, Double> weight, height, bmi, systolicBp, diastolicBp, bodyTemp,
            respRate, pulseRate, spO2;

    @FXML
    private TableColumn<PatientVitals, String> dateCreated;

    private Stage stage;

    public void initialize() {
        setUpTable();
        getVitalsRecords();
    }

    private void setUpTable() {
        //place holder
        Label label = new Label("No previous vitals records found!");
        label.getStyleClass().add("text-danger");
        tableView.setPlaceholder(label);

        //columns
        dateCreated.setCellValueFactory(param -> new SimpleStringProperty(DateUtil.formatDate(param.getValue()
                .getDateCreated())));
        weight.setCellValueFactory(param -> param.getValue().weightProperty().asObject());
        height.setCellValueFactory(param -> param.getValue().heightProperty().asObject());
        bmi.setCellValueFactory(param -> param.getValue().bmiProperty().asObject());
        systolicBp.setCellValueFactory(param -> param.getValue().systolicBpProperty().asObject());
        diastolicBp.setCellValueFactory(param -> param.getValue().diastolicBpProperty().asObject());
        bodyTemp.setCellValueFactory(param -> param.getValue().bodyTempProperty().asObject());
        respRate.setCellValueFactory(param -> param.getValue().respiratoryRateProperty().asObject());
        pulseRate.setCellValueFactory(param -> param.getValue().pulseRateProperty().asObject());
        spO2.setCellValueFactory(param -> param.getValue().spo2Property().asObject());
    }

    private void getVitalsRecords() {
        Inpatient patient = ViewInpatientController.patient;
        String sql = "select vitals.* from vitals " +
                "inner join inpatients on inpatients.admission_num = vitals.AdmissionNum " +
                "where AdmissionNum = " + patient.getAdmissionNumber() + " " +
                "and inpatients.status = '" + Inpatient.Status.ADMITTED + "'";
        ObservableList<PatientVitals> list = FXCollections.observableArrayList();
        try {
            ResultSet resultSet = DBUtil.executeQuery(sql);
            if (resultSet != null) {
                while (resultSet.next()) {
                    PatientVitals vitals = new PatientVitals();
                    vitals.setDateCreated(resultSet.getObject("DateCreated", LocalDate.class));
                    vitals.setBmi(resultSet.getDouble("BMI"));
                    vitals.setWeight(resultSet.getDouble("Weight"));
                    vitals.setHeight(resultSet.getDouble("Height"));
                    vitals.setDiastolicBp(resultSet.getDouble("DiastolicBp"));
                    vitals.setSystolicBp(resultSet.getDouble("SystolicBp"));
                    vitals.setBodyTemp(resultSet.getDouble("BodyTemperature"));
                    vitals.setRespiratoryRate(resultSet.getDouble("RespiratoryRate"));
                    vitals.setPulseRate(resultSet.getDouble("PulseRate"));
                    vitals.setSpo2(resultSet.getDouble("SPO2"));
                    vitals.setRhesusFactor(resultSet.getString("RhesusFactor"));
                    list.add(vitals);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        tableView.setItems(list);
    }

    void setStage(Stage stage) {
        this.stage = stage;
    }

    @FXML
    public void onClose() {
        stage.close();
    }
}
