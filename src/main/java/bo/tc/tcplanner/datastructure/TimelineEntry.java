package bo.tc.tcplanner.datastructure;

import bo.tc.tcplanner.domain.planningstructures.ResourceTotal;
import bo.tc.tcplanner.domain.planningstructures.ResourceTotalKey;
import bo.tc.tcplanner.domain.planningstructures.ResourceTotalValue;
import bo.tc.tcplanner.persistable.AbstractPersistable;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

@JsonIgnoreProperties(ignoreUnknown = true)

public class TimelineEntry extends AbstractPersistable {
    //names
    String title;
    String description;
    //state changes
    int executionMode;
    HumanStateChange humanStateChange;
    ResourceStateChange resourceStateChange;
    //progress changes
    ProgressChange progressChange;
    //chronological property
    ChronoProperty chronoProperty;
    //timeline property
    TimelineProperty timelineProperty;
    //validation notes
    @Nullable
    String score;

    public TimelineEntry() {
        super();
    }

    public TimelineEntry(TimelineEntry timelineEntry) {
        super(timelineEntry);
        this.title = timelineEntry.title;
        this.description = timelineEntry.description;
        this.executionMode = timelineEntry.executionMode;
        this.humanStateChange = new HumanStateChange(timelineEntry.humanStateChange);
        this.resourceStateChange = new ResourceStateChange(timelineEntry.resourceStateChange);
        this.progressChange = new ProgressChange(timelineEntry.progressChange);
        this.chronoProperty = new ChronoProperty(timelineEntry.chronoProperty);
        this.timelineProperty = new TimelineProperty(timelineEntry.timelineProperty);
        if (timelineEntry.score != null) this.score = timelineEntry.score;
    }

    @Override
    public boolean checkValid() {
        try {
            checkNotNull(title);
            checkNotNull(description);
            checkArgument(executionMode >= 0);
            checkNotNull(humanStateChange);
            checkNotNull(resourceStateChange);
            checkNotNull(progressChange);
            checkNotNull(chronoProperty);
            checkNotNull(timelineProperty);
            checkArgument(humanStateChange.checkValid());
            checkArgument(resourceStateChange.checkValid());
            checkArgument(progressChange.checkValid());
            checkArgument(chronoProperty.checkValid());
            checkArgument(timelineProperty.checkValid());
            return true;
        } catch (IllegalArgumentException | NullPointerException ex) {
            throw new IllegalArgumentException(this.toString(), ex);
        }
    }


    @Override
    public TimelineEntry setVolatileFlag(boolean volatileFlag) {
        super.setVolatileFlag(volatileFlag);
        return this;
    }

    @Override
    public String toString() {
        return timelineProperty.getRownum() + " " + title + "[" + description + "]";

    }

    @Override
    public TimelineEntry removeVolatile() {
        if (humanStateChange != null) humanStateChange.removeVolatile();
        if (resourceStateChange != null) resourceStateChange.removeVolatile();
        if (progressChange != null) progressChange.removeVolatile();
        if (chronoProperty != null) chronoProperty.removeVolatile();
        if (timelineProperty != null) timelineProperty.removeVolatile();
        return this;
    }

    @Override
    public AbstractPersistable removeEmpty() {
        if (humanStateChange != null) humanStateChange.removeEmpty();
        if (resourceStateChange != null) resourceStateChange.removeEmpty();
        if (progressChange != null) progressChange.removeEmpty();
        if (chronoProperty != null) chronoProperty.removeEmpty();
        if (timelineProperty != null) timelineProperty.removeEmpty();
        return this;
    }


    public String getScore() {
        return score;
    }

    public void setScore(String score) {
        this.score = score;
    }


    public String getTitle() {
        return title;
    }

