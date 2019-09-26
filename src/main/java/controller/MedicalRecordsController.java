package main.java.controller;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import main.java.model.Inpatient;
import main.java.model.LabRequest;
import main.java.model.PatientQueue;
import main.java.model.RadiologyRequest;
import main.java.util.AlertUtil;
import main.java.util.DBUtil;

import java.io.IOException;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class MedicalRecordsController {
    @FXML
    private Label numVisits, numAdmissions;
    @FXML
    private HBox container;
    private String patientId;

    @FXML
    private void initialize() {
        for (Node node : container.getChildren()) {
            node.setOnMouseClicked(event -> {
                handleClicked((VBox) node);
            });
        }
        getData();
    }

    private void handleClicked(VBox node) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("main/resources/view/" + node.getId() + ".fxml"));
            Stage stage = new Stage();
            stage.setScene(new Scene(loader.load()));
            stage.initOwner(container.getScene().getWindow());
            stage.initModality(Modality.WINDOW_MODAL);
            if (node.getId().equals("outpatient_medical_records")) {
                if (!numVisits.getText().equals("0")) {
                    OutpatientRecordsController controller = loader.getController();
                    controller.setPatientId(patientId);
                    controller.setContext(this);
                    stage.setTitle("Outpatient Medical Records");
                    stage.showAndWait();

                } else {
                    AlertUtil.showAlert("No Records", "Patient does not have any previous outpatient history.", Alert.AlertType.INFORMATION);
                }

            } else {
                if (!numAdmissions.getText().equals("0")) {
                    InpatientRecordsController controller = loader.getController();
                    controller.setPatientId(patientId);
                    controller.setContext(this);
                    stage.setTitle("Inpatient Medical Records");
                    stage.showAndWait();
                } else {
                    AlertUtil.showAlert("No Records", "Patient does not have any previous admission history.", Alert.AlertType.INFORMATION);
                }

            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setPatientId(String currentPatientId) {
        this.patientId = currentPatientId;
        getData();
    }

    private void getData() {
        Task<List<String>> task = new Task<List<String>>() {
            @Override
            protected List<String> call() throws Exception {
                List<String> list = new ArrayList<>();
                //inpatient
                String sql = "select count(*) as count from inpatients where status = '" + Inpatient.Status.DISCHARGED + "' " +
                        "and patient_id = '" + patientId + "'";
                ResultSet resultSet = DBUtil.executeQuery(sql);
                if (resultSet != null && resultSet.next()) {
                    list.add(resultSet.getString("count"));
                } else {
                    list.add("0");
                }

                //outpatient
                sql = "select count(*) as count from queues where status = '" + PatientQueue.Status.DISCHARGED + "' " +
                        "and patientId = '" + patientId + "'";
                resultSet = DBUtil.executeQuery(sql);
                if (resultSet != null && resultSet.next()) {
                    list.add(resultSet.getString("count"));
                } else {
                    list.add("0");
                }
                return list;
            }
        };
        task.setOnSucceeded(event -> {
            numAdmissions.setText(task.getValue().get(0));
            numVisits.setText(task.getValue().get(1));
        });
        new Thread(task).start();
    }

    void setUpTables(TableView<LabRequest> labTestsTableView, TableColumn<LabRequest, String> labTest, TableColumn<LabRequest, String> labResult, TableView<RadiologyRequest> radiologyTableView, TableColumn<RadiologyRequest, String> radiologyTest, TableColumn<RadiologyRequest, String> radiologyResult, VBox container) {
        labTest.setCellValueFactory(param -> param.getValue().nameProperty());
        labResult.setCellFactory(param -> new TableCell<LabRequest, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                int index = getIndex();
                if (index >= 0 && index < labTestsTableView.getItems().size()) {
                    ImageView imageView = new ImageView(new Image(getClass().getClassLoader().getResourceAsStream("main/resources/images/lock.png")));

                    imageView.setFitHeight(18);
                    imageView.setFitWidth(18);

                    Button view = new Button();
                    view.getStyleClass().remove("button");
                    view.setGraphic(imageView);
                    view.setOnAction(event -> {
                        viewLabTestResult(labTestsTableView.getItems().get(index), container);
                    });
                    setGraphic(view);
                } else {
                    setGraphic(null);
                }
            }
        });

        radiologyTest.setCellValueFactory(param -> param.getValue().descriptionProperty());
        radiologyResult.setCellFactory(param -> new TableCell<RadiologyRequest, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                int index = getIndex();
                if (index >= 0 && index < radiologyTableView.getItems().size()) {
                    ImageView imageView = new ImageView(new Image(getClass().getClassLoader().getResourceAsStream("main/resources/images/lock.png")));
                    imageView.setFitHeight(18);
                    imageView.setFitWidth(18);
                    Button button = new Button();
                    button.getStyleClass().remove("button");
                    button.setGraphic(imageView);
                    button.setOnMouseClicked(event -> {
                        viewRadiologyResult(radiologyTableView.getItems().get(index).getRequestId(), container);
                    });
                    setGraphic(button);
                } else {
                    setGraphic(null);
                }
            }
        });
    }

    private void viewRadiologyResult(int requestId, VBox container) {

    }

    private void viewLabTestResult(LabRequest request, VBox container) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource
                    ("main/resources/view/lab-test-result.fxml"));
            Stage stage = new Stage();
            stage.setScene(new Scene(loader.load()));
            stage.initModality(Modality.WINDOW_MODAL);
            stage.setResizable(false);
            stage.initOwner(container.getScene().getWindow());
            LabTestResultController controller = loader.getController();
            controller.setStage(stage);
            controller.setParameters(request.getId(), request.getTestId());

            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    int getCurrentIndex(String id, int currentIndex, int numRecords) {
        if (id.equals("prev")) {
            if (currentIndex != 0) {
                currentIndex -= 1;
            }
        } else {
            if (currentIndex != numRecords - 1) {
                currentIndex += 1;
            }
        }
        return currentIndex;
    }
}
