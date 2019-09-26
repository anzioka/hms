package main.java.controller;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import main.java.model.Medicine;
import main.java.util.CurrencyUtil;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Created by alfonce on 07/07/2017.
 */
public class PharmacyInventoryController {

    //service to read db
    private final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
    @FXML
    Button addMedicineBtn;
    @FXML
    private TableView<Medicine> medicineTableView;
    @FXML
    private TableColumn<Medicine, String> nameCol, sellingPrice;
    @FXML
    private TableColumn<Medicine, Integer> quantityCol;
    @FXML
    private TextField medicineFilter;
    private ScheduledFuture<?> serviceScheduler;
    private ObservableList<Medicine> list = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        //read db

        //get medicine
        serviceScheduler = executorService.scheduleAtFixedRate(() -> {
            getAvailableMedicine();
            Platform.runLater(() -> {
                Label label = new Label("No drug records in pharmacy!");
                label.getStyleClass().add("text-danger");
                medicineTableView.setPlaceholder(label);
            });
        }, 1, 60, TimeUnit.SECONDS);

        setUpTable();
        configureFilter();

        ImageView imageView = new ImageView(new Image(getClass().getClassLoader().getResourceAsStream
                ("main/resources/images/add.png")));
        imageView.setFitHeight(15.0);
        imageView.setFitWidth(15.0);
        addMedicineBtn.setGraphic(imageView);

    }

    private void configureFilter() {
        medicineFilter.textProperty().addListener((observable, oldValue, newValue) -> {
            if (serviceScheduler != null) {
                serviceScheduler.cancel(true);
                filterResults(newValue);
            }
        });
    }

    private void filterResults(String filter) {

        if (filter == null || filter.isEmpty()) {
            medicineTableView.setItems(list);
            return;
        }
        ObservableList<Medicine> filtered = FXCollections.observableArrayList();
        for (Medicine medicine : list) {
            if (medicine.getName().toLowerCase().contains(filter.toLowerCase())) {
                filtered.add(medicine);
            }
        }
        medicineTableView.setItems(filtered);
    }

    private void getAvailableMedicine() {
        medicineTableView.setItems(list);
    }

    private void setUpTable() {
        Label label = new Label("Retrieving drugs ...");
        label.getStyleClass().add("text-info");
        medicineTableView.setPlaceholder(label);

        nameCol.setCellValueFactory(param -> param.getValue().nameProperty());
        sellingPrice.setCellValueFactory(param -> new SimpleStringProperty(CurrencyUtil.formatCurrency(param.getValue
                ().getSellingPrice())));
    }

    @FXML
    private void onNewRefill() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getClassLoader().getResource
                    ("main/resources/view/create_transfer_request.fxml"));
            Parent node = loader.load();

            //stage
            Stage stage = new Stage();
            stage.setScene(new Scene(node));
            stage.setResizable(false);
            stage.initModality(Modality.WINDOW_MODAL);

            //controller
            CreateTransferRequestController controller = loader.getController();
            controller.setStage(stage);

            //show
            stage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onAddMedicine() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource
                    ("main/resources/view/add_medicine.fxml"));
            Parent node = loader.load();

            //stage
            Stage stage = new Stage();
            stage.setScene(new Scene(node));
            stage.setResizable(false);
            stage.initModality(Modality.WINDOW_MODAL);

            //controller
            AddMedicineController controller = loader.getController();
            controller.setStage(stage);

            //show
            stage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    void addMedicine(Medicine medicine) {
        medicineTableView.getItems().add(medicine);
    }
}
