package main;

import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import main.java.controller.LoginController;
import main.java.controller.MainController;
import main.java.dao.UsersDAO;
import main.java.model.*;
import main.java.util.DBUtil;
import main.java.util.LoggerUtil;

import java.io.IOException;
import java.util.Map;
import java.util.logging.Logger;

public class Main extends Application {

    public static final Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    private static final String APP_TITLE = "Quantum Hospital Management System";
    public static Stage stage;
    public static ObservableList<User> users;
    public static User currentUser;
    public static Map<Permission, Boolean> userPermissions;
    public static ObservableList<UserModule> userModules;
    public static VBox root;
    private AnchorPane mainContent;

    public static void main(String[] args) {
        launch(args);
    }

    public static void setCurrentUser(User user) {

        currentUser = user;

        userModules = UsersDAO.getUserModules(currentUser.getUserId());
        if (userModules.isEmpty() && currentUser.getCategory() == UserCategory.ADMIN) {
            for (Module module : Module.values()) {
                userModules.add(new UserModule(module, true));
            }
        }
    }

    public static void setUserPermissions(Map<Permission, Boolean> userPermissions) {
        Main.userPermissions = userPermissions;
    }

    public static void setUserModules(ObservableList<UserModule> userModules) {
        Main.userModules = userModules;
    }

    @Override
    public void start(Stage primaryStage) {
        stage = primaryStage;

        if (loginSuccessful()) {
            showMainContent();
        }

        //init Logger
        try {
            LoggerUtil.setup();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showMainContent() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("resources/view/root_layout" +
                    ".fxml"));
            root = loader.load();

            SplitPane splitPane = (SplitPane) root.getChildren().get(1);
            mainContent = (AnchorPane) splitPane.getItems().get(1);

            //stage
            stage = new Stage();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setMaximized(true);
            stage.setTitle(APP_TITLE);

            MainController controller = loader.getController();
            controller.setApplicationInfo();
            controller.setMainApp(this);

            stage.show();
            loadModule("home_screen");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean loginSuccessful() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("resources/view/login_screen.fxml"));
            Parent node = loader.load();

            //stage
            Stage stage  = new Stage();
            Scene scene = new Scene(node);
            stage.setScene(scene);
            stage.setResizable(false);

            LoginController loginController = loader.getController();
            loginController.setStage(stage);

            stage.showAndWait();
            return loginController.loginSuccessful();

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void logOutUser() {
        DBUtil.saveActivity("Logged out");
        stage.close();
        if (loginSuccessful()) {
            showMainContent();
        }
    }

    public void loadModule(String resourceName) {
        mainContent.getChildren().clear();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("resources/view/" + resourceName + ".fxml"));
            Node node = loader.load();
            mainContent.getChildren().add(node);

            anchorNode(node);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void anchorNode(Node node) {
        AnchorPane.setRightAnchor(node, 0.0);
        AnchorPane.setLeftAnchor(node, 0.0);
        AnchorPane.setBottomAnchor(node, 0.0);
        AnchorPane.setTopAnchor(node, 0.0);
    }

}
