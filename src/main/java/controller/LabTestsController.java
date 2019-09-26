package main.java.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import main.Main;
import main.java.dao.LabTestDAO;
import main.java.model.LabTest;
import main.java.model.LabTestFlag;
import main.java.model.Permission;
import main.java.util.CurrencyUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by alfonce on 01/08/2017.
 */
public class LabTestsController {

    @FXML
    private TextField testFilter;
    @FXML
    private AnchorPane container;
    @FXML
    private TableView<LabTest> tableView;
    @FXML
    private Button newLabTestBtn;
    @FXML
    private TableColumn<LabTest, String> name, cost, flags, options, flagName, flagRange;
    private ObservableList<LabTest> mainLabTestsList = FXCollections.observableArrayList();
    private Map<String, List<LabTestFlag>> labTestFlagMap = new HashMap<>();
    private ObservableList<LabTestFlag> labTestFlags = FXCollections.observableArrayList();

    @FXML
    public void initialize() {

        Task<ObservableList<LabTest>> task = new Task<ObservableList<LabTest>>() {
            @Override
            protected ObservableList<LabTest> call() {
                mainLabTestsList = LabTestDAO.getAllLabTests();
                labTestFlags = LabTestDAO.getLabTestFlags();
                for (LabTest labTest : mainLabTestsList) {
                    labTestFlagMap.put(labTest.getName(), getFlagsForLabTest(labTest.getName()));
                }
                return null;
            }
        };

        task.setOnSucceeded(event -> {

            //filter
            tableView.setItems(mainLabTestsList);
            testFilter.textProperty().addListener((observable, oldValue, newValue) -> filterResults(newValue.toLowerCase
                    ()));
        });
        setUpTable();
        new Thread(task).start();
        newLabTestBtn.setDisable(!Main.userPermissions.get(Permission.EDIT_LAB_TESTS));

    }

    private void setUpTable() {
        for (TableColumn column : tableView.getColumns()) {
            if (column == flags) {
                column.prefWidthProperty().bind(tableView.widthProperty().divide(2.5));
            } else {
                column.prefWidthProperty().bind(tableView.widthProperty().divide(5));
            }
        }
        //placeholder
        Label label = new Label("No lab tests found!");
        label.getStyleClass().add("text-danger");
        tableView.setPlaceholder(label);

        name.setCellValueFactory(param -> param.getValue().nameProperty());
        cost.setCellValueFactory(param -> CurrencyUtil.getStringProperty(param.getValue().getCost()));
        options.setCellFactory(param -> new TableCell<LabTest, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                int index = getIndex();
                if (index >= 0 && index < tableView.getItems().size()) {

                    Button button = new Button("Edit");
                    button.getStyleClass().add("btn-info-outline");
                    button.setOnAction(event -> {
                        editTest(tableView.getItems().get(index));
                    });
                    button.setDisable(!Main.userPermissions.get(Permission.EDIT_LAB_TESTS));
                    setGraphic(button);

                } else {
                    setGraphic(null);
                }
            }
        });
        flagName.setCellFactory(param -> new TableCell<LabTest, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                int index = getIndex();
                if (index >= 0 && index < tableView.getItems().size()) {
                    List<LabTestFlag> labTestFlags = labTestFlagMap.get(tableView.getItems().get(index).getName());
                    VBox box = new VBox(5.0);
                    box.setAlignment(Pos.CENTER);
                    box.setPadding(new Insets(5, 0, 5, 0));
                    for (LabTestFlag flag : labTestFlags) {
                        box.getChildren().add(new Label(flag.getName()));
                        Separator separator = new Separator(Orientation.HORIZONTAL);
                        box.getChildren().add(separator);
                    }
                    setGraphic(box);
                } else {
                    setGraphic(null);
                }
            }
        });
        flagRange.setCellFactory(param -> new TableCell<LabTest, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                int index = getIndex();
                if (index >= 0 && index < tableView.getItems().size()) {
                    List<LabTestFlag> labTestFlags = labTestFlagMap.get(tableView.getItems().get(index).getName());

                    VBox vBox = new VBox(5.0);
                    vBox.setAlignment(Pos.CENTER);
                    vBox.setPadding(new Insets(5, 0, 5, 0));
                    for (LabTestFlag flag : labTestFlags) {
                        vBox.getChildren().add(new Label(flag.getDefaultVal()));
                        Separator separator = new Separator(Orientation.HORIZONTAL);
                        vBox.getChildren().add(separator);
                    }
                    setGraphic(vBox);
                } else {
                    setGraphic(null);
                }
            }
        });

    }

    private List<LabTestFlag> getFlagsForLabTest(String testName) {
        List<LabTestFlag> flags = new ArrayList<>();
        for (LabTestFlag labTestFlag : labTestFlags) {
            if (labTestFlag.getTest().equals(testName)) {
                flags.add(labTestFlag);
            }
        }
        return flags;
    }

    private void filterResults(String filter) {
        if (filter == null || filter.isEmpty()) {
            tableView.setItems(mainLabTestsList);
            return;
        }
        ObservableList<LabTest> filteredList = FXCollections.observableArrayList();
        for (LabTest test : mainLabTestsList) {
            if (test.getName().toLowerCase().contains(filter.toLowerCase())) {
                filteredList.add(test);
            }
        }
        tableView.setItems(filteredList);
    }

    @FXML
    private void onNewLabTest() {
        editTest(null);
    }

    private void editTest(LabTest test) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource
                    ("main/resources/view/edit-lab-test.fxml"));
            Parent node = loader.load();

            //stage
            Stage stage = new Stage();
            stage.setScene(new Scene(node));
            stage.setResizable(false);
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(container.getScene().getWindow());

            //controller
            EditLabTestController controller = loader.getController();
            controller.setStage(stage);
            controller.setLabTest(test);
            if (test != null) {
                controller.setFlags(labTestFlagMap.get(test.getName()));

            }
            //show
            stage.showAndWait();

            if (test == null) {
                test = controller.getTest();
                if (test != null) {
                    labTestFlagMap.put(test.getName(), controller.getLabTestFlags());
                }
                tableView.getItems().add(test);
            } else {
                labTestFlagMap.put(test.getName(), controller.getLabTestFlags());
            }
            tableView.refresh();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
