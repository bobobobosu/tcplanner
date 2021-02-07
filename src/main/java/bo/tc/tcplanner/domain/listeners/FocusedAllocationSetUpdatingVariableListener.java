package bo.tc.tcplanner.domain.listeners;

import bo.tc.tcplanner.domain.planningstructures.Allocation;
import bo.tc.tcplanner.domain.planningstructures.AllocationKey;
import bo.tc.tcplanner.domain.planningstructures.Schedule;
import org.optaplanner.core.api.domain.variable.VariableListener;
import org.optaplanner.core.api.score.director.ScoreDirector;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static bo.tc.tcplanner.domain.listeners.ListenerTools.applyResourceTotalMap;
import static bo.tc.tcplanner.domain.listeners.ListenerTools.updateAllocationPreviousStandstill;

public class FocusedAllocationSetUpdatingVariableListener implements VariableListener<Schedule, Allocation> {
    // Before State
    Map<Allocation, StateSnapshot> snapshotMap = new HashMap<>();

    static class StateSnapshot {
        AllocationKey changedAllocationKey;
        Allocation predecessorAllocationBefore;
        Allocation successorAllocationBefore;
        Allocation predecessorAllocationAfter;
        Allocation successorAllocationAfter;
        Allocation changedAllocation;
    }

    @Override
    public void beforeEntityAdded(ScoreDirector scoreDirector, Allocation allocation) {

    }

    @Override
    public void afterEntityAdded(ScoreDirector scoreDirector, Allocation allocation) {
    }

    @Override
    public void beforeVariableChanged(ScoreDirector scoreDirector, Allocation allocation) {
        StateSnapshot stateSnapshot = new StateSnapshot();
        stateSnapshot.changedAllocationKey = allocation.getAllocationKey();
        if (allocation.isFocused()) {
            Optional.ofNullable(allocation.getFocusedAllocationSet().higherEntry(allocation.getAllocationKey())).ifPresent(
                    x -> stateSnapshot.successorAllocationBefore = x.getValue());
            Optional.ofNullable(allocation.getFocusedAllocationSet().lowerEntry(allocation.getAllocationKey())).ifPresent(
                    x -> stateSnapshot.predecessorAllocationBefore = x.getValue());
        }
        snapshotMap.put(allocation, stateSnapshot);
    }

    @Override
    public void afterVariableChanged(ScoreDirector scoreDirector, Allocation allocation) {
        Allocation prevAllocation, thisAllocation;
        // retrieve snapshot
        StateSnapshot stateSnapshot = Optional.ofNullable(snapshotMap.get(allocation)).orElse(new StateSnapshot());

        // Update focusedAllocationSet
        if (stateSnapshot.changedAllocationKey != null) {
            scoreDirector.beforeVariableChanged(allocation, "focusedAllocationSet");
            allocation.getFocusedAllocationSet().remove(stateSnapshot.changedAllocationKey);
            if (allocation.isFocused())
                allocation.getFocusedAllocationSet().put(allocation.getAllocationKey(), allocation);
            scoreDirector.afterVariableChanged(allocation, "focusedAllocationSet");
        }

        // Update new values
        stateSnapshot.changedAllocation = allocation.isFocused() ? allocation : null;
        if (stateSnapshot.changedAllocation != null) {
            Optional.ofNullable(allocation.getFocusedAllocationSet().higherEntry(stateSnapshot.changedAllocation.getAllocationKey())).ifPresent(
                    y -> stateSnapshot.successorAllocationAfter = y.getValue());
            Optional.ofNullable(allocation.getFocusedAllocationSet().lowerEntry(stateSnapshot.changedAllocation.getAllocationKey())).ifPresent(
                    y -> stateSnapshot.predecessorAllocationAfter = y.getValue());
        }

        // Update nextStartDate
        if ((thisAllocation = stateSnapshot.predecessorAllocationBefore) != null) {
            ListenerTools.updateNextStartDate(scoreDirector, thisAllocation, thisAllocation.getNextAllocation());
        }
        if ((thisAllocation = stateSnapshot.predecessorAllocationAfter) != null) {
            ListenerTools.updateNextStartDate(scoreDirector, thisAllocation, thisAllocation.getNextAllocation());
        }
        if ((thisAllocation = stateSnapshot.successorAllocationBefore) != null) {
            ListenerTools.updateNextStartDate(scoreDirector, thisAllocation, thisAllocation.getNextAllocation());
        }
        if ((thisAllocation = stateSnapshot.successorAllocationAfter) != null) {
            ListenerTools.updateNextStartDate(scoreDirector, thisAllocation, thisAllocation.getNextAllocation());
        }
        if ((thisAllocation = stateSnapshot.changedAllocation) != null) {
            ListenerTools.updateNextStartDate(scoreDirector, thisAllocation, thisAllocation.getNextAllocation());
        }

        // Update previousStandstill
        updateAllocationPreviousStandstill(scoreDirector, allocation.getPrevAllocation(), allocation);
        boolean changing;
        changing = true;
        if ((thisAllocation = stateSnapshot.successorAllocationBefore) != null) {
            prevAllocation = thisAllocation.getPrevAllocation();
            while (changing && thisAllocation != null) {
                changing = updateAllocationPreviousStandstill(scoreDirector, prevAllocation, thisAllocation);
                thisAllocation = (prevAllocation = thisAllocation).getNextAllocation();
            }
        }
        changing = true;
        if ((thisAllocation = stateSnapshot.successorAllocationAfter) != null) {
            prevAllocation = thisAllocation.getPrevAllocation();
            while (changing && thisAllocation != null) {
                changing = updateAllocationPreviousStandstill(scoreDirector, prevAllocation, thisAllocation);
                thisAllocation = (prevAllocation = thisAllocation).getNextAllocation();
            }
        }

        // Update ResourceElementMap
        Allocation startAllocation = stateSnapshot.successorAllocationBefore;
        if (startAllocation == null || (stateSnapshot.successorAllocationAfter != null &&
                stateSnapshot.successorAllocationAfter.getAllocationKey().compareTo(startAllocation.getAllocationKey()) < 0))
            startAllocation = stateSnapshot.successorAllocationAfter;
        applyResourceTotalMap(scoreDirector, allocation, startAllocation);


        // reset
        snapshotMap.remove(allocation);
    }

    @Override
    public void beforeEntityRemoved(ScoreDirector scoreDirector, Allocation allocation) {
    }

    @Override
    public void afterEntityRemoved(ScoreDirector scoreDirector, Allocation allocation) {
    }
}
