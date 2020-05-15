package bo.tc.tcplanner.Gui;

import bo.tc.tcplanner.app.SolverCore.SolverProgress;
import bo.tc.tcplanner.app.SolverCore.Toolbox;
import bo.tc.tcplanner.app.TCApp;
import bo.tc.tcplanner.datastructure.*;
import bo.tc.tcplanner.datastructure.converters.DataStructureBuilder;
import bo.tc.tcplanner.domain.filters.FilterTools;
import bo.tc.tcplanner.domain.moves.AllocationValues;
import bo.tc.tcplanner.domain.moves.SetValueMove;
import bo.tc.tcplanner.domain.planningstructures.Allocation;
import bo.tc.tcplanner.domain.planningstructures.ResourceTotalKey;
import bo.tc.tcplanner.domain.planningstructures.Schedule;
import com.calendarfx.model.Calendar;
import com.calendarfx.model.CalendarSource;
import com.calendarfx.model.Entry;
import com.calendarfx.model.Interval;
import com.calendarfx.view.CalendarView;
import com.calendarfx.view.EntryViewBase;
import com.calendarfx.view.TimeField;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Throwables;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.SetChangeListener;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.skin.ComboBoxListViewSkin;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.optaplanner.core.api.score.buildin.hardmediumsoftlong.HardMediumSoftLongScore;
import org.optaplanner.core.api.solver.SolverFactory;
import org.optaplanner.core.impl.score.director.ScoreDirector;

import java.io.IOException;
import java.rmi.RemoteException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static bo.tc.tcplanner.PropertyConstants.*;
import static bo.tc.tcplanner.app.Test.initializeExampleFiles;

public class TCGuiController {
    // Objects
    private TCApp application;
    private ScoreDirector<Schedule> guiScoreDirector;
    private Stage primaryStage;

    // Example Data
    TimelineBlock exampleTimelineBlock;
    TimeHierarchyMap exampleTimeHierarchyMap;
    LocationHierarchyMap exampleLocationHierarchyMap;
    ValueEntryMap exampleValueEntryMap;

    // Data
    public Schedule guiSchedule;
    FilteredList<Allocation> displayingAllocations;
    Map<Allocation, Entry<Allocation>> allocationCalendarEntryMap;
    Map<Entry<Allocation>, ChangeListener<Interval>> calendarEntryIntervalListenerMap;
    Map<Allocation, BiMap<ResourceTotalKey, Entry<Allocation>>> allocationResourceEntryMap;
    Queue<String> consoleBuffer;
    Calendar calendar_resolved;
    Calendar calendar_unresolved;
    Calendar resource_resolved;
    Calendar resource_unresolved;
    XYChart.Series<Number, Number> scoreSeries;
    XYChart.Series<ZonedDateTime, Number> resourceReserveSeries;
    XYChart.Series<ZonedDateTime, Number> resourceDeficitSeries;

    // Runtime
    Map<Allocation, AllocationValues> clipboard = new HashMap<>();
    Allocation currentAllocation;
    Map<Allocation, AllocationValues> pendingAllocationValues = new HashMap<>();
    Set<Allocation> forceDisplayingAllocations;
    Stack<SetValueMove> undoStack;
    Stack<SetValueMove> redoStack;

    // Listeneres
    ChangeListener<Interval> calendar_calendarview_intervalListener;

    // Components
    public Button start_btn;
    public Button stop_btn;
    public Button reset_btn;
    public Button refresh_btn;
    public Button commit_btn;
    public Button cancel_btn;
    public ToggleButton update_toggle;
    public TextArea console_textarea;
    public TextArea explanation_textarea;
    public TextArea statusdetail_textarea;
    public TextArea editor_textarea;
    public Text status_text;
    public TableView<Allocation> editor_table;
    public ProgressIndicator solving_progress;
    public CalendarView calendar_calendarview;
    public CalendarView resource_calendarview;
    public TabPane tab_pane;
    public Tab tab_planner;
    public Tab tab_calendar;
    public Tab tab_resource;
    public LineChart<Number, Number> solveingspeed_chart;
    public LineChart<ZonedDateTime, Number> resourceLineChart;
    public ComboBox<String> rawdata_selecteddata;
    public TextArea rawdata_textarea;
    public Tab tab_rawdata;

    public TCGuiController() {

    }

