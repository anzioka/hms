package main.java.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import main.java.dao.InpatientDao;
import main.java.model.Operation;
import main.java.model.TimePeriod;
import main.java.util.AlertUtil;
import main.java.util.DBUtil;
import main.java.util.DateUtil;
import main.java.util.TimePicker;
import org.controlsfx.control.textfield.TextFields;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

public class InpatientOperationController {
    @FXML
    private DatePicker datePicker;
    @FXML
    private ChoiceBox<TimePeriod> timePeriodChoiceBox;
    @FXML
    private ChoiceBox<String> hourPicker, minutePicker;
    @FXML
    private TextField operationName, indication, surgeon, assistants, anaesthetist, anaesthesia, incision;
    @FXML
    private TextArea procedure;
    @FXML
    private TableView<Operation> tableView;
    @FXML
    private TableColumn<Operation, String> dateCol, timeCol, operationCol, surgeonCol, optionsCol;

    private TimePicker timePicker;

    @FXML
    private void initialize() {
        setUpTable();
        getPreviousOperationsData();
        setAutoComplete();

        datePicker.setConverter(DateUtil.getDatePickerConverter());
        datePicker.setValue(LocalDate.now());

        timePicker = new TimePicker(hourPicker, minutePicker, timePeriodChoiceBox);
        timePicker.configureTime();
    }

    private void setAutoComplete() {
        Task<ObservableList<String>> task = new Task<ObservableList<String>>() {
            @Override
            protected ObservableList<String> call() {
                ObservableList<String> operations = FXCollections.observableArrayList();
                String sql = "select operation from operations";
                ResultSet resultSet = DBUtil.executeQuery(sql);
                try {
                    if (resultSet != null) {
                        while (resultSet.next()) {
                            operations.add(resultSet.getString("operation"));
                        }
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                return operations;
            }
        };
        task.setOnSucceeded(event -> {
            TextFields.bindAutoCompletion(operationName, task.getValue());
        });
        new Thread(task).start();
    }

    private void getPreviousOperationsData() {
        Task<ObservableList<Operation>> task = new Task<ObservableList<Operation>>() {
            @Override
            protected ObservableList<Operation> call() throws Exception {
                return InpatientDao.getOperations(ViewInpatientController.patient.getAdmissionNumber());
            }
        };
        task.setOnSucceeded(event -> {
            tableView.setItems(task.getValue());
        });
        new Thread(task).start();
    }

    private void setUpTable() {
        Label label = new Label("No previous operations for this patient!");
        label.getStyleClass().add("text-danger");
        tableView.setPlaceholder(label);

        dateCol.setCellValueFactory(param -> DateUtil.dateStringProperty(param.getValue().getDate()));
        timeCol.setCellValueFactory(param -> DateUtil.timeStringProperty(param.getValue().getTime()));
        operationCol.setCellValueFactory(param -> param.getValue().operationProperty());
        surgeonCol.setCellValueFactory(param -> param.getValue().surgeonProperty());
        optionsCol.setCellFactory(param -> new TableCell<Operation, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                int index = getIndex();
                if (index >= 0 && index < tableView.getItems().size()) {
                    Button button = new Button("View Operation");
                    button.getStyleClass().add("btn-info-outline");
                    button.setOnAction(event -> {
                        viewOperation(tableView.getItems().get(index));
                    });
                    setGraphic(button);
                } else {
                    setGraphic(null);
                }
            }
        });
    }

    private void viewOperation(Operation operation) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("main/resources/view/view-operation-notes.fxml"));
            Stage stage = new Stage();
            stage.setScene(new Scene(loader.load()));
            stage.initModality(Modality.WINDOW_MODAL);

            ViewOperationController controller = loader.getController();
            controller.setStage(stage);
            controller.setOperation(operation);

            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onSaveNotes() {
        if (validInput()) {
            Operation operation = createOperation();
            if (DBUtil.saveOperation(operation)) {
                AlertUtil.showAlert("New Operation", "Notes successfully saved", Alert.AlertType.INFORMATION);
                tableView.getItems().add(0, operation);
                clearFields();
            } else {
                AlertUtil.showAlert("Error", "An error occurred while attempting to save notes!", Alert.AlertType.ERROR);
            }
        }
    }

    private void clearFields() {
        operationName.clear();
        indication.clear();
        procedure.clear();
        surgeon.clear();
        assistants.clear();
        anaesthesia.clear();
        anaesthetist.clear();
        procedure.clear();
    }

    private boolean validInput() {
        String errorMsg = "";
        if (operationName.getText() == null || operationName.getText().isEmpty()) {
            errorMsg += "Operation name required!\n";
        }
        if (surgeon.getText() == null || surgeon.getText().isEmpty()) {
            errorMsg += "Surgeon name is required!\n";
        }
        if (errorMsg.isEmpty()) {
            return true;
        }
        AlertUtil.showAlert("Input Error(s)", errorMsg, Alert.AlertType.ERROR);
        return false;
    }

    private Operation createOperation() {
        Operation operation = new Operation();
        LocalDate localDate = datePicker.getValue() == null ? LocalDate.now() : datePicker.getValue();
        operation.setDate(localDate);
        operation.setTime(timePicker.getSelectedTime());
        operation.setProcedure(procedure.getText());
        operation.setIncision(incision.getText());
        operation.setIndication(indication.getText());
        operation.setOperation(operationName.getText());
        operation.setAssistants(assistants.getText());
        operation.setAnaesthesia(anaesthesia.getText());
        operation.setAnaesthetist(anaesthetist.getText());
        operation.setSurgeon(surgeon.getText());
        operation.setAdmissionNum(ViewInpatientController.patient.getAdmissionNumber());

        return operation;
    }
}
