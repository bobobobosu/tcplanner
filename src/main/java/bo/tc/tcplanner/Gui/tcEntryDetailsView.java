package bo.tc.tcplanner.Gui;

import bo.tc.tcplanner.domain.planningstructures.Allocation;
import com.calendarfx.model.Entry;
import com.calendarfx.view.Messages;
import com.calendarfx.view.TimeField;
import com.calendarfx.view.popover.EntryPopOverPane;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;

public class tcEntryDetailsView extends EntryPopOverPane {

    public tcEntryDetailsView(Entry<?> entry) {
        super();

        getStyleClass().add("entry-details-view");

        Label progressLabel = new Label(); //$NON-NLS-1$
        Label startDateLabel = new Label(Messages.getString("EntryDetailsView.FROM")); //$NON-NLS-1$
        Label endDateLabel = new Label(Messages.getString("EntryDetailsView.TO")); //$NON-NLS-1$

        Slider progressSlider = new Slider();
        progressSlider.disableProperty().set(true);
        progressSlider.setMin(0);
        progressSlider.setMax(100);
        progressSlider.setValue(entry.getUserObject() == null ?
                100 : ((Entry<Allocation>) entry).getUserObject().getProgressdelta());
        progressSlider.setShowTickLabels(true);
        progressSlider.setShowTickMarks(true);
        progressSlider.setMajorTickUnit(10);
        progressSlider.setMinorTickCount(1);

        TimeField startTimeField = new TimeField();
        startTimeField.setValue(entry.getStartTime());

        TimeField endTimeField = new TimeField();
        endTimeField.setValue(entry.getEndTime());

        DatePicker startDatePicker = new DatePicker();
        startDatePicker.setValue(entry.getStartDate());

        DatePicker endDatePicker = new DatePicker();
        endDatePicker.setValue(entry.getEndDate());

        entry.intervalProperty().addListener(it -> {
            startTimeField.setValue(entry.getStartTime());
            endTimeField.setValue(entry.getEndTime());
            startDatePicker.setValue(entry.getStartDate());
            endDatePicker.setValue(entry.getEndDate());
        });

        HBox startDateBox = new HBox(10);
        HBox endDateBox = new HBox(10);

        startDateBox.setAlignment(Pos.CENTER_LEFT);
        endDateBox.setAlignment(Pos.CENTER_LEFT);

        startDateBox.getChildren().addAll(startDateLabel, startDatePicker, startTimeField);
        endDateBox.getChildren().addAll(endDateLabel, endDatePicker, endTimeField);

        GridPane box = new GridPane();
        box.getStyleClass().add("content"); //$NON-NLS-1$
        box.add(progressLabel, 0, 0);
        box.add(progressSlider, 1, 0);
        box.add(startDateLabel, 0, 1);
        box.add(startDateBox, 1, 1);
        box.add(endDateLabel, 0, 2);
        box.add(endDateBox, 1, 2);

        ColumnConstraints col1 = new ColumnConstraints();
        ColumnConstraints col2 = new ColumnConstraints();

        col1.setHalignment(HPos.RIGHT);
        col2.setHalignment(HPos.LEFT);

        box.getColumnConstraints().addAll(col1, col2);

        getChildren().add(box);
    }
}
