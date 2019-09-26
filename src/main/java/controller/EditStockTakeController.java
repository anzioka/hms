package main.java.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Callback;
import main.Main;
import main.java.dao.MedicineDAO;
import main.java.dao.StockTakeDao;
import main.java.model.Medicine;
import main.java.model.MedicineLocation;
import main.java.model.StockTake;
import main.java.util.AlertUtil;
import main.java.util.DBUtil;
import main.java.util.NumberUtil;
import org.controlsfx.control.textfield.AutoCompletionBinding;
import org.controlsfx.control.textfield.TextFields;

import java.time.LocalDate;

public class EditStockTakeController {
    private Stage stage;
    private StockTake stockTake;
    private ObservableList<Medicine> medicines = FXCollections.observableArrayList();
    @FXML
    private VBox container;
    @FXML
    private TextField searchField;
    @FXML
    private ChoiceBox<MedicineLocation> medicineLocationChoiceBox;
    @FXML
    private TableView<StockTake> tableView;
    @FXML
    private TableColumn<StockTake, String> name;
    @FXML
    private TableColumn<StockTake, Integer> quantityChange, physicalCount, qtyAtHand, drugId;
    @FXML
    private TableColumn<StockTake, Double> valueChange, sellingPrice;

    @FXML
    private void initialize() {
        medicineLocationChoiceBox.setItems(FXCollections.observableArrayList(MedicineLocation.values()));
        medicineLocationChoiceBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue,
                                                                                          newValue) -> {
            addItemsToTableView();
            tableView.setVisible(true);
        });
        tableView.setVisible(false);
        setUpTableView();
        getData();
    }

    private void getData() {
        Task<ObservableList<Medicine>> task = new Task<ObservableList<Medicine>>() {
            @Override
            protected ObservableList<Medicine> call() {
                return MedicineDAO.getMedicineList();
            }
        };
        task.setOnSucceeded(event -> {
            addItemsToTableView();
            medicines = task.getValue();
            AutoCompletionBinding<Medicine> binding = TextFields.bindAutoCompletion(searchField, medicines);
            binding.setOnAutoCompleted(autoCompletionEvent -> {
                int row = getRowIndexMatchingMedicineId(autoCompletionEvent.getCompletion().getDrugCode());
                tableView.scrollTo(row);
                tableView.getSelectionModel().select(row);
                tableView.edit(row, physicalCount);
                searchField.clear();
            });
        });
        new Thread(task).start();
    }

    private int getRowIndexMatchingMedicineId(int medicineId) {
        for (int i = 0; i < tableView.getItems().size(); i++) {
            if (tableView.getItems().get(i).getDrugId() == medicineId) {
                return i;
            }
        }
        return 0;
    }

    private void addItemsToTableView() {
        MedicineLocation medicineLocation = medicineLocationChoiceBox.getValue();
        if (medicineLocation != null) {
            tableView.getItems().clear();

            Task<ObservableList<StockTake>> task = new Task<ObservableList<StockTake>>() {
                @Override
                protected ObservableList<StockTake> call() {
                    ObservableList<StockTake> list = FXCollections.observableArrayList();
                    int stockTakeId = StockTakeDao.getNextStockTakeId();

                    for (int i = 0; i < medicines.size(); i++) {
                        StockTake stockTake = new StockTake();
                        if (medicineLocation == MedicineLocation.SHOP) {
                            stockTake.setQtyOnHand(medicines.get(i).getShopQuantity());
                            stockTake.setCountedQty(medicines.get(i).getShopQuantity());
                        } else {
                            stockTake.setQtyOnHand(medicines.get(i).getStoreQuantity());
                            stockTake.setCountedQty(medicines.get(i).getStoreQuantity());
                        }
                        stockTake.setDrugId(medicines.get(i).getDrugCode());
                        stockTake.setMedicineLocation(medicineLocation);
                        stockTake.setSellingPrice(medicines.get(i).getSellingPrice());
                        stockTake.setMedicineName(medicines.get(i).getName());
                        stockTake.setStockTakeId(stockTakeId);
                        list.add(stockTake);
                        updateProgress(i, medicines.size());
                    }
                    return list;
                }
            };
            for (Node node : container.getChildren()) {
                node.setVisible(false);
                node.setManaged(false);
            }
            ProgressBar progressBar = new ProgressBar();
            progressBar.progressProperty().bind(task.progressProperty());
            container.getChildren().add(progressBar);

            task.setOnSucceeded(event -> {
                container.getChildren().remove(progressBar);
                for (Node node : container.getChildren()) {
                    node.setVisible(true);
                    node.setManaged(true);
                }
                if (!task.getValue().isEmpty()) {
                    tableView.setItems(task.getValue());
                    searchField.requestFocus();
                } else {
                    Label label = new Label("No items");
                    label.getStyleClass().add("missing-content");
                    tableView.setPlaceholder(label);
                }
            });
            new Thread(task).start();
        }
    }

    private void setUpTableView() {
        for (TableColumn tableColumn : tableView.getColumns()) {
            if (tableColumn != name) {
                tableColumn.prefWidthProperty().bind(tableView.widthProperty().divide(8));
            } else {
                tableColumn.prefWidthProperty().bind(tableView.widthProperty().divide(4));
            }
        }

        //enable cell selection
        tableView.getSelectionModel().setCellSelectionEnabled(true);

        //data
        name.setCellValueFactory(param -> param.getValue().medicineNameProperty());
        valueChange.setCellValueFactory(param -> param.getValue().valueChangeProperty().asObject());
        sellingPrice.setCellValueFactory(param -> param.getValue().sellingPriceProperty().asObject());
        qtyAtHand.setCellValueFactory(param -> param.getValue().qtyOnHandProperty().asObject());
        quantityChange.setCellValueFactory(param -> param.getValue().qtyChangeProperty().asObject());
        drugId.setCellValueFactory(param -> param.getValue().drugIdProperty().asObject());
        Callback<TableColumn<StockTake, Integer>, TableCell<StockTake, Integer>> cellFactory = (param -> new
                EditingCell());
        physicalCount.setCellFactory(cellFactory);
    }

    Stage getStage() {
        return stage;
    }

    void setStage(Stage stage) {
        this.stage = stage;
    }

    StockTake getStockTake() {
        return stockTake;
    }

    void setStockTake(StockTake stockTake) {
        this.stockTake = stockTake;
    }

    @FXML
    private void onExit() {
        stage.close();
    }

    @FXML
    private void onSave() {
        ObservableList<StockTake> list = FXCollections.observableArrayList();
        for (StockTake stockTake : tableView.getItems()) {
            if (stockTake.getQtyChange() != 0) {
                list.add(stockTake);
            }
        }

        if (list.isEmpty()) {
            AlertUtil.showAlert("Stock Take", "No items have been changed", Alert.AlertType.WARNING);
            return;
        }

        if (DBUtil.saveStockTake(list)) {
            AlertUtil.showAlert("Stock Take", "Stock take for " + list.size() + " drugs completed successfully",
                    Alert.AlertType.INFORMATION);
            createSummary(list);
            onExit();
        } else {
            AlertUtil.showAlert("Stock Take", "An error occurred while attempting to save stock take(s)", Alert
                    .AlertType.ERROR);
        }
    }

    private void createSummary(ObservableList<StockTake> list) {
        stockTake = new StockTake();
        int qtyChange = 0;
        double valueChange = 0;
        for (StockTake item : list) {
            qtyChange += item.getQtyChange();
            valueChange += item.getValueChange();
        }
        stockTake.setValueChange(valueChange);
        stockTake.setQtyChange(qtyChange);
        stockTake.setStockTakeId(list.get(0).getStockTakeId());
        stockTake.setUserName(Main.currentUser.getFirstName());
        stockTake.setDateCreated(LocalDate.now());
        stockTake.setMedicineLocation(medicineLocationChoiceBox.getValue());
    }

    private class EditingCell extends TableCell<StockTake, Integer> {
        private TextField textField;

        private void createTextField() {
            textField = new TextField();
            textField.setMinWidth(this.getWidth() - this.getGraphicTextGap() * 2);
            textField.setOnAction(event -> {
                commitEdit(NumberUtil.stringToInt(textField.getText()));
            });
            textField.setAlignment(Pos.CENTER);
        }

        @Override
        public void startEdit() {
            super.startEdit();
            if (textField != null) {
                textField.requestFocus();
            }
        }

        @Override
        public void commitEdit(Integer newValue) {
            super.commitEdit(newValue);
            StockTake stockTake = tableView.getItems().get(getIndex());
            if (newValue >= 0) {
                stockTake.setCountedQty(newValue);
                stockTake.setQtyChange(newValue - stockTake.getQtyOnHand());
                stockTake.setValueChange(stockTake.getQtyChange() * stockTake.getSellingPrice());
                textField.setText(newValue + "");
                searchField.requestFocus();
            } else {
                AlertUtil.showAlert("Invalid Quantity", "The value you entered is invalid", Alert.AlertType.ERROR);
                tableView.edit(getIndex(), physicalCount);
            }
        }

        @Override
        protected void updateItem(Integer item, boolean empty) {
            super.updateItem(item, empty);
            int index = getIndex();
            if (index >= 0 && index < tableView.getItems().size()) {
                if (textField == null) {
                    createTextField();
                }
                textField.setText(tableView.getItems().get(index).getCountedQty() + "");
                setGraphic(textField);
            } else {
                setGraphic(null);
            }
        }
    }
}
