package bo.tc.tcplanner.datastructure;

import bo.tc.tcplanner.persistable.AbstractPersistable;
import org.jetbrains.annotations.Nullable;

import static com.google.common.base.Preconditions.checkNotNull;

public class ProgressEntry extends AbstractPersistable {
    @Nullable
    String startTime;
    private double percentage;
    private String milestone;
    private String task;
    private int taskCount;
    private String upMilestone;

    public ProgressEntry() {
        super();
    }

    public ProgressEntry(ProgressEntry other) {
        startTime = other.startTime;
        percentage = other.percentage;
        milestone = other.milestone;
        task = other.task;
        taskCount = other.taskCount;
        upMilestone = other.upMilestone;
    }

    public double getPercentage() {
        return percentage;
    }

    public ProgressEntry setPercentage(double percentage) {
        this.percentage = percentage;
        return this;
    }

    public String getMilestone() {
        return milestone;
    }

    public ProgressEntry setMilestone(String milestone) {
        this.milestone = milestone;
        return this;
    }

    public String getTask() {
        return task;
    }

    public ProgressEntry setTask(String task) {
        this.task = task;
        return this;
    }

    public int getTaskCount() {
        return taskCount;
    }

    public ProgressEntry setTaskCount(int taskCount) {
        this.taskCount = taskCount;
        return this;
    }

    public String getUpMilestone() {
        return upMilestone;
    }

    public ProgressEntry setUpMilestone(String upMilestone) {
        this.upMilestone = upMilestone;
        return this;
    }

    public String getStartTime() {
        return startTime;
    }

    public ProgressEntry setStartTime(String startTime) {
        this.startTime = startTime;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ProgressEntry that = (ProgressEntry) o;

        if (Double.compare(that.percentage, percentage) != 0) return false;
        if (taskCount != that.taskCount) return false;
        if (!milestone.equals(that.milestone)) return false;
        if (!task.equals(that.task)) return false;
        if (!upMilestone.equals(that.upMilestone)) return false;
        return startTime != null ? startTime.equals(that.startTime) : that.startTime == null;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        temp = Double.doubleToLongBits(percentage);
        result = (int) (temp ^ (temp >>> 32));
        result = 31 * result + milestone.hashCode();
        result = 31 * result + task.hashCode();
        result = 31 * result + taskCount;
        result = 31 * result + upMilestone.hashCode();
        result = 31 * result + (startTime != null ? startTime.hashCode() : 0);
        return result;
    }

    @Override
    public ProgressEntry removeVolatile() {
        return this;
    }

    @Override
    public ProgressEntry removeEmpty() {
        return this;
    }

    @Override
    public boolean checkValid() {
        try {
            checkNotNull(milestone);
            checkNotNull(task);
            checkNotNull(upMilestone);
            return true;
        } catch (IllegalArgumentException | NullPointerException ex) {
            throw new IllegalArgumentException(this.toString(), ex);
        }
    }
}
