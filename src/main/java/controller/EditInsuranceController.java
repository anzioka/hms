package main.java.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import main.java.model.Insurance;
import main.java.util.AlertUtil;
import main.java.util.DBUtil;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by alfonce on 22/07/2017.
 */
public class EditInsuranceController {

    @FXML
    private TextField name;

    @FXML
    private ChoiceBox<String> insuranceGroupChoiceBox;

    @FXML
    private CheckBox assignInsuranceCheckBox;

    @FXML
    private Button deleteButton;

    private Stage stage;
    private InsuranceManagementController context;

    private Insurance insurance;

    @FXML
    public void initialize() {
        configureGroupChoiceBox();

        assignInsuranceCheckBox.setSelected(true);
        assignInsuranceCheckBox.setOnAction(event -> {
            if (!assignInsuranceCheckBox.isSelected()) {
                insuranceGroupChoiceBox.setValue(null);
            } else {
                if (insurance != null) {
                    insuranceGroupChoiceBox.setValue(insurance.getGroup());
                }
            }
        });
    }

    private void configureGroupChoiceBox() {
        String sql = "SELECT Name from InsuranceGroups";
        try {
            ResultSet resultSet = DBUtil.executeQuery(sql);
            if (resultSet != null) {
                while (resultSet.next()) {
                    insuranceGroupChoiceBox.getItems().add(resultSet.getString("Name"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onDelete() {
        if (insurance != null) {
            String sql = "delete from insurance where name ='" + insurance.getName() + "'";
            if (DBUtil.executeStatement(sql)) {
                AlertUtil.showAlert("Delete", "Insurance '" + insurance.getName() + "' has been successfully deleted" +
                        ".", Alert.AlertType.INFORMATION);
                context.handleDelete(insurance);
                stage.close();
            }
        }
    }

    @FXML
    private void saveDetails() {
        if (validInput()) {

            if (insurance == null) {
                //create a new insurance co
                Insurance insurance = new Insurance();
                insurance.setName(name.getText());

                if (DBUtil.addInsurances(FXCollections.observableArrayList(insurance))) {
                    AlertUtil.showAlert("New Insurance", "Insurance company has been successfully saved!", Alert
                            .AlertType.INFORMATION);

                    context.handleNewInsurance(insurance);
                    stage.close();

                } else {
                    AlertUtil.showAlert("Error", "An error occurred while attempting to add new insurance", Alert
                            .AlertType.ERROR);
                }

            } else {
                String initialName = insurance.getName();
                insurance.setName(name.getText());
                insurance.setGroup(insuranceGroupChoiceBox.getSelectionModel().getSelectedItem());
                if (DBUtil.updateInsurance(insurance, initialName)) {
                    AlertUtil.showAlert("Edit Insurance", "Changes have been successfully saved!", Alert.AlertType.INFORMATION);
                    context.refreshData();
                    stage.close();
                } else {
                    AlertUtil.showAlert("Error", "An error occurred while attempting to save changes", Alert.AlertType.ERROR);
                }
            }

            String selectedItem = insuranceGroupChoiceBox.getSelectionModel().getSelectedItem();
            if (selectedItem != null) {
                insurance.setGroup(selectedItem);
            }

        }
    }

    private boolean validInput() {
        if (name.getText() == null || name.getText().isEmpty()) {
            AlertUtil.showAlert("Error", "Name of insurance company is required.", Alert.AlertType.ERROR);
            return false;
        }
        return true;
    }

    void setInsuranceCompany(Insurance insurance) {
        this.insurance = insurance;
        name.setText(insurance.getName());
        insuranceGroupChoiceBox.setValue(insurance.getGroup());
        deleteButton.setVisible(true);
    }

    void setStage(Stage stage) {
        this.stage = stage;
    }

    void setContext(InsuranceManagementController context) {
        this.context = context;
    }
}
