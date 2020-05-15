package bo.tc.tcplanner.Gui;

import bo.tc.tcplanner.datastructure.TimelineEntry;
import bo.tc.tcplanner.domain.planningstructures.Allocation;
import com.calendarfx.model.Entry;
import com.calendarfx.view.CalendarView;
import com.calendarfx.view.DateControl;
import com.calendarfx.view.Messages;
import com.calendarfx.view.popover.EntryHeaderView;
import com.calendarfx.view.popover.EntryPropertiesView;
import com.calendarfx.view.popover.PopOverContentPane;
import com.calendarfx.view.popover.PopOverTitledPane;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.util.Duration;
import org.controlsfx.control.PopOver;

import java.util.Objects;

import static bo.tc.tcplanner.Gui.JsonTreeItem.createTree;

public class tcEntryPopOverContentPane extends PopOverContentPane {

    private Entry<?> entry;
    private DateControl dateControl;
    private PopOver popOver;

    public tcEntryPopOverContentPane(PopOver popOver, DateControl dateControl, Entry<?> entry) {
        getStylesheets().add(CalendarView.class.getResource("calendar.css").toExternalForm()); //$NON-NLS-1$

        this.popOver = popOver;
        this.dateControl = dateControl;
        this.entry = Objects.requireNonNull(entry);

        tcEntryDetailsView details = new tcEntryDetailsView(entry);

        PopOverTitledPane detailsPane = new PopOverTitledPane(Messages.getString("EntryPopOverContentPane.DETAILS"), //$NON-NLS-1$
                details);

        EntryHeaderView header = new EntryHeaderView(entry, dateControl.getCalendars());
        setHeader(header);


        JsonElement root = new Gson().toJsonTree(
                ((Entry<Allocation>) entry).getUserObject().getTimelineEntry(), TimelineEntry.class);
        TreeItem<JsonTreeItem.Value> treeRoot = createTree(root);
        treeRoot.setExpanded(true);
        TreeView<JsonTreeItem.Value> treeView = new TreeView<>(treeRoot);
        treeView.setCellFactory(tv -> new TreeCell<JsonTreeItem.Value>() {
            @Override
            protected void updateItem(JsonTreeItem.Value item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText("");
                } else {
                    setText(item.text);
                }
            }
        });
        PopOverTitledPane timelineEntryPane = new PopOverTitledPane("TimelineEntry", treeView);

        EntryPropertiesView properties = new EntryPropertiesView(entry);
        PopOverTitledPane propertiesPane = new PopOverTitledPane("Properties", properties);

        getPanes().addAll(detailsPane, timelineEntryPane, propertiesPane);

        setExpandedPane(detailsPane);

        entry.calendarProperty().addListener(it -> {
            if (entry.getCalendar() == null) {
                popOver.hide(Duration.ZERO);
            }
        });
    }

    public final PopOver getPopOver() {
        return popOver;
    }

    public final DateControl getDateControl() {
        return dateControl;
    }

    public final Entry<?> getEntry() {
        return entry;
    }
}
