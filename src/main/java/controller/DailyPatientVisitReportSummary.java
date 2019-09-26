package main.java.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.print.Paper;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import main.java.dao.PatientDAO;
import main.java.model.AgeGroup;
import main.java.model.Patient;
import main.java.model.PaymentMode;
import main.java.model.User;
import main.java.util.*;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by alfonce on 13/07/2017.
 */
public class DailyPatientVisitReportSummary {

    @FXML
    private Label reportsTitle;

    @FXML
    private Label reportsSubTitle;
    @FXML
    private TableView<List<String>> tableView;

    @FXML
    private TableColumn<List<String>, String> patientId, patientName, patientSex, age, phoneNumber, paymentMode,
            options;

    @FXML
    private HBox buttonBar;

    @FXML
    private VBox reportContainer;

    private String sex;
    private PaymentMode paymentModeVal;

    private AgeGroup ageGroup;
    private User doctor;

    @FXML
    public void initialize() {

        reportsSubTitle.setVisible(false);
        reportsSubTitle.setManaged(false);

        setUpTable();
    }

    private void setUpTable() {
        patientId.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().get(0)));
        patientName.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().get(1)));
        age.setCellValueFactory(param -> new SimpleStringProperty(AgeUtil.getAge(DateUtil.parseDate(param.getValue()
                .get(2)))));
        patientSex.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().get(3)));
        phoneNumber.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().get(4)));
        paymentMode.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().get(5)));
        options.setCellFactory(param -> new TableCell<List<String>, String>() {
            @Override
            public void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                int index = getIndex();
                if (index >= 0 && index < tableView.getItems().size()) {
                    Button button = new Button("View");
                    button.getStyleClass().add("btn-info");
                    button.setOnAction(event -> {
                        showPatientDetails(tableView.getItems().get(index).get(0));
                    });

                    setGraphic(button);
                } else {
                    setGraphic(null);
                }
            }
        });
    }

    private void showPatientDetails(String patientId) {
        String sql = "select * from patients where patientId = '" + patientId + "'";
        Patient patient = PatientDAO.getPatient(sql);
        if (patient != null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource
                        ("main/resources/view/patient_details.fxml"));
                Parent node = loader.load();

                //stage
                Stage stage = new Stage();
                stage.initModality(Modality.WINDOW_MODAL);
                stage.setScene(new Scene(node));
                stage.setResizable(false);

                //controller
                PatientDetailsController controller = loader.getController();
                controller.setStage(stage);
                controller.setPatient(patient);

                //show
                stage.showAndWait();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    void setParameters(String date, PaymentMode paymentModeValue, String sexChoiceValue, AgeGroup ageGroup,
                       User doctorChoiceValue) {

        this.paymentModeVal = paymentModeValue;
        this.sex = sexChoiceValue;
        this.ageGroup = ageGroup;
        this.doctor = doctorChoiceValue;
        LocalDate localDate = DateUtil.parseDate(date);

        String sql = "SELECT Patients.PatientId, FirstName, LastName, DateOfBirth, Sex, PhoneNumber, PaymentMode " +
                "FROM Patients " +
                "INNER JOIN Queues on Queues.PatientId = Patients.PatientId " +
                "WHERE Queues.DateCreated = '" + localDate + "' " +
                getOtherConditions() +
                " GROUP BY PatientId, FirstName, LastName, DateOfBirth, Sex, PhoneNumber, PaymentMode";
        getData(sql);
        reportsTitle.setText("Patient Visits Summary, " + DateUtil.formatDateLong(localDate));
        String subTitle = getReportsSubTitle();
        if (!subTitle.isEmpty()) {
            reportsSubTitle.setText(subTitle);
            reportsSubTitle.setManaged(true);
            reportsSubTitle.setVisible(true);
        }
    }

    private String getReportsSubTitle() {
        StringBuilder stringBuilder = new StringBuilder("( ");
        int initialLen = stringBuilder.toString().length();

        if (sex != null) {
            stringBuilder.append("Sex : ").append(sex).append(", ");
        }
        if (ageGroup != null) {
            stringBuilder.append("AgeGroup : ").append(ageGroup.toString()).append(", ");
        }

        if (doctor != null) {
            stringBuilder.append("Doctor : ").append(doctor.getLastName()).append(", ");
        }

        if (paymentModeVal != null) {
            stringBuilder.append("Payment Mode : ").append(paymentModeVal.toString());
        }

        if (stringBuilder.toString().length() == initialLen) {
            return "";
        } else {
            stringBuilder.append(" )");
        }
        return stringBuilder.toString();
    }

    private void getData(String sql) {
        ObservableList<List<String>> patientList = FXCollections.observableArrayList();
        try {
            ResultSet resultSet = DBUtil.executeQuery(sql);
            if (resultSet != null) {
                while (resultSet.next()) {
                    List<String> entry = new ArrayList<>();
                    entry.add(resultSet.getString("PatientId"));
                    entry.add(resultSet.getString("FirstName") + " " + resultSet.getString("LastName"));
                    entry.add(DateUtil.formatDate(resultSet.getObject("DateOfBirth", LocalDate.class)));
                    entry.add(resultSet.getString("Sex"));
                    entry.add(resultSet.getString("PhoneNumber"));
                    entry.add(PaymentMode.valueOf(resultSet.getString("PaymentMode")).toString());
                    patientList.add(entry);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if (ageGroup == null) {
            tableView.setItems(patientList);
        } else {
            filterListByAgeGroup(patientList);
        }
    }

    private void filterListByAgeGroup(ObservableList<List<String>> patientList) {
        Iterator<List<String>> iterator = patientList.iterator();
        while (iterator.hasNext()) {
            List<String> current = iterator.next();
            int age = AgeUtil.getYears(DateUtil.parseDate(current.get(2)));
            if (!ageGroup.containsAge(age)) {
                iterator.remove();
            }
        }

        tableView.setItems(patientList);
    }

    private String getOtherConditions() {
        StringBuilder stringBuilder = new StringBuilder();
        if (paymentModeVal != null) {
            stringBuilder.append(" AND PaymentMode = '").append(paymentModeVal).append("'");
        }

        if (sex != null) {
            stringBuilder.append(" AND Sex = '").append(sex).append("'");
        }

        if (doctor != null) {
            stringBuilder.append(" AND DoctorId = ").append(doctor.getUserId());
        }
        return stringBuilder.toString();
    }

    @FXML
    public void onPrint() {
        buttonBar.setVisible(false);
        buttonBar.setManaged(false);

        options.setVisible(false);

        boolean print = PrinterUtil.printNode(reportContainer, Paper.A4);
        if (!print) {
            AlertUtil.showAlert("Printer Error", "Could not connect to a printer!", Alert.AlertType.ERROR);
        }
        buttonBar.setManaged(true);
        buttonBar.setVisible(true);
        options.setVisible(true);
    }
}
