package main.java.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import main.Main;
import main.java.dao.DiagnosisDAO;
import main.java.model.Diagnosis;
import main.java.model.ICD10_Diagnosis;
import main.java.model.PatientCategory;
import main.java.util.AlertUtil;
import main.java.util.DBUtil;
import main.java.util.DateUtil;
import org.controlsfx.control.textfield.AutoCompletionBinding;
import org.controlsfx.control.textfield.TextFields;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Optional;

public class DiagnosisController {
    @FXML
    private TableView<Diagnosis> tableView;
    @FXML
    private TableColumn<Diagnosis, String> date, diagnosis, option;
    @FXML
    private TextField editField;
    private int visitId;
    private PatientCategory patientCategory;
    private ICD10_Diagnosis icd10_diagnosis;
    @FXML
    private void initialize() {
        setUpAutoComplete();
        setUpTable();
    }

    private void setUpTable() {
        date.setCellValueFactory(param -> DateUtil.dateStringProperty(param.getValue().getDateCreated()));
        date.prefWidthProperty().bind(tableView.widthProperty().divide(4));
        diagnosis.setCellValueFactory(param -> param.getValue().nameProperty());
        diagnosis.prefWidthProperty().bind(tableView.widthProperty().divide(2));
        option.prefWidthProperty().bind(tableView.widthProperty().divide(4));
        option.setCellFactory(param -> new TableCell<Diagnosis, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                int index = getIndex();
                if (index >= 0 && index < tableView.getItems().size()) {
                    Button button = new Button("Delete");
                    button.getStyleClass().add("btn-danger-outline");
                    Diagnosis diagnosis = tableView.getItems().get(index);
                    if (diagnosis.getUserId() != Main.currentUser.getUserId()) {
                        button.setDisable(true);
                    }
                    button.setOnAction(event -> {
                        if (deleteDiagnosis(diagnosis)) {
                            tableView.getItems().remove(index);
                        }
                    });
                    setGraphic(button);
                } else {
                    setGraphic(null);
                }
            }
        });
    }

    private boolean deleteDiagnosis(Diagnosis diagnosis) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Diagnosis");
        alert.setContentText("Are you sure you want to delete diagnosis '" + diagnosis.getName() + "'?");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            String sql = "delete from diagnosis where id = " + diagnosis.getId();
            return DBUtil.executeStatement(sql);
        }
        return false;
    }

    private void getPreviousDiagnosis() {
        Task<ObservableList<Diagnosis>> task = new Task<ObservableList<Diagnosis>>() {
            @Override
            protected ObservableList<Diagnosis> call() throws Exception {
                return DiagnosisDAO.getDiagnoses(visitId, patientCategory);
            }
        };
        task.setOnSucceeded(event -> {
            tableView.setItems(task.getValue());
        });
        new Thread(task).start();
    }

    private void setUpAutoComplete() {
        Task<ObservableList<ICD10_Diagnosis>> task = new Task<ObservableList<ICD10_Diagnosis>>() {
            @Override
            protected ObservableList<ICD10_Diagnosis> call() throws Exception {
                return DiagnosisDAO.getDiagnosisList();
            }
        };
        task.setOnSucceeded(event -> {
            AutoCompletionBinding<ICD10_Diagnosis> binding = TextFields.bindAutoCompletion(editField, task.getValue());
            binding.setOnAutoCompleted(e -> {
                if (e.getCompletion() != null) {
                    icd10_diagnosis = e.getCompletion();
                }
            });
            binding.prefWidthProperty().bind(editField.widthProperty());
        });
        new Thread(task).start();
    }

    @FXML
    private void onAddDiagnosis() {
        if (editField.getText() != null && !editField.getText().isEmpty() ) {
            Diagnosis diagnosis = createDiagnosis();
            if (DBUtil.saveDiagnosis(diagnosis)) {
                AlertUtil.showAlert("New Diagnosis", "Diagnosis successfully saved", Alert.AlertType.INFORMATION);
                editField.clear();
                tableView.getItems().add(diagnosis);
                icd10_diagnosis = null;
            } else {
                AlertUtil.showAlert("New Diagnosis", "An error occurred while attempting to save diagnosis", Alert.AlertType.ERROR);
            }
        } else {
            AlertUtil.showAlert("New Diagnosis", "Diagnosis field cannot be blank!\n", Alert.AlertType.ERROR);
        }
    }

    private Diagnosis createDiagnosis() {
        Diagnosis diagnosis = new Diagnosis();
        if (icd10_diagnosis == null) {
            icd10_diagnosis = new ICD10_Diagnosis(getUnlistedDiagnosisCode(), editField.getText());
            DBUtil.saveDiseases(FXCollections.observableArrayList(icd10_diagnosis));
        }
        diagnosis.setName(icd10_diagnosis.getName());
        diagnosis.setCode(icd10_diagnosis.getCode());

        diagnosis.setUserId(Main.currentUser.getUserId());
        diagnosis.setUser(Main.currentUser.getFirstName());
        diagnosis.setDateCreated(LocalDate.now());
        diagnosis.setId(DBUtil.getNextAutoIncrementId("diagnosis"));
        if (patientCategory == PatientCategory.INPATIENT) {
            diagnosis.setAdmissionNum(visitId);
        } else{
            diagnosis.setVisitId(visitId);
        }
        return diagnosis;
    }

    private String getUnlistedDiagnosisCode() {
        ResultSet resultSet = DBUtil.executeQuery("select count(*) as count from icd10_diagnoses");
        try {
            if (resultSet != null && resultSet.next()) {
                return resultSet.getString("count");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void setParameters(int admissionNumber, PatientCategory patientCategory) {
        this.visitId = admissionNumber;
        this.patientCategory = patientCategory;

        getPreviousDiagnosis();
    }
}
