package bo.tc.tcplanner.datastructure;

import bo.tc.tcplanner.persistable.AbstractPersistable;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ProgressChange extends AbstractPersistable {
    //percentage change
    private double progressDelta;
    //progress preset
    private List<ProgressEntry> progressPreset;
    //progress log
    private List<ProgressEntry> progressLog;

    public ProgressChange(ProgressChange other) {
        super(other);
        this.progressDelta = other.progressDelta;
        if (other.progressPreset != null)
            this.progressPreset = other.getProgressPreset().stream().map(ProgressEntry::new).collect(Collectors.toList());
        if (other.progressLog != null)
            this.progressLog = other.getProgressLog().stream().map(ProgressEntry::new).collect(Collectors.toList());
    }

    public ProgressChange() {
        super();
    }

    @Override
    public ProgressChange removeVolatile() {
        return this;
    }

    @Override
    public ProgressChange removeEmpty() {
        return this;
    }

    @Override
    public boolean checkValid() {
        try {
            checkNotNull(progressLog);
            checkArgument(progressDelta >= 0);
            checkArgument(progressDelta <= 1);
            checkArgument(progressPreset.stream().allMatch(ProgressEntry::checkValid));
            checkArgument(progressLog.stream().allMatch(ProgressEntry::checkValid));
            progressLog.forEach(x -> checkNotNull(x.getStartTime(), this));
            return true;
        } catch (IllegalArgumentException | NullPointerException ex) {
            throw new IllegalArgumentException(this.toString(), ex);
        }
    }

    public double getProgressDelta() {
        return progressDelta;
    }

    public int getProgressDelta100() {
        return (int) Math.round(progressDelta * 100);
    }

    public ProgressChange setProgressDelta(double progressDelta) {
        this.progressDelta = progressDelta;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        ProgressChange that = (ProgressChange) o;

        return Double.compare(that.progressDelta, progressDelta) == 0;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        long temp;
        temp = Double.doubleToLongBits(progressDelta);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    public List<ProgressEntry> getProgressPreset() {
        return progressPreset;
    }

    public ProgressChange setProgressPreset(List<ProgressEntry> progressPreset) {
        this.progressPreset = progressPreset;
        return this;
    }

    public List<ProgressEntry> getProgressLog() {
        return progressLog;
    }

    public ProgressChange setProgressLog(List<ProgressEntry> progressLog) {
        this.progressLog = progressLog;
        return this;
    }
}