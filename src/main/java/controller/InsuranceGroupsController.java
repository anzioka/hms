package main.java.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxListCell;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.VBox;
import javafx.util.converter.DoubleStringConverter;
import main.java.dao.InsuranceDAO;
import main.java.model.Insurance;
import main.java.model.InsuranceGroup;
import main.java.util.AlertUtil;
import main.java.util.DBUtil;
import main.java.util.NumberUtil;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by alfonce on 22/07/2017.
 */
public class InsuranceGroupsController {

    @FXML
    private Label nameOfGroupLabel;

    @FXML
    private TextField groupName, feeAmount;

    @FXML
    private TableView<Insurance> assignedItemsTableView;

    @FXML
    private TableColumn<Insurance, String> insuranceCol, assignedTableOptions;

    @FXML
    private TableView<InsuranceGroup> groupTableView;

    @FXML
    private TableColumn<InsuranceGroup, String> groupNameCol;

    @FXML
    private TableColumn<InsuranceGroup, Double> consultationFeeCol;

    @FXML
    private VBox newGroupSelector, groupEditContainer, assignedItemsContainer;

    @FXML
    private ListView<Insurance> insuranceListView;

    @FXML
    private ChoiceBox<Insurance> insuranceChoiceBox;

    private InsuranceGroup newGroup = null;
    private InsuranceManagementController context;

    @FXML
    public void initialize() {
        groupEditContainer.setVisible(false);
        configureAssignedItemsTable();
        configureGroupsTable();
        getAllGroups();
        insuranceListView.setCellFactory(CheckBoxListCell.forListView(Insurance::isAssignedProperty));

    }

