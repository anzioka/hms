package main.java.controller;

import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import main.java.model.Appointment;
import main.java.util.DBUtil;
import main.java.util.DateUtil;

import java.io.IOException;
import java.sql.ResultSet;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.TextStyle;
import java.util.*;

public class AppointmentController {
    @FXML
    private FlowPane monthViewPane;
    @FXML
    private VBox dayViewBox;
    @FXML
    private VBox container;
    @FXML
    private ChoiceBox<CalendarView> calendarViewChoiceBox;
    @FXML
    private StackPane nextControl, prevControl;
    @FXML
    private Label dayOfWeekLabel, dayOfMonthLabel, calendarTitle;

    private LocalTime startTime = LocalTime.of(7, 0);
    private LocalTime endTime = LocalTime.of(17, 0);
    private LocalDate currentDate = LocalDate.now();
    private Map<Integer, List<Appointment>> timeAppointmentListMap = new HashMap<>();
    private Map<Integer, Integer> dayAppointmentCountMap = new HashMap<>();
    private int userId;
    private Stage stage;
    private String patientId;

    @FXML
    private void initialize() {
        calendarViewChoiceBox.setItems(FXCollections.observableArrayList(CalendarView.values()));
        calendarViewChoiceBox.getSelectionModel().select(CalendarView.MONTH);
        calendarViewChoiceBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            getAppointments();
        });
        for (StackPane stackPane : new StackPane[]{prevControl, nextControl}) {
            stackPane.setOnMouseClicked(event -> {
                handleClicked((StackPane) event.getSource());
                getAppointments();
            });
        }
    }

    private void handleClicked(StackPane source) {
        CalendarView calendarView = calendarViewChoiceBox.getValue();
        switch (calendarView) {
            case DAY:
                if (source.getId().equals("prev")) {
                    currentDate = currentDate.minusDays(1);
                } else {
                    currentDate = currentDate.plusDays(1);
                }
                break;
            case MONTH:
                if (source.getId().equals("prev")) {
                    currentDate = currentDate.minusMonths(1);
                } else {
                    currentDate = currentDate.plusMonths(1);
                }
                break;
        }
        getAppointments();
    }

    private void generateCalendarView() {
        CalendarView calendarView = calendarViewChoiceBox.getValue();
        monthViewPane.setVisible(calendarView == CalendarView.MONTH);
        monthViewPane.setManaged(calendarView == CalendarView.MONTH);
        dayViewBox.setVisible(calendarView == CalendarView.DAY);
        dayViewBox.setManaged(calendarView == CalendarView.DAY);

        switch (calendarView) {
            case MONTH:
                showMonthView();
                break;
            case DAY:
                showDayView();
        }
    }

    private VBox getDayOfMonthCell(int day) {
        VBox vBox = new VBox(3.0);
        vBox.setPrefWidth(100);
        vBox.setPrefHeight(100);
        vBox.setMinHeight(100);
        vBox.setFillWidth(false);
        vBox.getStyleClass().addAll("border-light-3", "container-padded");
        if (day <= 7) {
            if ((day - 1) % 7 != 0) {
                vBox.setStyle("-fx-border-style: solid solid solid hidden;");
            }
        } else {
            if ((day - 1) % 7 != 0) {
                vBox.setStyle("-fx-border-style: hidden solid solid hidden;");
            } else {
                vBox.setStyle("-fx-border-style: hidden solid solid solid;");
            }
        }
        return vBox;
    }

    private StackPane getCurrentDayOfMonth(int day) {
        StackPane stackPane = new StackPane();
        stackPane.setPrefHeight(25);
        stackPane.setPrefWidth(25);

        Circle circle = new Circle(15);
        circle.setFill(Paint.valueOf("#2196F3"));
        circle.setStroke(Paint.valueOf("#2196F3"));

        Label label = new Label(day + "");
        label.getStyleClass().addAll("color-white", "fw-500", "h6_5");
        stackPane.getChildren().addAll(circle, label);
        return stackPane;
    }

    private void showMonthView() {
        monthViewPane.getChildren().clear();

        int numDays = currentDate.getMonth().maxLength(), day = 1, cellIndex = 0;
        LocalDate firstDay = currentDate.withDayOfMonth(1);
        while (day <= numDays) {
            VBox vBox = getDayOfMonthCell(cellIndex + 1);
            //day of week label
            if (cellIndex < 7) {
                Label dayOfWeek = new Label(DayOfWeek.of(cellIndex + 1).getDisplayName(TextStyle.SHORT, Locale.getDefault()) + "");
                dayOfWeek.getStyleClass().add("h6");
                dayOfWeek.setOpacity(0.78);
                if (cellIndex + 1 == currentDate.getDayOfWeek().getValue() && currentDate == LocalDate.now()) {
                    dayOfWeek.setStyle("-fx-text-fill: #2196F3");
                }
                vBox.getChildren().add(dayOfWeek);
            }
            if (cellIndex + 1 >= firstDay.getDayOfWeek().getValue()) {

                if (day == currentDate.getDayOfMonth() && currentDate.isEqual(LocalDate.now())) {
                    vBox.getChildren().add(getCurrentDayOfMonth(day));
                } else {
                    Label label = new Label(day + "");
                    label.getStyleClass().add("h6");
                    vBox.getChildren().add(label);
                }
                //appointments
                Integer numAppointments = dayAppointmentCountMap.get(day);
                if (numAppointments != null) {
                    Node appointments = getAppointmentsLabel(numAppointments);
                    int finalDay = day;
                    appointments.setOnMouseClicked(event -> {
                        viewAppointments(appointments, currentDate.withDayOfMonth(finalDay), null);
                        event.consume();
                    });
                    vBox.getChildren().add(appointments);
                }

                if (currentDate.isBefore(LocalDate.now()) || currentDate.isEqual(LocalDate.now())) {
                    vBox.setDisable(day < LocalDate.now().getDayOfMonth());
                }
                int finalDay1 = day;
                vBox.setOnMouseClicked(event -> {
                    vBox.getStyleClass().add("bg-brick-red");
                    createAppointment(vBox, null, currentDate.withDayOfMonth(finalDay1));
                    vBox.getStyleClass().remove("bg-brick-red");
                });
                day++;
            }

            monthViewPane.getChildren().add(vBox);
            cellIndex++;
        }
        stage.setHeight(computeHeight());
    }

    private double computeHeight() {
        //rather mechanical way to compute the height of window after drawing the layout.
        // Will try to figure out better way later. this works for now
        CalendarView calendarView = calendarViewChoiceBox.getValue();
        switch (calendarView) {
            case MONTH:
                int numCells = monthViewPane.getChildren().size();
                if (numCells > 35) {
                    return 71 + (105 * 6);
                } else {
                    return 71 + (105 * 5);
                }
            case DAY:
                return 144 + 53 * DateUtil.getNumHoursDiff(startTime, endTime);
        }
        return 0;
    }

    private void viewAppointments(Node container, LocalDate localDate, Integer appointmentId) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("main/resources/view/view-appointments.fxml"));
            Stage stage = new Stage();
            stage.setScene(new Scene(loader.load()));
            stage.initOwner(container.getScene().getWindow());
            stage.initModality(Modality.WINDOW_MODAL);

            ViewAppointments controller = loader.getController();
            controller.setParameters(localDate, appointmentId, userId);
            stage.showAndWait();
            getAppointments();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private Node getAppointmentsLabel(Integer numAppointments) {
        StackPane stackPane = new StackPane();
        stackPane.setPrefWidth(15);
        stackPane.setPrefHeight(15);

        Circle circle = new Circle(10, Paint.valueOf("#2BBBAD"));
        circle.setStroke(Paint.valueOf("#2BBBAD"));
        Label label = new Label(numAppointments + "");
        label.getStyleClass().add("fw-500");
        stackPane.getChildren().addAll(circle, label);
        return stackPane;
    }

    private void showDayView() {
        if (dayViewBox.getChildren().size() > 2) {
            dayViewBox.getChildren().remove(2, dayViewBox.getChildren().size());
        }
        dayOfMonthLabel.setText(currentDate.getDayOfMonth() + "");
        dayOfWeekLabel.setText(currentDate.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.getDefault()));

        for (int i = 0; i <= DateUtil.getNumHoursDiff(startTime, endTime); i++) {
            LocalTime time = startTime.plusHours(i);
            HBox hBox = new HBox(20);
            hBox.setPrefHeight(50);
            hBox.setAlignment(Pos.CENTER_LEFT);
            hBox.getChildren().add(new Label(DateUtil.formatTime(time)));
            hBox.setFillHeight(false);

            List<Appointment> appointments = timeAppointmentListMap.get(time.getHour());
            if (appointments != null) {
                for (Appointment appointment : appointments) {
                    HBox appt = new HBox();
                    appt.setAlignment(Pos.CENTER);
                    appt.getStyleClass().addAll("bg-success", "container-padded");

                    Label label = new Label(appointment.toString());
                    label.setOnMouseClicked(event -> {
                        viewAppointments(label, currentDate, appointment.getId());
                        event.consume();
                    });
                    appt.getChildren().add(label);
                    hBox.getChildren().add(appt);
                }
            }
            hBox.setOnMouseClicked(event -> {
                hBox.getStyleClass().add("bg-brick-red");
                createAppointment(hBox, time, null);
                hBox.getStyleClass().remove("bg-brick-red");
            });
            dayViewBox.getChildren().addAll(hBox, new Separator(Orientation.HORIZONTAL));
        }
        stage.setHeight(144 + 53 * DateUtil.getNumHoursDiff(startTime, endTime));
    }

    private void createAppointment(Node parent, LocalTime time, LocalDate date) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("main/resources/view/create-appointment.fxml"));
            Stage stage = new Stage();
            stage.setScene(new Scene(loader.load()));
            stage.initOwner(parent.getScene().getWindow());
            stage.initModality(Modality.WINDOW_MODAL);

            CreateAppointmentController controller = loader.getController();
            controller.setParameters(time, date, userId, patientId);
            controller.setStage(stage);
            stage.showAndWait();
            getAppointments();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void getAppointments() {
        CalendarView calendarView = calendarViewChoiceBox.getValue();
        if (calendarView == CalendarView.MONTH) {
            getSelectedMonthAppointments();
        } else {
            getSelectedDayAppointments();
        }
    }
    @FXML
    private void onNewAppointment() {
        createAppointment(container, null, null);
    }
    private void getSelectedMonthAppointments() {
        //after , set calendar title and generate calendar view
        Task<Map<Integer, Integer>> task = new Task<Map<Integer, Integer>>() {
            @Override
            protected Map<Integer, Integer> call() throws Exception {
                Map<Integer, Integer> map = new HashMap<>();
                ResultSet resultSet = DBUtil.executeQuery("select count(*) as count, date " +
                        "from appointments " +
                        "where doctor_id = " + userId + " " +
                        "and date between '" + currentDate.withDayOfMonth(1) + "' and '" + currentDate.withDayOfMonth(currentDate.lengthOfMonth()) + "' " +
                        "group by date");
                if (resultSet != null) {
                    while (resultSet.next()) {
                        Integer date = resultSet.getObject("date", LocalDate.class).getDayOfMonth();
                        map.put(date, resultSet.getInt("count"));
                    }
                }
                return map;
            }
        };
        task.setOnSucceeded(event -> {
            dayAppointmentCountMap = task.getValue();
            generateCalendarView();
            calendarTitle.setText(getMonthYear());
        });
        new Thread(task).start();
    }

    private String getMonthYear() {
        return currentDate.getMonth().getDisplayName(TextStyle.FULL, Locale.getDefault()) + " " + currentDate.getYear();
    }

    private void getSelectedDayAppointments() {
        //after , set calendar title and generate calendar view

        Task<Map<Integer, List<Appointment>>> task = new Task<Map<Integer, List<Appointment>>>() {
            @Override
            protected Map<Integer, List<Appointment>> call() throws Exception {
                Map<Integer, List<Appointment>> map = new HashMap<>();
                ResultSet resultSet = DBUtil.executeQuery("select id, date, time, FirstName, LastName " +
                        "from appointments " +
                        "inner join patients on patients.PatientId = appointments.patient_id " +
                        "where date = '" + currentDate + "' " +
                        "and doctor_id = " + userId);
                if (resultSet != null) {
                    while (resultSet.next()) {
                        Appointment appointment = new Appointment();
                        appointment.setId(resultSet.getInt("id"));
                        appointment.setDate(resultSet.getObject("date", LocalDate.class));
                        appointment.setPatientName(resultSet.getString("FirstName") + " " + resultSet.getString("LastName"));
                        int hour = resultSet.getObject("time", LocalTime.class).getHour();
                        if (map.containsKey(hour)) {
                            List<Appointment> soFar = map.get(hour);
                            soFar.add(appointment);
                            map.put(hour, soFar);
                        } else {
                            List<Appointment> list = new ArrayList<>();
                            list.add(appointment);
                            map.put(hour, list);
                        }
                    }
                }
                return map;
            }
        };

        task.setOnSucceeded(event -> {
            timeAppointmentListMap = task.getValue();
            generateCalendarView();
            calendarTitle.setText(getMonthYear());
        });

        new Thread(task).start();
    }


    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void setParameters(int userId, String currentPatientId) {
        this.userId = userId;
        this.patientId = currentPatientId;
        getAppointments();
    }

    private enum CalendarView {

        DAY("Day"), MONTH("Month");
        private String view;

        CalendarView(String view) {
            this.view = view;
        }

        @Override
        public String toString() {
            return view;
        }
    }

    @FXML
    private void onSetCurrentDate() {
        currentDate = LocalDate.now();
        getAppointments();
    }
}
