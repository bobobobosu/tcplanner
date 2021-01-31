package bo.tc.tcplanner.domain.moves;

import bo.tc.tcplanner.domain.planningstructures.Allocation;
import bo.tc.tcplanner.domain.planningstructures.Schedule;
import org.optaplanner.core.impl.heuristic.selector.move.factory.MoveListFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class SplitTimelineEntryFactory implements MoveListFactory<Schedule> {
    @Override
    public List<SetValueMove> createMoveList(Schedule schedule) {
        List<SetValueMove> moveList = new ArrayList<>();
        schedule.focusedAllocationSet.values().forEach(thisAllocation -> {
            AtomicReference<Allocation> notFocusedAllocation = new AtomicReference<>();
            if (schedule.getJobIdx2jobcloneIdxMap().containsKey(thisAllocation.getIndex())) {
                schedule.getJobIdx2jobcloneIdxMap().get(thisAllocation.getIndex())
                        .stream().map(x -> schedule.getAllocationList().get(x))
                        .filter(x -> !x.isFocused()).findFirst()
                        .ifPresent(notFocusedAllocation::set);
            }
            if (notFocusedAllocation.get() != null) {
                Integer sum = thisAllocation.getProgressdelta();
                int max = sum / 2;
                moveList.add(new SetValueMove(
                        "SplitMove",
                        Arrays.asList(thisAllocation, notFocusedAllocation.get()),
                        Arrays.asList(
                                new AllocationValues().setProgressDelta(max),
                                new AllocationValues()
                                        .setPlanningStartDate(thisAllocation.getEndDate())
                                        .setProgressDelta(sum - max)
                        )
                ));
            }
        });
        return moveList;
    }
}