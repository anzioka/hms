package main.java.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import main.java.model.HospitalProcedure;
import main.java.util.AlertUtil;
import main.java.util.DBUtil;
import main.java.util.NumberUtil;

import java.util.Optional;

/**
 * Created by alfonce on 01/08/2017.
 */
public class EditProcedureController {
    private Stage stage;
    private HospitalProcedure procedure;

    @FXML
    private TextField name, cost;
    private ProceduresController context;

    void setProcedure(HospitalProcedure procedure) {
        this.procedure = procedure;
        name.setText(procedure.getName());
        cost.setText(procedure.getCost() + "");
    }

    void setStage(Stage stage) {
        this.stage = stage;
    }

    @FXML
    private void onDelete() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Procedure");
        alert.setContentText("Are you sure you want to remove this procedure?");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            String delete = "delete from Procedures where name = '" + procedure.getName() + "'";
            if (DBUtil.executeStatement(delete)) {
                AlertUtil.showAlert("Delete Procedure", "Procedure '" + procedure.getName() + "' has been successfully" +
                        " removed from the database", Alert.AlertType.INFORMATION);
                context.handleDelete(procedure);
                stage.close();
            } else {
                AlertUtil.showAlert("Error", "An error occurred while attempting to delete procedure", Alert.AlertType.ERROR);
            }
        }
        stage.close();
    }

    @FXML
    private void onSave() {
        if (validInput()) {
            String initialName = procedure.getName();
            procedure.setName(name.getText());
            procedure.setCost(NumberUtil.stringToDouble(cost.getText()));

            if (DBUtil.updateProcedure(procedure, initialName)) {
                AlertUtil.showAlert("Procedure Updated", "Changes have been successfully saved!", Alert.AlertType.INFORMATION);
                stage.close();
            } else {
                AlertUtil.showAlert("Error", "An error occurred while trying to save changes", Alert.AlertType.ERROR);
            }
        }
    }

    private boolean validInput() {
        String errorMsg = "";
        if (errorMsg.isEmpty()) {
            return true;
        }
        if (name.getText() == null || name.getText().isEmpty()) {
            errorMsg += "Procedure name can't be blank!\n";
        }
        double costVal = NumberUtil.stringToDouble(cost.getText());
        if (costVal == -1) {
            errorMsg += "Cost value is invalid (only numbers are accepted)\n";
        }
        AlertUtil.showAlert("Input Error(s)", errorMsg, Alert.AlertType.ERROR);
        return false;
    }

    void setContext(ProceduresController context) {
        this.context = context;
    }
}
