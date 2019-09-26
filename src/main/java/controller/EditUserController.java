package main.java.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxListCell;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;
import main.Main;
import main.java.dao.UsersDAO;
import main.java.model.*;
import main.java.util.AlertUtil;
import main.java.util.DBUtil;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Optional;

/**
 * Created by alfonce on 07/09/2017.
 */
public class EditUserController {

    @FXML
    private Accordion modulesPermissionsAccordion;
    @FXML
    private TitledPane modulesTitledPane;
    @FXML
    private TextField userCode, firstName, lastName, loginName;

    @FXML
    private PasswordField passwordField, confirmPasswordField;

    @FXML
    private ChoiceBox<UserCategory> userCategoryChoiceBox;

    @FXML
    private ListView<UserModule> moduleAccessListView;
    @FXML
    private ListView<UserPermission> userPermissionListView;

    @FXML
    private Button resetPasswordBtn;

    @FXML
    private Button deleteButton;

    private ManageUsersController context;

    private Stage stage;
    private User user;

    @FXML
    public void initialize() {
        modulesPermissionsAccordion.setExpandedPane(modulesTitledPane);

        //accessible modules
        for (Module module : Module.values()) {
            moduleAccessListView.getItems().add(new UserModule(module, false));
        }
        moduleAccessListView.setCellFactory(CheckBoxListCell.forListView(UserModule::allowedProperty));
        moduleAccessListView.setDisable(!Main.userPermissions.get(Permission.SET_PERMISSIONS));

        //user permissions
        for (Permission permission : Permission.values()) {
            userPermissionListView.getItems().add(new UserPermission(permission, false));
        }
        userPermissionListView.setCellFactory(CheckBoxListCell.forListView(UserPermission::allowedProperty));
        userPermissionListView.setDisable(!Main.userPermissions.get(Permission.SET_PERMISSIONS));

        userCategoryChoiceBox.setItems(FXCollections.observableArrayList(UserCategory.values()));
        userCategoryChoiceBox.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.UP || event.getCode() == KeyCode.DOWN) {
                event.consume();
            }
        });
        userCategoryChoiceBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == UserCategory.ADMIN) {
                for (UserModule module : moduleAccessListView.getItems()) {
                    module.setAllowed(true);
                }
                for (UserPermission permission : userPermissionListView.getItems()) {
                    permission.setAllowed(true);
                }
            }
        });
    }

    void setStage(Stage stage) {
        this.stage = stage;
    }

    private void setUserDetails() {
        userCode.setText(user.getUserId() + "");
        firstName.setText(user.getFirstName());
        lastName.setText(user.getLastName());
        userCategoryChoiceBox.setValue(user.getCategory());
        loginName.setText(user.getLoginName());

        userPermissionListView.setItems(UsersDAO.getUserPermissions(user.getUserId()));
        moduleAccessListView.setItems(UsersDAO.getUserModules(user.getUserId()));
    }

    public User getUser() {
        return user;
    }

    void setUser(User user) {
        this.user = user;

        if (user == null) {
            deleteButton.setVisible(false);
            deleteButton.setManaged(false);

            resetPasswordBtn.setVisible(false);
            resetPasswordBtn.setManaged(false);

            userCode.setText(String.valueOf(DBUtil.getNextAutoIncrementId("users")));
        } else {

            passwordField.setDisable(true);
            confirmPasswordField.setDisable(true);
            loginName.setDisable(true);
            setUserDetails();
        }
    }

    @FXML
    private void onSaveUserInfo() {
        if (validInput()) {
            if (user == null) {
                if (!DBUtil.createUser(loginName.getText(), passwordField.getText())) {
                    AlertUtil.showAlert("Create User Error", "An error occurred while attempting to create new user" +
                            "", Alert.AlertType.ERROR);
                    return;
                }
            }
            saveUserDetails();
            DBUtil.saveModuleAccess(moduleAccessListView.getItems(), userCode.getText());
            DBUtil.saveUserPermissions(userPermissionListView.getItems(), userCode.getText());
            if (getCreateUserPermission()) {
                DBUtil.enableCreateUserPermission(loginName.getText());
            }
            //log activity
            if (loginName.isDisabled()) {
                //existing user
                DBUtil.saveActivity("Changed details of user '" + loginName.getText() + "'");
            } {
                DBUtil.saveActivity("Created a new user '" + loginName.getText()  + "'");
            }
            AlertUtil.showAlert("User Saved", "User details have been successfully saved!\n", Alert.AlertType
                    .INFORMATION);

            onCloseDialog();
        }
    }

    private boolean getCreateUserPermission() {
        for (UserPermission userPermission : userPermissionListView.getItems()) {
            if (userPermission.getPermission().equals(Permission.CREATE_USERS)) {
                return userPermission.isAllowed();
            }
        }
        return false;
    }

    private void saveUserDetails() {
        if (user == null) {
            user = new User();
        }
        user.setDateCreated(LocalDate.now());
        user.setUserId(Integer.parseInt(userCode.getText()));
        user.setLoginName(loginName.getText());
        user.setFirstName(firstName.getText());
        user.setLastName(lastName.getText());
        user.setCategory(userCategoryChoiceBox.getValue());
        DBUtil.saveUserDetails(user);
    }

    private boolean validInput() {
        String errorMsg = "";
        if (firstName.getText() == null || firstName.getText().isEmpty()) {
            errorMsg += "First name is required!\n";
        }
        if (lastName.getText() == null || lastName.getText().isEmpty()) {
            errorMsg += "Last name is required!\n";
        }

        if (loginName.getText() == null || loginName.getText().isEmpty()) {
            errorMsg += "Login name is required!\n";
        } else if (user == null && context.containsUserName(loginName.getText())) {
            errorMsg += "Login name already used!\n";
        }

        if (!loginName.isDisabled()) {
            if ( passwordField.getText() == null || passwordField.getText().isEmpty()) {
                errorMsg += "Password is required!\n";
            } else if (!confirmPasswordField.getText().equals(passwordField.getText())) {
                errorMsg += "Passwords do not match!\n";
            }
        }

        if (errorMsg.isEmpty()) {
            return true;
        }
        AlertUtil.showAlert("Input Error(s)", errorMsg, Alert.AlertType.ERROR);
        return false;
    }

    @FXML
    private void onCloseDialog() {
        stage.close();
    }

    @FXML
    private void onResetPassword() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource
                    ("main/resources/view/reset_password.fxml"));
            Parent parent = loader.load();

            //Stage
            Stage stage = new Stage();
            stage.setResizable(false);
            stage.initModality(Modality.WINDOW_MODAL);
            stage.setScene(new Scene(parent));
            stage.setTitle("Reset Password");

            //controller
            ResetPasswordController controller = loader.getController();

            controller.setStage(stage);
            controller.setUser(user);

            //show
            stage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onDeleteUser() {
        //drop user and delete from users table
        //alert dialog
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to delete user '" + user.getLoginName() + "'? You will not be unable to undo this action. ", ButtonType.NO, ButtonType.YES);
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.YES) {
            if (DBUtil.executeStatement("delete from users where Id = " + user.getUserId())) {
                DBUtil.executeStatement("delete from user_modules where UserId = '" + user.getUserId() + "'");
                DBUtil.executeStatement("delete from user_permissions where UserId = '" + user.getUserId() + "'");
                DBUtil.executeStatement("drop user '" + user.getLoginName() + "'@'localhost'");

                DBUtil.saveActivity("Deleted user " + user.getDescription());
                AlertUtil.showAlert("", "Successfully deleted user '" + user.getLoginName() + "'", Alert.AlertType.INFORMATION);
                stage.close();
            } else {
                AlertUtil.showGenericError();
            }
        }

    }

    public void setContext(ManageUsersController context) {
        this.context = context;
    }
}
