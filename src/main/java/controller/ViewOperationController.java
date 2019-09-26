package main.java.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import main.java.model.Operation;
import main.java.util.DateUtil;

public class ViewOperationController {
    private Stage stage;
    @FXML
    private Label operationName, date, indication, surgeon, assistants, anaesthetist, anaesthesia, notes, incision;

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void setOperation(Operation operation) {
        indication.setText(operation.getIndication());
        surgeon.setText(operation.getSurgeon());
        assistants.setText(operation.getAssistants());
        anaesthesia.setText(operation.getAnaesthesia());
        anaesthetist.setText(operation.getAnaesthetist());
        operationName.setText(operation.getOperation());
        date.setText(DateUtil.formatDateLong(operation.getDate()) + " " + DateUtil.formatTime(operation.getTime()));
        notes.setText(operation.getProcedure());
        incision.setText(operation.getIncision());
    }

    @FXML
    private void onClose() {
        stage.close();
    }
}
