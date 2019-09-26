package main.java.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.*;
import main.java.dao.InpatientDao;
import main.java.model.Inpatient;
import main.java.model.Ward;
import main.java.util.AgeUtil;
import main.java.util.DateUtil;
import org.controlsfx.control.textfield.AutoCompletionBinding;

import java.io.IOException;

public class InpatientManagementController {
    @FXML
    private TabPane tabPane;
    @FXML
    private TableView<Inpatient> tableView;
    @FXML
    private TableColumn<Inpatient, String> inpatientNo, name, dateAdmitted, ward, sex, bed, age, options;
    @FXML
    private ChoiceBox<Ward> wardChoiceBox;
    @FXML
    private ChoiceBox<SearchParam> searchParamChoiceBox;
    @FXML
    private TextField searchField;
    private ObservableList<Inpatient> allInpatients = FXCollections.observableArrayList();
    private AutoCompletionBinding<Inpatient> binding;

    @FXML
    private void initialize() {
        setUpTable();
        getData();
        searchParamChoiceBox.setItems(FXCollections.observableArrayList(SearchParam.values()));
        searchParamChoiceBox.getSelectionModel().select(SearchParam.FIRST_NAME);

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterResults();
        });

        wardChoiceBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            searchField.clear();
            searchField.requestFocus();
            filterResults();
        });

        tabPane.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.intValue() == 1) {
                loadTab(newValue.intValue());
            }
        });
    }

    private void loadTab(int index) {
        String[] resourceFiles = new String[]{"admission_history"};
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("main/resources/view/" + resourceFiles[index - 1] + ".fxml"));
            Tab tab = tabPane.getTabs().get(index);
            tab.setContent(loader.load());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setUpTable() {
        //placeholder
        Label label = new Label("No admitted patients");
        label.getStyleClass().add("text-danger");
        tableView.setPlaceholder(label);

        //
        inpatientNo.setCellValueFactory(param -> param.getValue().inpatientNumberProperty());
        name.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getFirstName() + " " + param
                .getValue().getLastName()));
        dateAdmitted.setCellValueFactory(param -> DateUtil.dateStringProperty(param.getValue().getDateAdmitted()));
        ward.setCellValueFactory(param -> param.getValue().assignedWardProperty());
        sex.setCellValueFactory(param -> param.getValue().sexualityProperty());
        age.setCellValueFactory(param -> AgeUtil.getAgeStringProperty(param.getValue().getDateOfBirth()));
        options.setCellFactory(param -> new TableCell<Inpatient, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                int index = getIndex();
                if (index >= 0 && index < tableView.getItems().size()) {
                    Button button = new Button("View");
                    button.getStyleClass().add("btn-info-outline");
                    button.setOnAction(event -> {
                        viewPatient(tableView.getItems().get(index));
                    });
                    setGraphic(button);
                } else {
                    setGraphic(null);
                }
            }
        });
        bed.setCellFactory(param -> new TableCell<Inpatient, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                int index = getIndex();
                if (index >= 0 && index < tableView.getItems().size()) {
                    setText(tableView.getItems().get(index).getBedId() + "");
                } else {
                    setText(null);
                }
            }
        });
    }

    private void viewPatient(Inpatient inpatient) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource
                    ("main/resources/view/view-inpatient.fxml"));
            Node node = loader.load();

            //controller
            ViewInpatientController controller = loader.getController();
            controller.setPatient(inpatient);
            controller.setContext(this);
            addNewTab("Patient : " + inpatient.getFirstName(), node);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void addNewTab(String tabName, Node node) {
        if (tabPane.getTabs().size() > 2) {
            tabPane.getTabs().remove(tabPane.getTabs().size() - 1); //remove the last tab
        }
        Tab tab = new Tab(tabName);
        tab.setContent(node);
        tab.setClosable(true);
        tabPane.getTabs().add(tab);
        tabPane.getSelectionModel().select(tab);
    }

    private void getData() {
        Task<ObservableList<Ward>> task = new Task<ObservableList<Ward>>() {
            @Override
            protected ObservableList<Ward> call() throws Exception {
                return InpatientDao.getWards();
            }

            ;
        };
        task.setOnSucceeded(event -> {
            wardChoiceBox.setItems(task.getValue());
            wardChoiceBox.getItems().add(0, new Ward("All wards"));
            wardChoiceBox.getSelectionModel().select(0);
        });

        new Thread(task).start();
        //get patients
        Task<ObservableList<Inpatient>> inpatientsTask = new Task<ObservableList<Inpatient>>() {
            @Override
            protected ObservableList<Inpatient> call() throws Exception {
                return InpatientDao.getAdmittedPatients();
            }
        };
        inpatientsTask.setOnSucceeded(event -> {
            allInpatients = inpatientsTask.getValue();
            tableView.setItems(allInpatients);
        });
        new Thread(inpatientsTask).start();
    }

    private void filterResults() {
        String searchString = searchField.getText();
        int selectedWardIndex = wardChoiceBox.getSelectionModel().getSelectedIndex();
        if (searchString == null || searchString.isEmpty()) {
            if (selectedWardIndex == 0 || selectedWardIndex == -1) {
                tableView.setItems(allInpatients);
            } else {
                ObservableList<Inpatient> results = FXCollections.observableArrayList();
                for (Inpatient inpatient : allInpatients) {
                    if (inpatient.getAssignedWard().equals(wardChoiceBox.getItems().get(selectedWardIndex).getName())) {
                        results.add(inpatient);
                    }
                }
                tableView.setItems(results);
            }
            return;
        }
        ObservableList<Inpatient> results = FXCollections.observableArrayList();
        SearchParam searchParam = searchParamChoiceBox.getValue();
        if (searchParam != SearchParam.INPATIENT_NUM) {
            if (selectedWardIndex != 0) {
                for (Inpatient inpatient : allInpatients) {
                    if ((inpatient.getFirstName().toLowerCase().contains(searchString.toLowerCase()) || (inpatient.getLastName().toLowerCase().contains(searchString.toLowerCase()))) && inpatient.getAssignedWard().equals(wardChoiceBox.getItems().get(selectedWardIndex).getName())) {
                        results.add(inpatient);
                    }
                }
            } else {
                for (Inpatient inpatient : allInpatients) {
                    if (inpatient.getFirstName().toLowerCase().contains(searchString.toLowerCase()) || inpatient.getLastName().toLowerCase().contains(searchString.toLowerCase())) {
                        results.add(inpatient);
                    }
                }
            }
        }
        tableView.setItems(results);
    }

    @FXML
    private void onAdmitPatient() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource
                    ("main/resources/view/register-patient.fxml"));
            Node node = loader.load();
            EditPatientController controller = loader.getController();
            controller.setInpatientMode();
            controller.setContext(this);
            addNewTab("Admit Patient", node);

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @FXML
    private void onSearch() {
        //TODO : search patients based on search params
        SearchParam searchParam = searchParamChoiceBox.getValue();
        if (searchParam == SearchParam.INPATIENT_NUM) {
            ObservableList<Inpatient> results = FXCollections.observableArrayList();
            String searchString = searchField.getText();
            if (searchString != null && !searchString.isEmpty()) {
                for (Inpatient inpatient : allInpatients) {
                    if (inpatient.getInpatientNumber().toLowerCase().contains(searchString.toLowerCase())) {
                        results.add(inpatient);
                    }
                }
                tableView.setItems(results);
            } else {
                tableView.setItems(allInpatients);
            }
        }
        searchField.clear();
        searchField.requestFocus();
    }

    @FXML
    public void onRefreshData() {
        getData();
    }

    void closeTab() {
        if (tabPane.getTabs().size() > 2) {
            tabPane.getTabs().remove(tabPane.getTabs().size() - 1); //remove the last tab
        }
        tabPane.getSelectionModel().select(0);
    }

    public enum SearchParam {
        FIRST_NAME("First Name"), LAST_NAME("Last Name"), INPATIENT_NUM("Inpatient Number");
        String param;

        SearchParam(String param) {
            this.param = param;
        }

        @Override
        public String toString() {
            return this.param;
        }
    }

}
