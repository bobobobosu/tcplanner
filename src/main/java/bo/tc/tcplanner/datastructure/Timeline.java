package bo.tc.tcplanner.datastructure;

import bo.tc.tcplanner.persistable.AbstractPersistable;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public class Timeline extends AbstractPersistable {
    private List<TimelineEntry> timelineEntryList;
    private String owner;
    private Integer blockStartId;
    private Integer blockEndId;


    @Override
    public Timeline removeVolatile() {
        timelineEntryList.forEach(TimelineEntry::removeVolatile);
        return this;
    }

    @Override
    public Timeline removeEmpty() {
        timelineEntryList.forEach(TimelineEntry::removeEmpty);
        return this;
    }

    @Override
    public boolean checkValid() {
        try {
            checkNotNull(timelineEntryList);
            checkNotNull(owner);
            checkArgument(timelineEntryList.stream().allMatch(TimelineEntry::checkValid));
            return true;
        } catch (IllegalArgumentException | NullPointerException ex) {
            throw new IllegalArgumentException(this.toString(), ex);
        }
    }

    public List<TimelineEntry> getTimelineEntryList() {
        return timelineEntryList;
    }

    public void setTimelineEntryList(List<TimelineEntry> timelineEntryList) {
        this.timelineEntryList = timelineEntryList;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public Integer getBlockStartId() {
        return blockStartId;
    }

    public Timeline setBlockStartId(Integer blockStartId) {
        this.blockStartId = blockStartId;
        return this;
    }

    public Integer getBlockEndId() {
        return blockEndId;
    }

    public Timeline setBlockEndId(Integer blockEndId) {
        this.blockEndId = blockEndId;
        return this;
    }

    public TimelineBlock getTimelineBlock(Duration planningAhead) {
        checkValid();
        boolean inBlock = false;
        List<TimelineEntry> timelineEntryListBlock = new ArrayList<>();
        for (TimelineEntry timelineEntry : timelineEntryList) {
            if (timelineEntry.getTimelineProperty().getTimelineid().equals(blockStartId)) inBlock = true;
            if (inBlock) timelineEntryListBlock.add(timelineEntry);
            if (timelineEntry.getTimelineProperty().getTimelineid().equals(blockEndId)) inBlock = true;
        }

        return new TimelineBlock()
                .setTimelineEntryList(timelineEntryListBlock)
                .setBlockStartTime(timelineEntryListBlock.get(0).getChronoProperty().getStartTime())
                .setBlockEndTime(timelineEntryListBlock.get(timelineEntryListBlock.size() - 1).getChronoProperty().getStartTime())
                .setBlockScheduleAfter(ZonedDateTime.now().toString());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        Timeline timeline = (Timeline) o;

        if (!timelineEntryList.equals(timeline.timelineEntryList)) return false;
        if (!owner.equals(timeline.owner)) return false;
        if (!blockStartId.equals(timeline.blockStartId)) return false;
        return blockEndId.equals(timeline.blockEndId);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + timelineEntryList.hashCode();
        result = 31 * result + owner.hashCode();
        result = 31 * result + blockStartId.hashCode();
        result = 31 * result + blockEndId.hashCode();
        return result;
    }
}