    @FXML
    private void initialize() {
        Platform.runLater(() -> {
            initializeScoreDirector();
            initializeSchedule();
            undoStack = new Stack<>();
            redoStack = new Stack<>();

            // Buttons
            start_btn.setOnAction(e -> {
                Platform.runLater(() -> primaryStage.getScene().setCursor(Cursor.WAIT));
                try {
                    application.getRmiInterface().startSolver(guiSchedule);
                    Platform.runLater(() -> scoreSeries.getData().clear());
                } catch (Exception ex) {
                    ex.printStackTrace();
                    showOperationForbiddenAlert(Throwables.getStackTraceAsString(ex));
                }
                Platform.runLater(() -> primaryStage.getScene().setCursor(Cursor.DEFAULT));
            });
            stop_btn.setOnAction(e -> {
                Platform.runLater(() -> primaryStage.getScene().setCursor(Cursor.WAIT));
                try {
                    application.getRmiInterface().stopSolver();
                    refreshScheduleRemote();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    showOperationForbiddenAlert(Throwables.getStackTraceAsString(ex));
                }
                Platform.runLater(() -> primaryStage.getScene().setCursor(Cursor.DEFAULT));
            });
            reset_btn.setOnAction(e -> {
                Platform.runLater(() -> primaryStage.getScene().setCursor(Cursor.WAIT));
                try {
                    application.getRmiInterface().resetSolver();

                } catch (Exception ex) {
                    ex.printStackTrace();
                    showOperationForbiddenAlert(Throwables.getStackTraceAsString(ex));
                }
                Platform.runLater(() -> primaryStage.getScene().setCursor(Cursor.DEFAULT));
            });
            refresh_btn.setOnAction(e -> {
                refreshScheduleRemote();
            });

            // Toggles
            consoleBuffer = new CircularFifoQueue<>(60);
            Service<Void> consoleService = new Service<>() {
                @Override
                protected Task<Void> createTask() {
                    return new Task<>() {
                        @Override
                        protected Void call() {
                            while (!isCancelled()) {
                                try {
                                    synchronized (consoleBuffer) {
                                        consoleBuffer.add(application.getRmiInterface().getConsoleBuffer());
                                    }
                                    Platform.runLater(() -> {
                                        console_textarea.setText(String.join("", consoleBuffer));
                                        console_textarea.appendText("");
                                    });

                                } catch (RemoteException e) {
                                    e.printStackTrace();
                                }
                            }
                            return null;
                        }
                    };
                }
            };
            console_textarea.textProperty().addListener(e -> {
                console_textarea.setScrollTop(Double.MAX_VALUE);
            });

            // Raw Data Tab
            rawdata_selecteddata.getItems().addAll("TimelineBlock", "TimeHierarchyMap", "LocationHierarchyMap", "ValueEntryMap");
            rawdata_selecteddata.getSelectionModel().selectedItemProperty().addListener((observableValue, oldValue, newValue) -> {
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                if (newValue.equals("TimelineBlock")) {
                    rawdata_textarea.setText(gson.toJson(exampleTimelineBlock));
                }
                if (newValue.equals("TimeHierarchyMap")) {
                    rawdata_textarea.setText(gson.toJson(exampleTimeHierarchyMap));
                }
                if (newValue.equals("LocationHierarchyMap")) {
                    rawdata_textarea.setText(gson.toJson(exampleLocationHierarchyMap));
                }
                if (newValue.equals("ValueEntryMap")) {
                    rawdata_textarea.setText(gson.toJson(exampleValueEntryMap));
                }
            });

            scoreSeries = new XYChart.Series<>();
            solveingspeed_chart.setAnimated(false);
            ((NumberAxis) solveingspeed_chart.getXAxis()).setForceZeroInRange(false);
            solveingspeed_chart.getData().add(scoreSeries);
            Service<Void> solverProgressService = new Service<>() {
                @Override
                protected Task<Void> createTask() {
                    return new Task<>() {
                        UUID currentScheduleID = null;

                        @Override
                        protected Void call() {
                            while (!isCancelled()) {
                                try {
                                    SolverProgress solverProgress = application.getRmiInterface().getSolvingProgress();
                                    Platform.runLater(() -> {
                                        solving_progress.setProgress(
                                                1 - (double) solverProgress.getUnsolvedEntities() /
                                                        solverProgress.getTotalEntities());
                                        status_text.setText("Solving: " +
                                                solverProgress.getSolverScoreDelta().currScore.toShortString() +
                                                "(" + solverProgress.getUnsolvedEntities() + "/" + solverProgress.getTotalEntities() + ")");
                                        statusdetail_textarea.setText(
                                                "Status: " + solverProgress.getSolverScoreDelta().getSinceLastImproved() + "ms " +
                                                        "\nPhase: " + solverProgress.getSolvePhaseName()
                                                        + " \nScore:" + solverProgress.getSolverScoreDelta().currScore.toShortString()
                                                        + " \nRate: " + solverProgress.getSolverScoreDelta().getRate().toShortString()
                                                        + " \nAdded:" + solverProgress.getAddedTimelineEntries().toString());
                                        long secondElapsed = solverProgress.getSolverScoreDelta().getElapsed() / 1000;
                                        while (scoreSeries.getData().size() > 20) scoreSeries.getData().remove(0);
                                        HardMediumSoftLongScore score = (HardMediumSoftLongScore) solverProgress.getSolverScoreDelta().currScore;
                                        String newLabel = score.getHardScore() < 0 ? "h" : (score.getMediumScore() < 0 ? "m" : "s");
                                        if (scoreSeries.getName() == null || !scoreSeries.getName().equals(newLabel) ||
                                                !solverProgress.getScheduleId().equals(currentScheduleID)) {
                                            currentScheduleID = solverProgress.getScheduleId();
                                            scoreSeries.getData().clear();
                                            scoreSeries.setName(newLabel);
                                        }
                                        scoreSeries.getData().add(new XYChart.Data<>(
                                                secondElapsed,
                                                score.getHardScore() < 0 ? score.getHardScore() : (score.getMediumScore() < 0 ? score.getMediumScore() : score.getSoftScore())));
                                    });
                                } catch (RemoteException e) {
                                    e.printStackTrace();
                                }
                            }
                            return null;
                        }
                    };
                }
            };

            Service<Void> scheduleDetailService = new Service<>() {
                @Override
                protected Task<Void> createTask() {
                    return new Task<>() {
                        @Override
                        protected Void call() {
                            while (!isCancelled()) {
                                try {
                                    String solverProgress = application.getRmiInterface().getScoreExplaination();
                                    Platform.runLater(() -> {
                                        explanation_textarea.setText(solverProgress);
                                    });
                                } catch (RemoteException e) {
                                    e.printStackTrace();
                                }
                            }
                            return null;
                        }
                    };
                }
            };

            update_toggle.selectedProperty().addListener((observableValue, oldValue, newValue) -> {
                Platform.runLater(() -> primaryStage.getScene().setCursor(Cursor.WAIT));
                if (newValue) {
                    consoleService.restart();
                    solverProgressService.restart();
                    scheduleDetailService.restart();
                } else {
                    consoleService.cancel();
                    solverProgressService.cancel();
                    scheduleDetailService.cancel();
                }
                Platform.runLater(() -> primaryStage.getScene().setCursor(Cursor.DEFAULT));
            });

            // Table
            TableColumn<Allocation, Integer> idxColumn = new TableColumn<>("Idx");
            idxColumn.setCellValueFactory(x ->
                    new SimpleObjectProperty<>(x.getValue().getWeight()));
            idxColumn.setPrefWidth(40);

            TableColumn<Allocation, Integer> rowColumn = new TableColumn<>("Row");
            rowColumn.setCellValueFactory(x ->
                    new SimpleObjectProperty<>(x.getValue().getTimelineEntry().getTimelineProperty().getRownum()));
            rowColumn.setPrefWidth(40);

            TableColumn<Allocation, Integer> idColumn = new TableColumn<>("Id");
            idColumn.setCellValueFactory(x ->
                    new SimpleObjectProperty<>(x.getValue().getTimelineEntry().getTimelineProperty().getTimelineid()));
            idColumn.setPrefWidth(40);

            TableColumn<Allocation, Integer> percentColumn = new TableColumn<>("%");
            percentColumn.setCellValueFactory(new PropertyValueFactory<>("progressdelta"));
            percentColumn.setCellFactory(x -> new TableCell<>() {
                @Override
                protected void updateItem(Integer item, boolean empty) {
                    super.updateItem(item, empty);
                    Allocation thisObject = this.getTableRow().getItem();
                    if (empty || thisObject == null) {
                        setText(null);
                    } else {
                        ComboBox<Integer> cb = new ComboBox<>(FXCollections.observableArrayList());
                        thisObject.getProgressDeltaRange().createOriginalIterator()
                                .forEachRemaining(x -> cb.getItems().add(x));
                        Integer originalValue = thisObject.getProgressdelta();
                        cb.getSelectionModel().select(originalValue);
                        cb.getSelectionModel().selectedItemProperty().addListener((observableValue, oldValue, newValue) -> {
                            if (!newValue.equals(originalValue))
                                setAllocationVal(thisObject, "progressdelta", newValue);
                        });
                        cb.disableProperty().set(!FilterTools.ProgressDeltaCanChange(thisObject));
                        scrollComboBox(cb);
                        setGraphic(cb);
                        setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                    }
                }
            });
            percentColumn.setCellValueFactory(x -> new SimpleObjectProperty<>(x.getValue().getProgressdelta()));
            percentColumn.setPrefWidth(80);

            TableColumn<Allocation, ZonedDateTime> startdateColumn = new TableColumn<>("Start Date");
            startdateColumn.setCellFactory(col -> new TableCell<>() {
                @Override
                public void updateItem(ZonedDateTime item, boolean empty) {
                    super.updateItem(item, empty);
                    Allocation thisObject = this.getTableRow().getItem();
                    if (empty || thisObject == null || thisObject.getStartDate() == null) {
                        setText(null);
                    } else {
                        LocalDateTime originalValue = item.withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime();
                        HBox dateBox = new HBox(10);
                        DatePicker datePicker = new DatePicker();
                        TimeField timeField = new TimeField();
                        dateBox.setAlignment(Pos.CENTER_LEFT);
                        datePicker.setPrefWidth(110);
                        datePicker.setValue(originalValue.toLocalDate());
                        datePicker.setOnAction(event -> setAllocationVal(
                                thisObject, "start_date",
                                LocalDateTime.of(datePicker.getValue(), timeField.getValue())));
                        timeField.setPrefWidth(90);
                        timeField.setValue(originalValue.toLocalTime());
                        timeField.valueProperty().addListener(event -> setAllocationVal(
                                thisObject, "start_date",
                                LocalDateTime.of(datePicker.getValue(), timeField.getValue())));
                        dateBox.getChildren().addAll(datePicker, timeField);
                        dateBox.disableProperty().set(!FilterTools.StartTimeCanChange(thisObject));
                        setGraphic(dateBox);
                    }
                }
            });
            startdateColumn.setCellValueFactory(x -> new SimpleObjectProperty<>(x.getValue().getStartDate()));
            startdateColumn.setPrefWidth(200);

            TableColumn<Allocation, ZonedDateTime> enddateColumn = new TableColumn<>("End Date");
            enddateColumn.setCellFactory(col -> new TableCell<>() {
                @Override
                public void updateItem(ZonedDateTime item, boolean empty) {
                    super.updateItem(item, empty);
                    Allocation thisObject = this.getTableRow().getItem();
                    if (empty || thisObject == null || thisObject.getStartDate() == null) {
                        setText(null);
                    } else {
                        LocalDateTime originalValue = item.withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime();
                        HBox dateBox = new HBox(10);
                        DatePicker datePicker = new DatePicker();
                        TimeField timeField = new TimeField();
                        dateBox.setAlignment(Pos.CENTER_LEFT);
                        datePicker.setPrefWidth(110);
                        datePicker.setValue(originalValue.toLocalDate());
                        datePicker.setOnAction(event -> setAllocationVal(
                                thisObject, "end_date",
                                LocalDateTime.of(datePicker.getValue(), timeField.getValue())));
                        timeField.setPrefWidth(90);
                        timeField.setValue(originalValue.toLocalTime());
                        timeField.valueProperty().addListener(event -> setAllocationVal(
                                thisObject, "end_date",
                                LocalDateTime.of(datePicker.getValue(), timeField.getValue())));
                        dateBox.getChildren().addAll(datePicker, timeField);
                        dateBox.disableProperty().set(!FilterTools.StartTimeCanChange(thisObject));
                        setGraphic(dateBox);
                    }
                }
            });
            enddateColumn.setCellValueFactory(x -> new SimpleObjectProperty<>(x.getValue().getEndDate()));
            enddateColumn.setPrefWidth(200);

            TableColumn<Allocation, Duration> durationColumn = new TableColumn<>("Duration");
            durationColumn.setCellValueFactory(x ->
                    new SimpleObjectProperty<>(x.getValue().getPlannedDuration()));
            durationColumn.setPrefWidth(80);

            TableColumn<Allocation, TimelineEntry> titleColumn = new TableColumn<>("Entry");
            titleColumn.setCellValueFactory(x ->
                    new SimpleObjectProperty<>(x.getValue().getTimelineEntry()));
            titleColumn.setCellFactory(x -> new TableCell<>() {
                @Override
                protected void updateItem(TimelineEntry item, boolean empty) {
                    super.updateItem(item, empty);
                    Allocation thisObject = this.getTableRow().getItem();
                    if (empty || thisObject == null) {
                        setText(null);
                    } else {
                        ComboBox<TimelineEntry> cb = new ComboBox<>(
                                FXCollections.observableArrayList(thisObject.getSchedule().getTimelineEntryList()
                                        .stream().filter(x -> x.getTimelineProperty().getPlanningWindowType().equals(PlanningWindowTypes.types.Draft.name()))
                                        .collect(Collectors.toList()))
                                        .sorted(Comparator.comparing(TimelineEntry::toString)));
                        TimelineEntry originalValue = thisObject.getTimelineEntry();
                        cb.setEditable(true);
//                        TextFields.bindAutoCompletion(cb.getEditor(), cb.getItems());
                        cb.setConverter(new StringConverter<>() {
                            @Override
                            public String toString(TimelineEntry timelineEntry) {
                                String s = timelineEntry.getResourceStateChange().getResourceChange().toString();
                                return timelineEntry.toString() + " " + s.substring(0, Math.min(s.length(), 50));
                            }

                            @Override
                            public TimelineEntry fromString(String s) {
                                return thisObject.getTimelineEntry();
                            }
                        });
                        cb.getSelectionModel().select(originalValue);
                        cb.getSelectionModel().selectedItemProperty().addListener((observableValue, oldValue, newValue) -> {
                            if (!newValue.equals(originalValue))
                                setAllocationVal(thisObject, "timelineentry", newValue);
                        });
                        scrollComboBox(cb);
                        setGraphic(cb);
                        setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                    }
                }
            });
            titleColumn.setPrefWidth(200);

            TableColumn<Allocation, String> scoreColumn = new TableColumn<>("Score");
            scoreColumn.setCellValueFactory(x ->
                    new SimpleObjectProperty<>(scoreString(x.getValue())));
            scoreColumn.setPrefWidth(100);

            TableColumn<Allocation, ResourceElementMap> resourceColumn = new TableColumn<>("Resource Change");
            resourceColumn.setCellValueFactory(x ->
                    new SimpleObjectProperty<>(x.getValue().getTimelineEntry().getResourceStateChange().getResourceChange()));
            resourceColumn.setPrefWidth(200);

            editor_table.getColumns().addAll(Arrays.asList(
                    idxColumn, rowColumn, idColumn, percentColumn, startdateColumn, enddateColumn, durationColumn, titleColumn, scoreColumn, resourceColumn
            ));
            editor_table.getColumns().forEach(x -> x.setSortable(true));
            editor_table.getSelectionModel().selectedItemProperty().addListener(
                    (observableValue, oldValue, newValue) -> {
                        editor_textarea.setText(detailString(newValue));
                    });
            // print sort order on change
            editor_table.getSortOrder().addListener((ListChangeListener<TableColumn<Allocation, ?>>) change -> {
                Platform.runLater(() -> editor_table.scrollTo(editor_table.getSelectionModel().getFocusedIndex()));
            });

            // Table Context Menu
            MenuItem mi0 = new MenuItem("Add");
            mi0.setOnAction(e -> addAction(e, editor_table.getSelectionModel().getSelectedItem()));
            MenuItem mi1 = new MenuItem("Copy");
            mi1.setOnAction(e -> copyAction(e, editor_table.getSelectionModel().getSelectedItem()));
            MenuItem mi2 = new MenuItem("Cut");
            mi2.setOnAction(e -> cutAction(e, editor_table.getSelectionModel().getSelectedItem()));
            MenuItem mi3 = new MenuItem("Paste Below");
            mi3.setOnAction(e -> pasteAction(e, editor_table.getSelectionModel().getSelectedItem()));
            MenuItem mi4 = new MenuItem("Delete");
            mi4.setOnAction(e -> deleteAction(e, editor_table.getSelectionModel().getSelectedItem()));
            MenuItem mi5 = new MenuItem("View Calendar");
            mi5.setOnAction(e -> viewCalendarAction(e, editor_table.getSelectionModel().getSelectedItem()));
            MenuItem mi6 = new MenuItem("View Resource");
            mi6.setOnAction(e -> viewResourceAction(e, editor_table.getSelectionModel().getSelectedItem()));

            ContextMenu menu = new ContextMenu();
            menu.getItems().addAll(mi0, mi1, mi2, mi3, mi4, mi5, mi6);
            editor_table.setContextMenu(menu);

            // Timeline Calendar
            calendar_resolved = new Calendar("Resolved");
            calendar_resolved.setShortName("S");
//            calendar_resolved.setReadOnly(true);
            calendar_resolved.setStyle(Calendar.Style.STYLE1);
            calendar_unresolved = new Calendar("Unresolved");
            calendar_unresolved.setShortName("U");
//            calendar_unresolved.setReadOnly(true);
            calendar_unresolved.setStyle(Calendar.Style.STYLE4);
            CalendarSource calendarSource = new CalendarSource("Solver");
            calendarSource.getCalendars().setAll(calendar_resolved, calendar_unresolved);
            calendar_calendarview.getCalendarSources().setAll(calendarSource);
            calendar_calendarview.setEntryDetailsPopOverContentCallback(param ->
            {
                tcEntryPopOverContentPane popOverContentPane = new tcEntryPopOverContentPane(
                        param.getPopOver(), param.getDateControl(), param.getEntry());
                return popOverContentPane;
            });
            calendar_calendarview.setEntryContextMenuCallback(param -> {
                EntryViewBase<?> entryView = param.getEntryView();
                Entry<Allocation> entry = (Entry<Allocation>) entryView.getEntry();
                ContextMenu contextMenu = new ContextMenu();
                MenuItem mi7 = new MenuItem("Add");
                mi7.setOnAction(e -> addAction(e, entry.getUserObject()));
                MenuItem mi8 = new MenuItem("Copy");
                mi8.setOnAction(e -> copyAction(e, entry.getUserObject()));
                MenuItem mi9 = new MenuItem("Cut");
                mi9.setOnAction(e -> cutAction(e, entry.getUserObject()));
                MenuItem mi10 = new MenuItem("Paste Below");
                mi10.setOnAction(e -> pasteAction(e, entry.getUserObject()));
                MenuItem mi11 = new MenuItem("Delete");
                mi11.setOnAction(e -> deleteAction(e, entry.getUserObject()));
                MenuItem mi12 = new MenuItem("View Plan");
                mi12.setOnAction(e -> viewPlanAction(e, entry.getUserObject()));
                MenuItem mi13 = new MenuItem("View Resource");
                mi13.setOnAction(e -> viewResourceAction(e, entry.getUserObject()));
                contextMenu.getItems().addAll(mi7, mi8, mi9, mi10, mi11, mi12, mi13);
                return contextMenu;
            });

            // Resource Calendar
            resource_resolved = new Calendar("Resolved");
            resource_resolved.setShortName("R");
            resource_resolved.setReadOnly(true);
            resource_resolved.setStyle(Calendar.Style.STYLE1);
            resource_unresolved = new Calendar("Unresolved");
            resource_unresolved.setShortName("R");
            resource_unresolved.setReadOnly(true);
            resource_unresolved.setStyle(Calendar.Style.STYLE4);
            CalendarSource resourceSource = new CalendarSource("Resource");
            resourceSource.getCalendars().addAll(resource_resolved, resource_unresolved);
            resource_calendarview.getCalendarSources().setAll(resourceSource);
            resource_calendarview.setEntryContextMenuCallback(param -> {
                EntryViewBase<?> entryView = param.getEntryView();
                Entry<Allocation> entry = (Entry<Allocation>) entryView.getEntry();
                ContextMenu contextMenu = new ContextMenu();
                MenuItem mi14 = new MenuItem("View Plan");
                mi14.setOnAction(e -> viewPlanAction(e, entry.getUserObject()));
                MenuItem mi15 = new MenuItem("View Calendar");
                mi15.setOnAction(e -> viewCalendarAction(e, entry.getUserObject()));
                contextMenu.getItems().addAll(mi14, mi15);
                return contextMenu;
            });
            resource_calendarview.setEntryDetailsPopOverContentCallback(param ->
            {
                tcEntryPopOverContentPane popOverContentPane = new tcEntryPopOverContentPane(
                        param.getPopOver(), param.getDateControl(), param.getEntry());
                return popOverContentPane;
            });

            // Update current periodically
            Thread updateTimeThread = new Thread(() -> {
                while (true) {
                    Platform.runLater(() -> {
                        calendar_calendarview.setToday(LocalDate.now());
                        calendar_calendarview.setTime(LocalTime.now());
                        resource_calendarview.setToday(LocalDate.now());
                        resource_calendarview.setTime(LocalTime.now());
                    });
                    try {
                        // update every 10 seconds
                        Thread.sleep(10000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });
            updateTimeThread.setPriority(Thread.MIN_PRIORITY);
            updateTimeThread.setDaemon(true);
            updateTimeThread.start();

            console_textarea.setFont(sarasa_font);
            editor_textarea.setFont(sarasa_font);

            resourceReserveSeries = new XYChart.Series<>();
            resourceReserveSeries.setName("reserve");
            resourceDeficitSeries = new XYChart.Series<>();
            resourceDeficitSeries.setName("deficit");
            resourceLineChart.setAnimated(false);
            resourceLineChart.getData().addAll(resourceReserveSeries, resourceDeficitSeries);
            resourceLineChart.setOnScroll((ScrollEvent event) -> {
                ZonedDateTimeAxis zonedDateTimeAxis = ((ZonedDateTimeAxis) resourceLineChart.getXAxis());
                long delta = (long) event.getDeltaX() * 24 * 60 / 7;
                zonedDateTimeAxis.setLowerBound(zonedDateTimeAxis.getLowerBound().minusMinutes(delta));
                zonedDateTimeAxis.setUpperBound(zonedDateTimeAxis.getUpperBound().minusMinutes(delta));
            });
            resource_calendarview.getBoundDateControls().forEach(x -> x.getSelections().addListener(
                    (SetChangeListener<Entry<?>>) change -> {
                        if (change.getElementAdded() != null) {
                            Entry<Allocation> selected = (Entry<Allocation>) change.getElementAdded();
                            ResourceTotalKey resourceTotalKey = allocationResourceEntryMap.get(selected.getUserObject()).inverse().get(selected);
                            Platform.runLater(() -> {
                                resourceReserveSeries.getData().clear();
                                resourceDeficitSeries.getData().clear();
                                displayingAllocations.stream()
                                        .filter(y -> y != null && y.getTimelineEntry().getDeltaResourceTotalKeys().contains(resourceTotalKey))
                                        .forEach(z -> {

                                            XYChart.Data<ZonedDateTime, Number> reserve = new XYChart.Data<>(
                                                    z.getStartDate(),
                                                    z.getRealResourceReserve(resourceTotalKey));
                                            XYChart.Data<ZonedDateTime, Number> deficit = new XYChart.Data<>(
                                                    z.getStartDate(),
                                                    z.getRealResourceDeficit(resourceTotalKey));
                                            resourceReserveSeries.getData().add(reserve);
                                            resourceDeficitSeries.getData().add(deficit);
                                            // must set node properties AFTER adding to series
                                            if (z == selected.getUserObject()) {
                                                for (XYChart.Data data : Arrays.asList(reserve, deficit)) {
                                                    data.getNode().setStyle("-fx-background-color: #860061, white;\n"
                                                            + "    -fx-background-insets: 0, 2;\n"
                                                            + "    -fx-background-radius: 5px;\n"
                                                            + "    -fx-padding: 5px;");
                                                }
                                            }
                                            reserve.getNode().setOnMouseClicked((MouseEvent event) ->
                                                    resource_calendarview.getSelectedPage().showEntry(allocationResourceEntryMap.get(z).get(resourceTotalKey)));
                                            deficit.getNode().setOnMouseClicked((MouseEvent event) ->
                                                    resource_calendarview.getSelectedPage().showEntry(allocationResourceEntryMap.get(z).get(resourceTotalKey)));
                                        });
                                resourceLineChart.getYAxis().setAutoRanging(true);
                                resourceLineChart.getXAxis().setAutoRanging(false);
                                ((ZonedDateTimeAxis) resourceLineChart.getXAxis())
                                        .setLowerBound(selected.getUserObject().getStartDate().minusDays(7));
                                ((ZonedDateTimeAxis) resourceLineChart.getXAxis())
                                        .setUpperBound(selected.getUserObject().getEndDate().plusDays(7));
                            });
                        }
                    }));
        });
    }

    private void initializeScoreDirector() {
        SolverFactory<Schedule> solverFactory = SolverFactory.createFromXmlResource("solverPhase1.xml");
        guiScoreDirector = solverFactory.getScoreDirectorFactory().buildScoreDirector();
    }

    public void setOnlineGui() {
        Platform.runLater(() -> {
            start_btn.disableProperty().setValue(false);
            stop_btn.disableProperty().setValue(false);
            reset_btn.disableProperty().setValue(false);
            refresh_btn.disableProperty().setValue(false);
            update_toggle.disableProperty().setValue(false);
        });
    }

    public void setOfflineGui() {
        Platform.runLater(() -> {
            start_btn.disableProperty().setValue(true);
            stop_btn.disableProperty().setValue(true);
            reset_btn.disableProperty().setValue(true);
            refresh_btn.disableProperty().setValue(true);
            update_toggle.disableProperty().setValue(true);
        });
    }

    public void refreshScheduleRemote() {
        Platform.runLater(() -> primaryStage.getScene().setCursor(Cursor.WAIT));
        try {
            guiSchedule = application.getRmiInterface().getCurrentSchedule();
            status_text.setText("Last Updated (remote): " +
                    new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date()));
            populateSchedule();
            Platform.runLater(() -> primaryStage.getScene().setCursor(Cursor.DEFAULT));
        } catch (Exception e) {
            System.out.println(Throwables.getStackTraceAsString(e));
//            showOperationForbiddenAlert(Throwables.getStackTraceAsString(e));
        }
    }

    public void refreshScheduleLocal(Schedule schedule) {
        Platform.runLater(() -> primaryStage.getScene().setCursor(Cursor.WAIT));
        guiSchedule = schedule;
        if (guiSchedule == null) return;
        Platform.runLater(() -> status_text.setText("Last Updated (local): " +
                new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date())));
        populateSchedule();
        Platform.runLater(() -> primaryStage.getScene().setCursor(Cursor.DEFAULT));
    }

    private void initializeSchedule() {
        initializeExampleFiles();
        exampleTimelineBlock = timelineBlock;
        exampleTimeHierarchyMap = timeHierarchyMap;
        exampleLocationHierarchyMap = locationHierarchyMap;
        exampleValueEntryMap = valueEntryMap;
        guiSchedule = new DataStructureBuilder(
                exampleValueEntryMap, exampleTimelineBlock, exampleTimeHierarchyMap, exampleLocationHierarchyMap, false)
                .constructChainProperty().getSchedule();
    }

    private void populateSchedule() {
        if (guiSchedule == null) return;
        Platform.runLater(() -> primaryStage.getScene().setCursor(Cursor.WAIT));

        // Initialize Score Director
        guiScoreDirector.setWorkingSolution(guiSchedule);
        guiScoreDirector.triggerVariableListeners();
        guiScoreDirector.calculateScore();

        ObservableList<Allocation> guiAllocationList = FXCollections.observableArrayList(guiSchedule.getAllocationList());

        // Initialize Calendar
        allocationCalendarEntryMap = new HashMap<>();
        allocationResourceEntryMap = new HashMap<>();
        calendarEntryIntervalListenerMap = new HashMap<>();
        guiSchedule.getAllocationList().forEach(x -> allocationCalendarEntryMap.put(x, new Entry<>()));
        guiSchedule.getAllocationList().forEach(x -> allocationResourceEntryMap.put(x, HashBiMap.create()));
        calendar_resolved.clear();
        calendar_unresolved.clear();
        resource_resolved.clear();
        resource_unresolved.clear();

        // Initialize FilteredList
        forceDisplayingAllocations = new HashSet<>();
        displayingAllocations = new FilteredList<>(guiAllocationList);
        SortedList<Allocation> sortedAllocations = new SortedList<>(displayingAllocations);
        sortedAllocations.comparatorProperty().bind(editor_table.comparatorProperty());

        refreshDisplay();

        // Display Score & Explanation
        Platform.runLater(() -> explanation_textarea.setText(guiScoreDirector.explainScore()));

        // Initialize Resource
        Platform.runLater(() ->

        {
            // populate table value
            editor_table.setItems(sortedAllocations);
            editor_table.refresh();
        });

        Platform.runLater(() -> status_text.setText("Refreshed: " + guiScoreDirector.calculateScore().

                toShortString()));
        Platform.runLater(() -> primaryStage.getScene().

                setCursor(Cursor.DEFAULT));

    }

    private void refreshDisplay() {
        Platform.runLater(() -> {
            guiScoreDirector.calculateScore();
            solving_progress.setProgress(
                    1 - (double) Schedule.unsolvedEntityCount(guiSchedule) /
                            guiSchedule.focusedAllocationSet.size());
            displayingAllocations.setPredicate(null);
            displayingAllocations.setPredicate(x -> x.isFocused() || forceDisplayingAllocations.contains(x));

            calendarEntryIntervalListenerMap.forEach((k, v) -> k.intervalProperty().removeListener(v));
            allocationCalendarEntryMap.forEach((key, value) -> {
                if (!displayingAllocations.contains(key) || !key.isFocused()) {
                    value.setCalendar(null);
                } else {
                    value.setTitle(key.toString());
                    value.setUserObject(key);
                    value.setInterval(
                            key.getStartDate().withZoneSameInstant(ZoneId.systemDefault()),
                            key.getEndDate().withZoneSameInstant(ZoneId.systemDefault()));
                    value.setLocation(key.getTimelineEntry().getHumanStateChange().getCurrentLocation());
                    ChangeListener<Interval> intervalChangeListener = (observable, oldValue, newValue) -> {
                        setAllocationVal(key, "progressdelta", (int) Math.min(100,
                                (100 * key.getTimelineEntry().getProgressChange().getProgressDelta() *
                                        (newValue.getDuration().toMinutes() /
                                                key.getTimelineEntry().getHumanStateChange().getDuration()))));
                        setAllocationVal(key, "start_date", newValue.getStartDateTime());

                    };
                    calendarEntryIntervalListenerMap.put(value, intervalChangeListener);
                    value.intervalProperty().addListener(intervalChangeListener);
                    value.setCalendar(scoreString(key).equals("") ? calendar_resolved : calendar_unresolved);
                }
            });

            allocationResourceEntryMap.forEach((key, value) -> {
                if (!displayingAllocations.contains(key) || !key.isFocused()) {
                    value.values().forEach(x -> x.setCalendar(null));
                } else {
                    value.entrySet().stream().filter((entry) ->
                            !key.getResourceTotal().containsKey(entry.getKey()))
                            .forEach(entry -> {
                                entry.getValue().setCalendar(null);
                                value.remove(entry.getKey());
                            });
                    key.getThisResourceTotal().entrySet().forEach(x -> {
                        double reserve = x.getValue().getRealReserveAmt();
                        double deficit = x.getValue().getRealDeficitAmt();
                        if (reserve == 0 && deficit == 0) return;
                        String title = x.getKey().getResourceName() + "["
                                + ("+" + reserve) + " "
                                + (deficit < 0 ? deficit : "") + "]";
                        if (!value.containsKey(x.getKey())) value.put(x.getKey(), new Entry<>());
                        Entry<Allocation> existingEntry = value.get(x.getKey());
                        existingEntry.setTitle(title);
                        existingEntry.setInterval(
                                key.getStartDate().withZoneSameInstant(ZoneId.systemDefault()),
                                key.getEndDate().withZoneSameInstant(ZoneId.systemDefault()));
                        existingEntry.setUserObject(key);
                        existingEntry.setCalendar(
                                deficit < 0 ||
                                        reserve > key.getSchedule().getValueEntryMap()
                                                .get(x.getKey().getResourceName()).getCapacity()
                                        ? resource_unresolved : resource_resolved);
                    });
                }
            });

            status_text.setText("Current Score: " + guiScoreDirector.calculateScore().toShortString());
            calendar_calendarview.refreshData();
            resource_calendarview.refreshData();
        });
    }

    // Operations
    private void setAllocationVal(Allocation thisObject, String propertyName, Object newValue) {
        if (currentAllocation != thisObject) pendingAllocationValues.clear();
        currentAllocation = thisObject;
        if (propertyName.equals("progressdelta") && newValue instanceof Integer) {
            pendingAllocationValues.putIfAbsent(thisObject, new AllocationValues());
            pendingAllocationValues.get(thisObject).setProgressDelta((Integer) newValue);
        }
        if (propertyName.equals("start_date") && newValue instanceof LocalDateTime) {
            pendingAllocationValues.putIfAbsent(thisObject, new AllocationValues());
            pendingAllocationValues.get(thisObject).setPlanningStartDate(
                    ((LocalDateTime) newValue).atZone(thisObject.getStartDate().getZone()));
        }
        if (propertyName.equals("end_date") && newValue instanceof LocalDateTime) {
            pendingAllocationValues.putIfAbsent(thisObject, new AllocationValues());
            pendingAllocationValues.get(thisObject).setPlanningStartDate(
                    ((LocalDateTime) newValue).atZone(thisObject.getStartDate().getZone())
                            .minus(thisObject.getPlannedDuration()));
        }
        if (propertyName.equals("timelineentry") && newValue instanceof TimelineEntry) {
            guiSchedule.getTimelineEntry2AllocationIdxMap().get(newValue).stream()
                    .map(x -> guiSchedule.getAllocationList().get(x))
                    .filter(x -> !x.isFocused()).findFirst().ifPresent(x ->
                    pendingAllocationValues.putIfAbsent(x,
                            new AllocationValues().extract(thisObject)));
            pendingAllocationValues.putIfAbsent(thisObject, new AllocationValues());
            pendingAllocationValues.get(thisObject).setPlanningStartDate(null);
        }
        commit_btn.setVisible(true);
        cancel_btn.setVisible(true);
    }


    // Actions
    public void replaceScheduleAction(ActionEvent actionEvent) throws IOException {
        if (tab_pane.getSelectionModel().getSelectedItem().equals(tab_rawdata)) {
            var selected = rawdata_selecteddata.getSelectionModel().getSelectedItem();
            Map<String, Map> updatedfiles = new ObjectMapper().readValue(rawdata_textarea.getText(), Map.class);
            if (selected.equals("TimelineBlock")) {
                exampleTimelineBlock = new ObjectMapper().convertValue(updatedfiles, TimelineBlock.class);
            }
            if (selected.equals("TimeHierarchyMap")) {
                exampleTimeHierarchyMap = new ObjectMapper().convertValue(updatedfiles, TimeHierarchyMap.class);
            }
            if (selected.equals("LocationHierarchyMap")) {
                exampleLocationHierarchyMap = new ObjectMapper().convertValue(updatedfiles, LocationHierarchyMap.class);
            }
            if (selected.equals("ValueEntryMap")) {
                exampleValueEntryMap = new ObjectMapper().convertValue(updatedfiles, ValueEntryMap.class);
            }
            guiSchedule = new DataStructureBuilder(
                    exampleValueEntryMap, exampleTimelineBlock, exampleTimeHierarchyMap, exampleLocationHierarchyMap, false)
                    .constructChainProperty().getSchedule();
        }
        refreshDisplay();
    }

    public boolean commitAction(ActionEvent actionEvent) {
        List<Allocation> allocationList = new ArrayList<>();
        List<AllocationValues> allocationValuesList = new ArrayList<>();
        pendingAllocationValues.forEach((k, v) -> {
            allocationList.add(k);
            allocationValuesList.add(v);
        });
        SetValueMove currentMove = new SetValueMove(
                "UserMove",
                allocationList,
                allocationValuesList
        );
        if (currentMove.isMoveDoable(guiScoreDirector)) {
            undoStack.push((SetValueMove) currentMove.doMove(guiScoreDirector));
        } else {
            showOperationForbiddenAlert("Not Doable:" + currentMove);
        }

        pendingAllocationValues.clear();
        refreshDisplay();
        commit_btn.setVisible(false);
        cancel_btn.setVisible(false);
        return true;
    }

    public void cancelAction(ActionEvent actionEvent) {
        pendingAllocationValues.clear();
        refreshDisplay();
        commit_btn.setVisible(false);
        cancel_btn.setVisible(false);
    }

    public void undoAction(ActionEvent actionEvent) {
        if (!undoStack.empty() && undoStack.peek().isMoveDoable(guiScoreDirector)) {
            redoStack.push((SetValueMove) undoStack.pop().doMove(guiScoreDirector));
            refreshDisplay();
        } else {
            showOperationForbiddenAlert("Not Doable");
        }
    }

    public void redoAction(ActionEvent actionEvent) {
        if (!redoStack.empty() && redoStack.peek().isMoveDoable(guiScoreDirector)) {
            undoStack.push((SetValueMove) redoStack.pop().doMove(guiScoreDirector));
            refreshDisplay();
        } else {
            showOperationForbiddenAlert("Not Doable");
        }
    }


    public void addAction(ActionEvent actionEvent, Allocation item) {
        guiSchedule.getAllocationList().stream().filter(x -> !x.isFocused())
                .findFirst().ifPresent(x -> {
            pendingAllocationValues.put(x, new AllocationValues().setPlanningStartDate(item.getEndDate()));
            forceDisplayingAllocations.add(x);
            commitAction(actionEvent);
        });
        refreshDisplay();
    }

    public void cutAction(ActionEvent actionEvent, Allocation item) {
        clipboard.clear();
        clipboard.put(item, new AllocationValues().extract(item));
        SetValueMove setValueMove = new SetValueMove(
                "UserMove",
                Collections.singletonList(item),
                Collections.singletonList(new AllocationValues().setPlanningStartDate(null))
        );
        if (setValueMove.isMoveDoable(guiScoreDirector)) {
            undoStack.push((SetValueMove) setValueMove.doMove(guiScoreDirector));
            forceDisplayingAllocations.remove(item);
            refreshDisplay();
        } else {
            showOperationForbiddenAlert("Not Doable");
        }
    }

    public void copyAction(ActionEvent actionEvent, Allocation item) {
        clipboard.clear();
        clipboard.put(item, new AllocationValues().extract(item));
    }

    public void pasteAction(ActionEvent actionEvent, Allocation item) {
        for (Map.Entry<Allocation, AllocationValues> entry : clipboard.entrySet()) {
            guiSchedule.getTimelineEntry2AllocationIdxMap().get(entry.getKey().getTimelineEntry())
                    .stream().map(x -> guiSchedule.getAllocationList().get(x))
                    .filter(x -> !x.isFocused())
                    .findFirst().ifPresent(x -> {
                SetValueMove setValueMove = new SetValueMove(
                        "UserMove",
                        Arrays.asList(entry.getKey()),
                        Arrays.asList(entry.getValue().setPlanningStartDate(item.getEndDate())));
                if (setValueMove.isMoveDoable(guiScoreDirector)) {
                    undoStack.push((SetValueMove) setValueMove.doMove(guiScoreDirector));
                    forceDisplayingAllocations.add(x);
                    refreshDisplay();
                } else {
                    showOperationForbiddenAlert("Not Doable");
                }
            });
        }
    }

    public void deleteAction(ActionEvent actionEvent, Allocation item) {
        SetValueMove setValueMove = new SetValueMove(
                "UserMove",
                Collections.singletonList(item),
                Collections.singletonList(new AllocationValues().setPlanningStartDate(null))
        );
        if (setValueMove.isMoveDoable(guiScoreDirector)) {
            undoStack.push((SetValueMove) setValueMove.doMove(guiScoreDirector));
            forceDisplayingAllocations.remove(item);
            refreshDisplay();
        } else {
            showOperationForbiddenAlert("Not Doable");
        }
    }

    public void viewCalendarAction(ActionEvent actionEvent, Allocation item) {
        tab_pane.getSelectionModel().select(tab_calendar);
        calendar_calendarview.showWeekPage();
        try {
            Thread.sleep(900);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        calendar_calendarview.getSelectedPage().showEntry(allocationCalendarEntryMap.get(item));
    }

    public void viewResourceAction(ActionEvent actionEvent, Allocation item) {
        tab_pane.getSelectionModel().select(tab_resource);
        resource_calendarview.showWeekPage();
        try {
            Thread.sleep(900);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        resource_calendarview.getSelectedPage().showEntry(allocationResourceEntryMap.get(item).values().iterator().next());
    }

    public void viewPlanAction(ActionEvent actionEvent, Allocation item) {
        tab_pane.getSelectionModel().select(tab_planner);
        resource_calendarview.showWeekPage();
        editor_table.getSelectionModel().select(item);
        editor_table.scrollTo(item);
    }

    // Tools
    private void showOperationForbiddenAlert(String detail) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(detail);
        alert.showAndWait();
    }

    private String dateString(Allocation allocation) {
        if (allocation == null || allocation.getStartDate() == null) return "";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd HH:mm");
        String datetime = formatter.format(
                allocation.getStartDate().withZoneSameInstant(ZoneId.systemDefault())) + " ~ " +
                formatter.format(
                        allocation.getEndDate().withZoneSameInstant(ZoneId.systemDefault()));
        return datetime;
    }

    private String scoreString(Allocation allocation) {
        if (allocation == null) return "";
        HardMediumSoftLongScore thisScore = null;
        if (guiScoreDirector.getIndictmentMap().containsKey(allocation)) {
            thisScore = (HardMediumSoftLongScore) guiScoreDirector.getIndictmentMap().get(allocation).getScore();
        }
        return thisScore != null && thisScore.getHardScore() != 0 ?
                thisScore.toShortString() : "";
    }

    private String detailString(Allocation allocation) {
        if (allocation == null || allocation.getStartDate() == null) return "";
        Toolbox.PrettyPrintAlloc printAlloc = new Toolbox.PrettyPrintAlloc(guiScoreDirector);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return printAlloc.prettyAllocation(allocation) + "\n" +
                printAlloc.indictmentStr(allocation) + "\n" +
                printAlloc.resourceTotalStr(allocation) + "\n" +
                gson.toJson(allocation.getTimelineEntry()) + "\n" +
                gson.toJson(allocation.getResourceTotal());
    }

    public static String getLastLines(String string, int numLines) {
        List<String> lines = Arrays.asList(string.split(System.lineSeparator()));
        return String.join("", lines.subList(Math.max(0, lines.size() - numLines), lines.size()));
    }

    private void scrollComboBox(ComboBox cb) {
        try {
            ListView list = (ListView) ((ComboBoxListViewSkin) cb.getSkin()).getPopupContent();
            list.scrollTo(Math.max(0, cb.getSelectionModel().getSelectedIndex()));
        } catch (Exception ignore) {
        }
    }

    public TCApp getApplication() {
        return application;
    }

    public void setApplication(TCApp application) {
        this.application = application;
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    public void QuitApp(ActionEvent actionEvent) {
        Platform.exit();
    }
}
