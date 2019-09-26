package main.java.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import main.Main;
import main.java.dao.MedicineDAO;
import main.java.model.Medicine;
import main.java.model.Permission;
import main.java.util.AlertUtil;
import main.java.util.CurrencyUtil;
import main.java.util.NumberUtil;
import main.java.util.StringUtil;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by alfonce on 07/07/2017.
 */
public class StockManagementController {

    private static final int MAX_RECORDS_PER_PAGE = 10;
    //table (drugs)
    @FXML
    private VBox container;
    @FXML
    private TableView<Medicine> medicineTableView;
    @FXML
    private TableColumn<Medicine, String> nameCol, buyingPriceCol,
            sellingPriceCol;
    @FXML
    private TableColumn<Medicine, Integer> shopQuantity, reorderLevelCol, storeQuantity, drugCode, quantity;
    @FXML
    private TextField searchFilter, quantityField;
    @FXML
    private TabPane tabPane;
    @FXML
    private ChoiceBox<String> choiceBox;
    @FXML
    private Pagination pagination;
    @FXML
    private Button addMedicineBtn, editMedicineBtn;
    private ObservableList<Medicine> medicineList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        addMedicineBtn.setDisable(!Main.userPermissions.get(Permission.CREATE_DRUGS));
        editMedicineBtn.setDisable(!Main.userPermissions.get(Permission.CREATE_DRUGS));

        tabPane.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.intValue() != 0) {
                loadTab(newValue.intValue());
            } else {
                getData();
            }
        });
        choiceBox.setItems(FXCollections.observableArrayList("All drugs", "Shop quantity below", "Store quantity " +
                "below" +
                ""));
        choiceBox.getSelectionModel().select(0);
        searchFilter.textProperty().addListener((observable, oldValue, newValue) -> {
            filterByName(newValue);
        });
        setUpTable();
        Executors.newSingleThreadScheduledExecutor().schedule(this::getData, 1, TimeUnit.SECONDS);
    }

    private void loadTab(int tabIndex) {
        String[] resourceFiles = new String[]{"purchases", "purchase_orders", "drug_transfers", "purchase-returns", "stock_take", "suppliers"};

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource
                    ("main/resources/view/" + resourceFiles[tabIndex - 1] + ".fxml"));
            Node node = loader.load();
            Tab selected = tabPane.getTabs().get(tabIndex);
            selected.setContent(node);

            AnchorPane.setRightAnchor(node, 0.0);
            AnchorPane.setLeftAnchor(node, 0.0);
            AnchorPane.setTopAnchor(node, 0.0);
            AnchorPane.setBottomAnchor(node, 0.0);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void filterByQuantity() {
        int index = choiceBox.getSelectionModel().getSelectedIndex();
        if (index == 0) {
            configurePagination(medicineList);
        }
        int quantityFilter = NumberUtil.stringToInt(quantityField.getText());
        if (quantityFilter > 0) {
            ObservableList<Medicine> list = FXCollections.observableArrayList();
            switch (index) {
                case 0:
                    list = medicineList;
                    break;
                case 1:
                    for (Medicine medicine : medicineList) {
                        if (medicine.getShopQuantity() < quantityFilter) {
                            list.add(medicine);
                        }
                    }
                    break;
                case 2:
                    for (Medicine medicine : medicineList) {
                        if (medicine.getStoreQuantity() < quantityFilter) {
                            list.add(medicine);
                        }
                    }
                    break;
            }
            configurePagination(list);
        }
    }

    private void getData() {
        Task<ObservableList<Medicine>> task = new Task<ObservableList<Medicine>>() {
            @Override
            protected ObservableList<Medicine> call() {
                return MedicineDAO.getMedicineList();
            }
        };
        task.setOnSucceeded(event -> {
            medicineList = task.getValue();
            configurePagination(medicineList);

            Label label = new Label("No drugs found!");
            label.getStyleClass().add("text-danger");
            medicineTableView.setPlaceholder(label);
        });
        new Thread(task).start();
    }

    private void configurePagination(ObservableList<Medicine> medicines) {
        int numPages = (int) Math.ceil((double) medicines.size() / MAX_RECORDS_PER_PAGE);
        if (numPages == 0) {
            numPages = 1;
        }
        pagination.setPageCount(numPages);
        pagination.setPageFactory(param -> {
            int startIndex = MAX_RECORDS_PER_PAGE * param;
            int endIndex = Math.min(startIndex + MAX_RECORDS_PER_PAGE, medicines.size());
            ObservableList<Medicine> list = FXCollections.observableArrayList(medicines.subList(startIndex,
                    endIndex));
            medicineTableView.setItems(list);
            return null;
        });
    }

    private void filterByName(String newValue) {
        if (newValue == null || newValue.isEmpty()) {
            configurePagination(medicineList);
            return;
        }
        ObservableList<Medicine> filteredList = FXCollections.observableArrayList();
        for (Medicine medicine : medicineList) {
            if (medicine.getName().toLowerCase().contains(newValue.toLowerCase())) {
                filteredList.add(medicine);
            }
        }
        configurePagination(filteredList);
    }

    private void setUpTable() {
        //place holder
        Label label = new Label("Searching drugs...");
        label.getStyleClass().add("text-info");
        medicineTableView.setPlaceholder(label);

        //column widths
        //columns
        drugCode.setCellValueFactory(param -> param.getValue().drugCodeProperty().asObject());
        storeQuantity.setCellValueFactory(param -> param.getValue().storeQuantityProperty().asObject());
        shopQuantity.setCellValueFactory(param -> param.getValue().shopQuantityProperty().asObject());
        reorderLevelCol.setCellValueFactory(param -> param.getValue().reorderLevelProperty().asObject());
        nameCol.setCellValueFactory(param -> param.getValue().nameProperty());
        buyingPriceCol.setCellValueFactory(param -> StringUtil.getStringProperty(CurrencyUtil.formatCurrency(param
                .getValue().getBuyingPrice())));
        sellingPriceCol.setCellValueFactory(param -> StringUtil.getStringProperty(CurrencyUtil.formatCurrency(param
                .getValue().getSellingPrice())));

    }

    @FXML
    private void onEditMedicine() {
        Medicine medicine = medicineTableView.getSelectionModel().getSelectedItem();
        if (medicine != null) {
            showEditDrugDialog(medicine);
        } else {
            AlertUtil.showAlert("Select Medicine", "Please select medicine from the table in order to edit", Alert
                    .AlertType.ERROR);
        }
    }

    private void showEditDrugDialog(Medicine medicine) {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getClassLoader().getResource("main/resources/view/add_medicine.fxml"));
            Parent node = loader.load();

            //stage
            Stage stage = new Stage();
            stage.setScene(new Scene(node));
            stage.setResizable(false);
            stage.initModality(Modality.WINDOW_MODAL);

            //controller
            AddMedicineController controller = loader.getController();
            controller.setMedicine(medicine);
            controller.setContext(this);
            controller.setStage(stage);
            stage.showAndWait();

            if (medicine == null) {
                if (controller.getMedicine() != null) {
                    medicineTableView.getItems().add(0, controller.getMedicine());
                }
            }
            medicineTableView.refresh();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onNewMedicine() {
        showEditDrugDialog(null);
    }

    @FXML
    private void onUploadList() {
        //TODO : enable upload later
    }

    @FXML
    private void onFilter() {
        filterByQuantity();
    }

    boolean isNameDuplicate(String name) {
        for (Medicine medicine : medicineTableView.getItems()) {
            if (medicine.getName().toLowerCase().equals(name.toLowerCase())) {
                return true;
            }
        }
        return false;
    }
}