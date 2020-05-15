package bo.tc.tcplanner.domain.listeners;

import bo.tc.tcplanner.domain.planningstructures.Allocation;
import org.optaplanner.core.impl.domain.variable.listener.VariableListener;
import org.optaplanner.core.impl.score.director.ScoreDirector;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static bo.tc.tcplanner.domain.listeners.ListenerTools.applyResourceTotalMap;

public class ResourceStateChangeVariableListener implements VariableListener<Allocation> {
    // Before State
    Map<Allocation, ProgressSnapshot> snapshotMap = new HashMap<>();

    static class ProgressSnapshot {
        Integer progressdeltaBefore;
        Allocation successorAllocationBefore;
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
        ProgressSnapshot progressSnapshot = new ProgressSnapshot();
        progressSnapshot.progressdeltaBefore = allocation.getProgressdelta();
        if (allocation.isFocused()) {
            Optional.ofNullable(allocation.getFocusedAllocationSet().higherEntry(allocation.getAllocationKey())).ifPresent(
                    x -> progressSnapshot.successorAllocationBefore = x.getValue());
        }
        snapshotMap.put(allocation, progressSnapshot);
    }

    @Override
    public void afterVariableChanged(ScoreDirector scoreDirector, Allocation allocation) {
        // retrieve snapshot
        ProgressSnapshot progressSnapshot = Optional.ofNullable(snapshotMap.get(allocation))
                .orElse(new ProgressSnapshot());

        // Update new values
        progressSnapshot.changedAllocation = allocation.isFocused() ? allocation : null;
        if (progressSnapshot.changedAllocation != null) {
            Optional.ofNullable(allocation.getFocusedAllocationSet().higherEntry(progressSnapshot.changedAllocation.getAllocationKey())).ifPresent(
                    y -> progressSnapshot.successorAllocationAfter = y.getValue());
        }

        if (!allocation.getProgressdelta().equals(progressSnapshot.progressdeltaBefore)) {
            // Update ResourceElementMap
            if (progressSnapshot.successorAllocationAfter != null) {
                applyResourceTotalMap(scoreDirector, allocation,
                        progressSnapshot.successorAllocationAfter);
            }
        }


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
