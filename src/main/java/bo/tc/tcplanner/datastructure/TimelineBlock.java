package bo.tc.tcplanner.datastructure;

import bo.tc.tcplanner.persistable.AbstractPersistable;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.jetbrains.annotations.Nullable;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static bo.tc.tcplanner.FunctionConstants.ZonedDateTimeParseCache;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TimelineBlock extends AbstractPersistable {
    private List<TimelineEntry> timelineEntryList;
    private String blockStartTime;
    private String blockEndTime;
    private String blockScheduleAfter;
    @Nullable
    private String origin;
    @Nullable
    private String score;

    public TimelineBlock() {
        super();
    }

    public TimelineBlock(TimelineBlock timelineBlock) {
        super(timelineBlock);
        this.timelineEntryList = timelineBlock.timelineEntryList.stream().map(x -> new TimelineEntry(x))
                .collect(Collectors.toList());
        this.setBlockStartTime(timelineBlock.blockStartTime);
        this.setBlockEndTime(timelineBlock.blockEndTime);
        this.blockScheduleAfter = timelineBlock.blockScheduleAfter;
        this.origin = timelineBlock.origin;
        this.score = timelineBlock.score;
    }


    @Override
    public boolean checkValid() {
        try {
            checkNotNull(blockStartTime);
            checkNotNull(blockStartTime);
            checkNotNull(blockEndTime);
            checkNotNull(blockScheduleAfter);
            checkNotNull(timelineEntryList);
            checkArgument(getZonedBlockEndTime().isAfter(getZonedBlockStartTime()));
            checkArgument(timelineEntryList.stream().allMatch(TimelineEntry::checkValid));
            return true;
        } catch (IllegalArgumentException | NullPointerException ex) {
            throw new IllegalArgumentException(this.toString(), ex);
        }

    }

    @Override
    public TimelineBlock removeVolatile() {
        timelineEntryList.forEach(TimelineEntry::removeVolatile);
        return this;
    }

    @Override
    public TimelineBlock removeEmpty() {
        timelineEntryList.forEach(TimelineEntry::removeEmpty);
        return this;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        TimelineBlock that = (TimelineBlock) o;

        if (!timelineEntryList.equals(that.timelineEntryList)) return false;
        if (!blockStartTime.equals(that.blockStartTime)) return false;
        if (!blockEndTime.equals(that.blockEndTime)) return false;
        if (!blockScheduleAfter.equals(that.blockScheduleAfter)) return false;
        return origin.equals(that.origin);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + timelineEntryList.hashCode();
        result = 31 * result + blockStartTime.hashCode();
        result = 31 * result + blockEndTime.hashCode();
        result = 31 * result + blockScheduleAfter.hashCode();
        result = 31 * result + origin.hashCode();
        return result;
    }

    public List<TimelineEntry> getTimelineEntryList() {
        return timelineEntryList;
    }

    public TimelineBlock setTimelineEntryList(List<TimelineEntry> timelineEntryList) {
        this.timelineEntryList = timelineEntryList;
        return this;
    }

    public String getBlockStartTime() {
        return blockStartTime;
    }

    public TimelineBlock setBlockStartTime(String blockStartTime) {
        this.blockStartTime = blockStartTime;
        return this;
    }


    public String getBlockEndTime() {
        return blockEndTime;
    }

    public TimelineBlock setBlockEndTime(String blockEndTime) {
        this.blockEndTime = blockEndTime;
        return this;
    }

    @JsonIgnore
    public ZonedDateTime getZonedBlockEndTime() {
        if (blockEndTime == null) return null;
        return ZonedDateTimeParseCache.computeIfAbsent(blockEndTime, k -> ZonedDateTime.parse(blockEndTime));
    }

    @JsonIgnore
    public ZonedDateTime getZonedBlockStartTime() {
        if (blockStartTime == null) return null;
        return ZonedDateTimeParseCache.computeIfAbsent(blockStartTime, k -> ZonedDateTime.parse(blockStartTime));
    }

    public String getOrigin() {
        return origin;
    }

    public TimelineBlock setOrigin(String origin) {
        this.origin = origin;
        return this;
    }

    public String getScore() {
        return score;
    }

    public TimelineBlock setScore(String score) {
        this.score = score;
        return this;
    }

    public String getBlockScheduleAfter() {
        return blockScheduleAfter;
    }

    public TimelineBlock setBlockScheduleAfter(String blockScheduleAfter) {
        this.blockScheduleAfter = blockScheduleAfter;
        return this;
    }

    public ZonedDateTime getZonedBlockScheduleAfter() {
        return ZonedDateTimeParseCache.computeIfAbsent(blockScheduleAfter, k -> ZonedDateTime.parse(blockScheduleAfter));
    }


}
