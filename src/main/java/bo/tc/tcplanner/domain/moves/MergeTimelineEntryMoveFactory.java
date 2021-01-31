package bo.tc.tcplanner.domain.moves;

import bo.tc.tcplanner.domain.planningstructures.Allocation;
import bo.tc.tcplanner.domain.planningstructures.Schedule;
import org.optaplanner.core.impl.heuristic.selector.move.factory.MoveListFactory;

import java.util.*;
import java.util.stream.Collectors;

public class MergeTimelineEntryMoveFactory implements MoveListFactory<Schedule> {
    @Override
    public List<SetValueMove> createMoveList(Schedule schedule) {
        List<SetValueMove> moveList = new ArrayList<>();
        schedule.focusedAllocationSet.values().forEach(toAllocation -> {
            Set<Allocation> allocationSet = schedule.getTimelineEntry2AllocationIdxMap()
                    .get(toAllocation.getTimelineEntry()).stream().map(x -> schedule.getAllocationList().get(x))
                    .collect(Collectors.toSet());
            for (Allocation thisAllocation : allocationSet) {
                if (!thisAllocation.isFocused() || thisAllocation == toAllocation) continue;
                int sum = thisAllocation.getProgressdelta() + toAllocation.getProgressdelta();
                int mergedToProgressDelta = Math.min(sum, 100);
                int rest = sum - mergedToProgressDelta;
                moveList.add(new SetValueMove(
                        "MergeMove",
                        Arrays.asList(toAllocation, thisAllocation),
                        Arrays.asList(
                                new AllocationValues()
                                        .setProgressDelta(mergedToProgressDelta),
                                new AllocationValues()
                                        .setProgressDelta(rest)
                                        .setPlanningStartDate(rest == 0 ?
                                                null : thisAllocation.getPlanningStartDate())
                        )
                ));
            }
        });
//        Collections.reverse(moveList);
        return moveList;
    }
}