    public TimelineEntry setTitle(String title) {
        this.title = title;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public TimelineEntry setDescription(String description) {
        this.description = description;
        return this;
    }

    public int getExecutionMode() {
        return executionMode;
    }

    public TimelineEntry setExecutionMode(int executionMode) {
        this.executionMode = executionMode;
        return this;
    }

    public HumanStateChange getHumanStateChange() {
        return humanStateChange;
    }

    public TimelineEntry setHumanStateChange(HumanStateChange humanStateChange) {
        this.humanStateChange = humanStateChange;
        return this;
    }

    public ResourceStateChange getResourceStateChange() {
        return resourceStateChange;
    }

    public TimelineEntry setResourceStateChange(ResourceStateChange resourceStateChange) {
        this.resourceStateChange = resourceStateChange;
        return this;
    }

    public ProgressChange getProgressChange() {
        return progressChange;
    }

    public TimelineEntry setProgressChange(ProgressChange progressChange) {
        this.progressChange = progressChange;
        return this;
    }

    public ChronoProperty getChronoProperty() {
        return chronoProperty;
    }

    public TimelineEntry setChronoProperty(ChronoProperty chronoProperty) {
        this.chronoProperty = chronoProperty;
        return this;
    }

    public TimelineProperty getTimelineProperty() {
        return timelineProperty;
    }

    public TimelineEntry setTimelineProperty(TimelineProperty timelineProperty) {
        this.timelineProperty = timelineProperty;
        return this;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        TimelineEntry that = (TimelineEntry) o;

        if (executionMode != that.executionMode) return false;
        if (!title.equals(that.title)) return false;
        if (!description.equals(that.description)) return false;
        if (!humanStateChange.equals(that.humanStateChange)) return false;
        if (!resourceStateChange.equals(that.resourceStateChange)) return false;
        if (!progressChange.equals(that.progressChange)) return false;
        if (!chronoProperty.equals(that.chronoProperty)) return false;
        return timelineProperty.equals(that.timelineProperty);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + title.hashCode();
        result = 31 * result + description.hashCode();
        result = 31 * result + executionMode;
        result = 31 * result + humanStateChange.hashCode();
        result = 31 * result + resourceStateChange.hashCode();
        result = 31 * result + progressChange.hashCode();
        result = 31 * result + chronoProperty.hashCode();
        result = 31 * result + timelineProperty.hashCode();
        return result;
    }

    // Complex Methods
    private ResourceTotal hundrespercentResourceTotal = null; //memotization
    private Set<ResourceTotalKey> resourceTotalKeySet = null;

    @JsonIgnore
    public Set<ResourceTotalKey> getDeltaResourceTotalKeys() {
        Set<ResourceTotalKey> resourceTotalKeys;
        if (resourceTotalKeySet == null) {
            resourceTotalKeys = new HashSet<>();
            getResourceStateChange().getResourceChange().forEach((k, v) -> {
                v.forEach(x -> resourceTotalKeys.add(
                        new ResourceTotalKey()
                                .setResourceName(k)
                                .setLocation(x.getLocation())));
            });
            resourceTotalKeySet = resourceTotalKeys;
            return resourceTotalKeys;
        } else {
            return resourceTotalKeySet;
        }
    }

    // the keys of this map is incomplete(removed when value is 0, for equals() purposes),
    // use getDeltaResourceTotalKeys() to get all keys instead
    @JsonIgnore
    public ResourceTotal getDeltaResourceTotal_hundredpercent() {
        ResourceTotal resourceTotal;
        if (hundrespercentResourceTotal == null) {
            resourceTotal = new ResourceTotal();
            getResourceStateChange().getResourceChange().forEach((k, v) -> {
                v.forEach(x -> {
                    ResourceTotalKey resourceTotalKey = new ResourceTotalKey()
                            .setResourceName(k)
                            .setLocation(x.getLocation());
                    resourceTotal.addResourceWithProgress(
                            resourceTotalKey,
                            new ResourceTotalValue()
                                    .setReserveAmt(x.getAmt() > 0 ? (int) Math.round(x.getAmt() * 100) : 0)
                                    .setDeficitAmt(x.getAmt() < 0 ? (int) Math.round(x.getAmt() * 100) : 0),
                            getProgressChange().getProgressDelta100(),
                            100);
                });
            });
            hundrespercentResourceTotal = resourceTotal;
            return resourceTotal;
        } else {
            return hundrespercentResourceTotal;
        }
    }
}

