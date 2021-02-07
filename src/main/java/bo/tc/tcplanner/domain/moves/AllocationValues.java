package bo.tc.tcplanner.domain.moves;

import bo.tc.tcplanner.domain.planningstructures.Allocation;
import org.optaplanner.core.api.score.director.ScoreDirector;

import java.time.ZonedDateTime;
import java.util.Objects;

public class AllocationValues {
    private Integer progressDelta = null;
    private ZonedDateTime planningStartDate = null;
    boolean progressDeltaApply = false;
    boolean planningStartDateApply = false;

    public AllocationValues() {

    }

    public AllocationValues(AllocationValues allocationValues) {
        this.progressDelta = allocationValues.progressDelta;
        this.planningStartDate = allocationValues.planningStartDate;
        this.progressDeltaApply = allocationValues.progressDeltaApply;
        this.planningStartDateApply = allocationValues.planningStartDateApply;
    }

    public Integer getProgressDelta() {
        return progressDelta;
    }

    public AllocationValues setProgressDelta(Integer progressDelta) {
        this.progressDelta = progressDelta;
        this.progressDeltaApply = true;
        return this;
    }

    public ZonedDateTime getPlanningStartDate() {
        return planningStartDate;
    }

    public AllocationValues setPlanningStartDate(ZonedDateTime planningStartDate) {
        this.planningStartDate = planningStartDate;
        this.planningStartDateApply = true;
        return this;
    }

    public AllocationValues extract(Allocation allocation) {
        return this
                .setProgressDelta(allocation.getProgressdelta())
                .setPlanningStartDate(allocation.getPlanningStartDate());
    }

    public void apply(Allocation allocation, ScoreDirector scoreDirector) {
        if (planningStartDateApply) {
            scoreDirector.beforeVariableChanged(allocation, "planningStartDate");
            allocation.setPlanningStartDate(planningStartDate);
            scoreDirector.afterVariableChanged(allocation, "planningStartDate");
        }
        if (progressDeltaApply) {
            scoreDirector.beforeVariableChanged(allocation, "progressdelta");
            allocation.setProgressdelta(progressDelta);
            scoreDirector.afterVariableChanged(allocation, "progressdelta");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AllocationValues that = (AllocationValues) o;
        return progressDeltaApply == that.progressDeltaApply &&
                planningStartDateApply == that.planningStartDateApply &&
                Objects.equals(progressDelta, that.progressDelta) &&
                Objects.equals(planningStartDate, that.planningStartDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(progressDelta, planningStartDate, progressDeltaApply, planningStartDateApply);
    }
}
