package main.java.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import main.Main;
import main.java.dao.UsersDAO;
import main.java.model.User;
import main.java.model.UserCategory;
import main.java.util.AlertUtil;
import main.java.util.DBUtil;

import java.time.LocalDate;

/**
 * Created by alfonce on 19/04/2017.
 */
public class LoginController {
    private static final int ROOT_USER_ID = 1;
    private static final String ROOT_USER = "root";
    @FXML
    private PasswordField userPassword;
    @FXML
    private TextField userName;
    @FXML
    private Button loginButton;
    private Main main;
    private boolean loginSuccessful = false;
    private Stage stage;

    @FXML
    public void initialize() {

        userName.textProperty().addListener((observable, oldValue, newValue) -> activateLoginButton(newValue, userPassword.getText()));

        userPassword.textProperty().addListener((observable, oldValue, newValue) -> activateLoginButton(userName.getText
                (), newValue));
    }

    private void activateLoginButton(String userName, String password) {
        loginButton.setDisable(userName == null || userName.isEmpty() || password == null || password.isEmpty());
    }

    @FXML
    private void onLogin() {
        if (DBUtil.getConnection(userName.getText(), userPassword.getText())) {
            User user;
            if (userName.getText().equals(ROOT_USER)) {
                user = createAdminUser();
            } else {
                String getUserSql = "select * from hmsdb.users where UserName = '" + userName.getText() + "'";
                user = UsersDAO.getUser(getUserSql);
            }

            if (user != null) {
                AlertUtil.showAlert("Login Successful", "You have been successfully logged in.", Alert.AlertType.INFORMATION);
                Main.setCurrentUser(user);
                Main.setUserPermissions(UsersDAO.getUserPermissionsMap(user.getUserId()));
                Main.setUserModules(UsersDAO.getUserModules(user.getUserId()));
                DBUtil.saveActivity("Logged in");
                loginSuccessful = true;
                stage.close();

            } else {
                AlertUtil.showAlert("Error", "The user name you provided does not exist", Alert.AlertType.ERROR);
            }
        } else {
            AlertUtil.showAlert("Login Error", "An error occurred while attempting to log you in", Alert.AlertType.ERROR);
        }
    }

    private User createAdminUser() {
        User user = new User();
        user.setUserId(1);
        user.setPassword(userPassword.getText());
        user.setLoginName(userName.getText());
        user.setLastName("");
        user.setFirstName("Admin");
        user.setCategory(UserCategory.ADMIN);
        user.setDateCreated(LocalDate.now());
        return user;
    }

    public boolean loginSuccessful() {
        return loginSuccessful;
    }

    Stage getStage() {
        return stage;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }
}