    private void getAllGroups() {
        ObservableList<InsuranceGroup> list = FXCollections.observableArrayList();
        String sql = "SELECT * FROM InsuranceGroups";
        try {
            ResultSet resultSet = DBUtil.executeQuery(sql);
            if (resultSet != null) {
                while (resultSet.next()) {
                    InsuranceGroup group = new InsuranceGroup();
                    group.setConsultationFee(resultSet.getDouble("ConsultationFee"));
                    group.setName(resultSet.getString("Name"));
                    list.add(group);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        groupTableView.setItems(list);
    }

    private void configureGroupsTable() {
        //place holder
        Label label = new Label("No groups found!");
        label.getStyleClass().add("text-danger");
        groupTableView.setPlaceholder(label);

        groupNameCol.setCellValueFactory(param -> param.getValue().nameProperty());
        consultationFeeCol.setCellValueFactory(param -> param.getValue().consultationFeeProperty().asObject());

        groupTableView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                showItemsInGroup(newValue);
            }
        });

        //editing
        groupNameCol.setCellFactory(TextFieldTableCell.forTableColumn());
        groupNameCol.setOnEditCommit(event -> {
            if (event.getNewValue() != null && !event.getNewValue().isEmpty()) {
                InsuranceGroup group = event.getTableView().getItems().get(event.getTablePosition().getRow());
                if (group != null) {
                    String sql = "Update InsuranceGroups set Name = '" + event.getNewValue() + "' " +
                            "WHERE name = '" + group.getName() + "'";
                    DBUtil.executeStatement(sql);
                    group.setName(event.getNewValue());
                    updateGroupNameInfo(group);
                    nameOfGroupLabel.setText(group.getName());
                    context.getData();
                }
            }
        });
        consultationFeeCol.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter()));
        consultationFeeCol.setOnEditCommit(event -> {
            InsuranceGroup group = event.getTableView().getItems().get(event.getTablePosition().getRow());
            if (group != null) {
                if (event.getNewValue() == (double) event.getNewValue()) {
                    String sql = "Update InsuranceGroups set ConsultationFee = " + event.getNewValue() + " WHERE " +
                            "Name = '" + group.getName() + "'";
                    DBUtil.executeStatement(sql);
                }
            }
        });
    }

    private void updateGroupNameInfo(InsuranceGroup group) {
        List<String> items = new ArrayList<>();
        for (Insurance insurance : assignedItemsTableView.getItems()) {
            items.add(insurance.getName());
        }
        DBUtil.addItemsToInsuranceGroup(group.getName(), items);
    }

    private void showItemsInGroup(InsuranceGroup newValue) {
        nameOfGroupLabel.setText(newValue.getName());
        setNewGroupSelectorMode(false);
        getAssignedItems(newValue);
        configureChoiceBox(newValue);
    }

    private void configureChoiceBox(InsuranceGroup newValue) {
        String sql = "SELECT * from Insurance " +
                "WHERE NOT InsuranceGroup = '" + newValue.getName() + "' OR " +
                "InsuranceGroup is NULL";
        insuranceChoiceBox.setItems(InsuranceDAO.getList(sql));
        insuranceChoiceBox.getSelectionModel().clearSelection();
    }

    private void getAssignedItems(InsuranceGroup newValue) {
        String sql = "SELECT * FROM Insurance " +
                "WHERE InsuranceGroup = '" + newValue.getName() + "'";
        ObservableList<Insurance> list = InsuranceDAO.getList(sql);
        if (list.isEmpty()) {
            newGroup = newValue;
            setNewGroupSelectorMode(true);
            configureNewGroupListSelector();
            assignedItemsTableView.getItems().clear();
        } else {
            assignedItemsTableView.setItems(list);
        }
    }

    private void configureNewGroupListSelector() {
        //place holder
        ObservableList<Insurance> list = FXCollections.observableArrayList();
        String sql = "SELECT * From Insurance";
        try {
            ResultSet resultSet = DBUtil.executeQuery(sql);
            if (resultSet != null) {
                while (resultSet.next()) {
                    Insurance insurance = new Insurance();
                    insurance.setIsAssigned(false);
                    insurance.setName(resultSet.getString("Name"));
                    list.add(insurance);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        insuranceListView.setItems(list);

    }

    private void configureAssignedItemsTable() {
        //place holder
        Label label = new Label("No items found in group!");
        label.getStyleClass().add("text-danger");
        assignedItemsTableView.setPlaceholder(label);

        insuranceCol.setCellValueFactory(param -> param.getValue().nameProperty());
        assignedTableOptions.setCellFactory(param -> new TableCell<Insurance, String>() {
            @Override
            public void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                int index = getIndex();
                if (index >= 0 && index < assignedItemsTableView.getItems().size()) {
                    Button button = new Button("Remove");
                    button.getStyleClass().add("text-danger");

                    button.setOnAction(event -> {
                        removeItemFromGroup(assignedItemsTableView.getItems().get(index));
                    });
                    setGraphic(button);
                } else {
                    setGraphic(null);
                }
            }
        });
    }

    private void removeItemFromGroup(Insurance insurance) {
        String sql = "update insurance set InsuranceGroup = NULL where Name = '" + insurance.getName() + "'";
        if (DBUtil.executeStatement(sql)) {
            assignedItemsTableView.getItems().remove(insurance);
        }

        context.getData();
    }

    @FXML
    private void addItemsToGroup() {
        if (newGroup == null) {
            return;
        }
        List<String> insuranceNames = new ArrayList<>();
        for (Insurance insurance : insuranceListView.getItems()) {
            if (insurance.isIsAssigned()) {
                insuranceNames.add(insurance.getName());
            }
        }

        if (DBUtil.addItemsToInsuranceGroup(newGroup.getName(), insuranceNames)) {
            AlertUtil.showAlert("Group : " + newGroup, insuranceNames.size() + " companies have been assigned to " +
                    "group '" +
                    newGroup +
                    "'", Alert.AlertType.INFORMATION);
            groupTableView.getSelectionModel().select(newGroup);

        }
        context.getData();
    }

    @FXML
    private void addItemToGroup() {
        InsuranceGroup group = groupTableView.getSelectionModel().getSelectedItem();
        Insurance insurance = insuranceChoiceBox.getSelectionModel().getSelectedItem();
        if (insurance != null && group != null) {
            if (!assignedItemsTableView.getItems().contains(insurance)) {
                String sql = "Update Insurance set InsuranceGroup = '" + group.getName() + "' " +
                        "WHERE Name = '" + insurance.getName() + "'";
                if (DBUtil.executeStatement(sql)) {
                    assignedItemsTableView.getItems().add(insurance);
                    configureChoiceBox(group);

                }
            }
        }

        context.getData();
    }

    @FXML
    private void createGroup() {
        if (validInput()) {
            newGroup = new InsuranceGroup();
            newGroup.setName(groupName.getText());
            newGroup.setConsultationFee(NumberUtil.stringToDouble(feeAmount.getText()));

            if (DBUtil.addInsuranceGroup(newGroup)) {

                groupTableView.getSelectionModel().clearSelection();

                groupName.setText(null);
                feeAmount.setText(null);

                groupTableView.getItems().add(newGroup);
                nameOfGroupLabel.setText(newGroup.getName());
                setNewGroupSelectorMode(true);
                configureNewGroupListSelector();
            }
        }
    }

    private void setNewGroupSelectorMode(boolean selectorMode) {
        groupEditContainer.setVisible(true);
        newGroupSelector.setVisible(selectorMode);
        newGroupSelector.setManaged(selectorMode);

        assignedItemsContainer.setVisible(!selectorMode);
        assignedItemsContainer.setManaged(!selectorMode);
    }

    private boolean validInput() {
        String errorMsg = "";

        if (groupName.getText() == null || groupName.getText().isEmpty()) {
            errorMsg += "Group name required!\n";
        }
        InsuranceGroup group = new InsuranceGroup();
        group.setName(groupName.getText());
        if (groupTableView.getItems().contains(group)) {
            errorMsg += "Group already exists!\n";
        }

        double fee = NumberUtil.stringToDouble(feeAmount.getText());
        if (fee == -1) {
            errorMsg += "Invalid consultation fee (only numbers allowed)\n";
        }

        if (errorMsg.isEmpty()) {
            return true;
        }
        AlertUtil.showAlert("Error", errorMsg, Alert.AlertType.ERROR);
        return false;
    }

    @FXML
    private void onDeleteGroup() {

        InsuranceGroup group = groupTableView.getSelectionModel().getSelectedItem();
        if (group != null) {
            String sql = "delete from InsuranceGroups " +
                    "where name = '" + group.getName() + "'";
            if (DBUtil.executeStatement(sql)) {
                if (DBUtil.unassignGroup(assignedItemsTableView.getItems())) {
                    AlertUtil.showAlert("Delete '" + group + "'", "Group has been successfully deleted", Alert.AlertType
                            .INFORMATION);
                    groupEditContainer.setVisible(false);
                    groupTableView.getItems().remove(group);
                    groupTableView.getSelectionModel().clearSelection();
                    context.getData();
                }
            }

            context.getData();
        }

    }

    void setContext(InsuranceManagementController context) {
        this.context = context;
    }
}
