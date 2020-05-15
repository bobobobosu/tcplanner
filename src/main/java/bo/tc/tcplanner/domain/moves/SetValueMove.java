package bo.tc.tcplanner.domain.moves;

import bo.tc.tcplanner.domain.filters.FilterTools;
import bo.tc.tcplanner.domain.planningstructures.Allocation;
import bo.tc.tcplanner.domain.planningstructures.Schedule;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.optaplanner.core.impl.heuristic.move.AbstractMove;
import org.optaplanner.core.impl.score.director.ScoreDirector;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;


public class SetValueMove extends AbstractMove<Schedule> {
    private List<Allocation> allocationList;
    private List<AllocationValues> allocationValuesList;
    private List<AllocationValues> oldallocationValuesList;
    private String moveName = "SetValueMove";

    public SetValueMove(String moveName, List<Allocation> allocationList, List<AllocationValues> allocationValuesList) {
        this.moveName = moveName;
        this.allocationList = allocationList;
        this.allocationValuesList = allocationValuesList;
        this.oldallocationValuesList = allocationList.stream()
                .map(x -> new AllocationValues()
                        .setProgressDelta(x.getProgressdelta())
                        .setPlanningStartDate(x.getPlanningStartDate())).collect(Collectors.toList());
    }

    @Override
    public AbstractMove<Schedule> createUndoMove(ScoreDirector<Schedule> scoreDirector) {
        SetValueMove undoMove = new SetValueMove(moveName, allocationList, oldallocationValuesList);
        // Fix the "old" values in undo move since at this point the move hasn't been done yet
        undoMove.oldallocationValuesList = allocationValuesList;
        return undoMove;
    }

    @Override
    protected void doMoveOnGenuineVariables(ScoreDirector<Schedule> scoreDirector) {
        for (int i = 0; i < allocationList.size(); i++) {
            Allocation allocation = allocationList.get(i);
            AllocationValues allocationValues = allocationValuesList.get(i);
            allocationValues.apply(allocation, scoreDirector);
        }

    }

    @Override
    public boolean isMoveDoable(ScoreDirector<Schedule> scoreDirector) {
        for (int i = 0; i < allocationList.size(); i++) {
            if (!FilterTools.ProgressDeltaCanChange(allocationList.get(i)) &&
                    allocationValuesList.get(i).progressDeltaApply) return false;
            if (!FilterTools.StartTimeCanChange(allocationList.get(i)) &&
                    allocationValuesList.get(i).planningStartDateApply) return false;
        }
        return true;
    }

    @Override
    public SetValueMove rebase(ScoreDirector<Schedule> destinationScoreDirector) {
        return new SetValueMove(
                moveName,
                allocationList.stream().map(destinationScoreDirector::lookUpWorkingObject).collect(Collectors.toList()),
                allocationValuesList.stream().map(x -> {
                    AllocationValues newAllocationValue = new AllocationValues()
                            .setProgressDelta(destinationScoreDirector.lookUpWorkingObject(x.getProgressDelta()))
                            .setPlanningStartDate(destinationScoreDirector.lookUpWorkingObject(x.getPlanningStartDate()));
                    newAllocationValue.planningStartDateApply = destinationScoreDirector.lookUpWorkingObject(x.planningStartDateApply);
                    newAllocationValue.progressDeltaApply = destinationScoreDirector.lookUpWorkingObject(x.progressDeltaApply);
                    return newAllocationValue;
                }).collect(Collectors.toList()));
    }


    public String toString() {
        StringBuilder s = new StringBuilder();
        for (int i = 0; i < allocationList.size(); i++) {
            Allocation allocation = allocationList.get(i);
            AllocationValues allocationValues = allocationValuesList.get(i);
            s.append(allocation).append(" {");
            if (allocationValues.getProgressDelta() != null)
                s.append(allocation.getProgressdelta()).append(" -> ").append(allocationValues.getProgressDelta()).append(", ");
            if (allocationValues.getPlanningStartDate() != null)
                s.append(allocation.getPlanningStartDate()).append(" -> ").append(allocationValues.getPlanningStartDate()).append(", ");
            s.append("} ");
        }
        s.deleteCharAt(s.length() - 1);
        return s.insert(0, " ").insert(0, moveName).toString();
    }

    @Override
    public String getSimpleMoveTypeDescription() {
        return moveName;
    }

    @Override
    public Collection<? extends Object> getPlanningEntities() {
        return allocationList;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o instanceof SetValueMove) {
            SetValueMove other = (SetValueMove) o;
            return new EqualsBuilder()
                    .append(allocationList, other.allocationList)
                    .append(allocationValuesList, other.allocationValuesList)
                    .isEquals();
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(allocationList)
                .append(allocationValuesList)
                .toHashCode();
    }

}
