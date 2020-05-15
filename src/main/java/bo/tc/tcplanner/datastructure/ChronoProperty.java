package bo.tc.tcplanner.datastructure;

import bo.tc.tcplanner.persistable.AbstractPersistable;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.firebase.database.Exclude;
import org.jetbrains.annotations.Nullable;

import java.time.ZonedDateTime;
import java.util.Objects;

import static bo.tc.tcplanner.FunctionConstants.ZonedDateTimeParseCache;
import static com.google.common.base.Preconditions.checkNotNull;

public class ChronoProperty extends AbstractPersistable {
    // Properties of timeline
    @Nullable
    private String startTime;
    @Nullable
    private String deadline;
    @Nullable
    private String aliveline;
    @Nullable
    private Integer priority;

    // Common properties
    private Integer draggable;
    private Integer substitutable;
    private Integer splittable;
    private Integer gravity;

    public ChronoProperty() {
        super();
    }

    public ChronoProperty(ChronoProperty other) {
        super(other);
        this.setStartTime(other.startTime);
        this.setDeadline(other.deadline);
        this.setAliveline(other.aliveline);
        this.setPriority(other.priority);
        this.setGravity(other.gravity);
        this.setDraggable(other.draggable);
        this.setSubstitutable(other.substitutable);
        this.setSplittable(other.splittable);
    }


    @Override
    public ChronoProperty removeVolatile() {
        return this;
    }

    @Override
    public ChronoProperty removeEmpty() {
        return this;
    }

    @Override
    public boolean checkValid() {
        try {
            checkNotNull(draggable);
            checkNotNull(splittable);
            checkNotNull(substitutable);
            checkNotNull(gravity);
            return true;
        } catch (IllegalArgumentException | NullPointerException ex) {
            throw new IllegalArgumentException(this.toString(), ex);
        }
    }

    @JsonIgnore
    @Exclude
    public ZonedDateTime getZonedStartTime() {
        if (startTime == null) return null;
        return ZonedDateTimeParseCache.computeIfAbsent(startTime, k -> ZonedDateTime.parse(startTime));
    }

    @JsonIgnore
    @Exclude
    public ZonedDateTime getZonedDeadline() {
        if (deadline == null) return null;
        return ZonedDateTimeParseCache.computeIfAbsent(deadline, k -> ZonedDateTime.parse(deadline));
    }

    @JsonIgnore
    @Exclude
    public ZonedDateTime getZonedAliveline() {
        if (deadline == null) return null;
        return ZonedDateTimeParseCache.computeIfAbsent(aliveline, k -> ZonedDateTime.parse(aliveline));
    }

    @Nullable
    public String getStartTime() {
        return startTime;
    }

    public ChronoProperty setStartTime(String startTime) {
        this.startTime = startTime;
        return this;
    }

    @Nullable
    public String getDeadline() {
        return deadline;
    }

    public ChronoProperty setDeadline(String deadline) {
        this.deadline = deadline;
        return this;
    }

    @Nullable
    public String getAliveline() {
        return aliveline;
    }

    public ChronoProperty setAliveline(String aliveline) {
        this.aliveline = aliveline;
        return this;
    }

    public Integer getDraggable() {
        return draggable;
    }

    public ChronoProperty setDraggable(Integer draggable) {
        this.draggable = draggable;
        return this;
    }

    public Integer getSplittable() {
        return splittable;
    }

    public ChronoProperty setSplittable(Integer splittable) {
        this.splittable = splittable;
        return this;
    }

    public Integer getSubstitutable() {
        return substitutable;
    }

    public ChronoProperty setSubstitutable(Integer substitutable) {
        this.substitutable = substitutable;
        return this;
    }

    public Integer getGravity() {
        return gravity;
    }

    public ChronoProperty setGravity(Integer gravity) {
        this.gravity = gravity;
        return this;
    }

    public Integer getPriority() {
        return priority;
    }

    public ChronoProperty setPriority(Integer priority) {
        this.priority = priority;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        ChronoProperty that = (ChronoProperty) o;
        return Objects.equals(startTime, that.startTime) &&
                Objects.equals(deadline, that.deadline) &&
                Objects.equals(aliveline, that.aliveline) &&
                Objects.equals(priority, that.priority) &&
                draggable.equals(that.draggable) &&
                substitutable.equals(that.substitutable) &&
                splittable.equals(that.splittable) &&
                gravity.equals(that.gravity);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), startTime, deadline, aliveline, priority, draggable, substitutable, splittable, gravity);
    }
}
