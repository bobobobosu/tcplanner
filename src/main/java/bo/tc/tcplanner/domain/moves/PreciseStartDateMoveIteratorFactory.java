package bo.tc.tcplanner.domain.moves;

import bo.tc.tcplanner.domain.planningstructures.Allocation;
import bo.tc.tcplanner.domain.planningstructures.Schedule;
import org.optaplanner.core.impl.heuristic.move.Move;
import org.optaplanner.core.impl.heuristic.selector.move.factory.MoveIteratorFactory;
import org.optaplanner.core.impl.score.director.ScoreDirector;

import java.util.*;

public class PreciseStartDateMoveIteratorFactory implements MoveIteratorFactory<Schedule> {
    @Override
    public long getSize(ScoreDirector<Schedule> scoreDirector) {
        Schedule thisSchedule = scoreDirector.getWorkingSolution();
        return thisSchedule.focusedAllocationSet.size() * thisSchedule.getAllocationList().size();
    }

    @Override
    public Iterator<? extends Move<Schedule>> createOriginalMoveIterator(ScoreDirector<Schedule> scoreDirector) {
        return new Iterator<Move<Schedule>>() {
            Iterator<Allocation> allocationIterator = scoreDirector.getWorkingSolution().getAllocationList().iterator();
            Allocation thisAllocation = allocationIterator.next();
            Iterator<Allocation> endDateIterator = scoreDirector.getWorkingSolution().focusedAllocationSet.values().iterator();

            @Override
            public boolean hasNext() {
                return thisAllocation != null && endDateIterator.hasNext();
            }

            @Override
            public Move<Schedule> next() {
                Allocation allocation = thisAllocation;
                if (!endDateIterator.hasNext()) {
                    thisAllocation = allocationIterator.hasNext() ? allocationIterator.next() : null;
                    if (thisAllocation != null)
                        endDateIterator = scoreDirector.getWorkingSolution().focusedAllocationSet.values().iterator();
                }

                return new SetValueMove(
                        " PreciseStartDateMove",
                        Arrays.asList(allocation),
                        Arrays.asList(new AllocationValues().setPlanningStartDate(endDateIterator.next().getEndDate())));
            }
        };
    }

    @Override
    public Iterator<? extends Move<Schedule>> createRandomMoveIterator(ScoreDirector<Schedule> scoreDirector, Random random) {
        return new Iterator<Move<Schedule>>() {
            ListIterator<Allocation> allocationIterator = scoreDirector.getWorkingSolution().getAllocationList()
                    .listIterator(scoreDirector.getWorkingSolution().getAllocationList().size());
            Allocation thisAllocation = allocationIterator.previous();
            Iterator<Allocation> endDateIterator = scoreDirector.getWorkingSolution().focusedAllocationSet.values().iterator();

            @Override
            public boolean hasNext() {
                return thisAllocation != null && endDateIterator.hasNext();
            }

            @Override
            public Move<Schedule> next() {
                Allocation allocation = thisAllocation;
                if (!endDateIterator.hasNext()) {
                    thisAllocation = allocationIterator.hasPrevious() ? allocationIterator.previous() : null;
                    if (thisAllocation != null)
                        endDateIterator = scoreDirector.getWorkingSolution().focusedAllocationSet.values().iterator();
                }

                return new SetValueMove(
                        "PreciseStartDateMove",
                        Collections.singletonList(allocation),
                        Collections.singletonList(new AllocationValues().setPlanningStartDate(endDateIterator.next().getEndDate())));
            }
        };
    }
}
