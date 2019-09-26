package main.java.controller;

import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import main.Main;
import main.java.dao.UsersDAO;
import main.java.model.Permission;
import main.java.model.User;

import java.io.IOException;

/**
 * Created by alfonce on 29/06/2017.
 */
public class ManageUsersController {
    private static Stage stage;
    @FXML
    private TableView<User> userTableView;

    @FXML
    private TableColumn<User, String> firstNameCol, surnameCol, loginNameCol, optionsCol, categoryCol;

    @FXML
    private VBox container;

    static void setStage(Stage stage) {
        ManageUsersController.stage = stage;
    }

    @FXML
    public void initialize() {
        //table
        initializeUsersTable();
        getUsers();
    }

    private void getUsers() {
        //set users
        Task<ObservableList<User>> task = new Task<ObservableList<User>>() {
            @Override
            protected ObservableList<User> call() {
                return UsersDAO.getUserObservableList("select * from users where ID != 1");
            }
        };
        task.setOnSucceeded(event -> {
            userTableView.setItems(task.getValue());
        });
        new Thread(task).start();

    }

    private void initializeUsersTable() {
        //place holder for empty table
        Label label = new Label("No users found!");
        label.getStyleClass().add("text-danger");
        userTableView.setPlaceholder(label);

        //columns
        firstNameCol.setCellValueFactory(param -> param.getValue().firstNameProperty());
        surnameCol.setCellValueFactory(param -> param.getValue().lastNameProperty());
        loginNameCol.setCellValueFactory(param -> param.getValue().loginNameProperty());
        categoryCol.setCellValueFactory(param -> param.getValue().categoryProperty());
        optionsCol.setCellFactory(param -> new TableCell<User, String>() {
            @Override
            public void updateItem(String item, boolean empty) {
                int index = getIndex();
                if (index >= 0 && index < userTableView.getItems().size()) {
                    Button edit = new Button("Edit");
                    edit.getStyleClass().add("btn-info-outline");
                    edit.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
                        showEditUserDialog(userTableView.getItems().get(index));
                    });
                    edit.setDisable(!Main.userPermissions.get(Permission.CREATE_USERS));

                    Button activity = new Button("Activity Log");
                    activity.getStyleClass().add("btn-info-outline");
                    activity.setOnAction(event -> {
                        showActivityLog(userTableView.getItems().get(index));
                    });
                    activity.setDisable(Main.currentUser.getUserId() != userTableView.getItems().get(index).getUserId() && !Main.userPermissions.get(Permission.VIEW_ACTIVITY));
                    HBox hBox = new HBox(5.0, activity, edit);
                    hBox.setAlignment(Pos.CENTER);
                    setGraphic(hBox);
                } else {
                    setGraphic(null);
                }
            }
        });

    }

    private void showActivityLog(User user) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("main/resources/view/user-activity.fxml"));
            Stage stage = new Stage();
            stage.setScene(new Scene(loader.load()));
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(container.getScene().getWindow());
            UserActivityController controller = loader.getController();
            controller.setUser(user);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showEditUserDialog(User user) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("main/resources/view/edit_user" +
                    ".fxml"));
            Parent node = loader.load();

            //stage
            Stage stage = new Stage();
            stage.setScene(new Scene(node));
            stage.setResizable(false);
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(container.getScene().getWindow());

            //controller
            EditUserController controller = loader.getController();
            controller.setStage(stage);
            controller.setUser(user);
            controller.setContext(this);
            stage.showAndWait();
            getUsers();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    boolean containsUserName(String loginName) {
        for (User user : userTableView.getItems()) {
            if (user.getLoginName().equals(loginName)) {
                return true;
            }
        }
        return false;
    }

    @FXML
    public void onNewUser() {
        showEditUserDialog(null);
    }
}
