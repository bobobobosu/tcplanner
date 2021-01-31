package bo.tc.tcplanner.domain.moves;

import bo.tc.tcplanner.domain.planningstructures.Allocation;
import bo.tc.tcplanner.domain.planningstructures.AllocationKey;
import bo.tc.tcplanner.domain.planningstructures.Schedule;
import org.optaplanner.core.impl.heuristic.move.Move;
import org.optaplanner.core.impl.heuristic.selector.move.factory.MoveIteratorFactory;
import org.optaplanner.core.impl.score.director.ScoreDirector;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

public class RemoveMoveIteratorFactory implements MoveIteratorFactory<Schedule> {
    Map<AllocationKey, Allocation> focusedAllocationMap;
    Iterator<Allocation> focusedAllocationIterator;

    @Override
    public long getSize(ScoreDirector<Schedule> scoreDirector) {
        return focusedAllocationMap.size();
    }

    @Override
    public Iterator<? extends Move<Schedule>> createOriginalMoveIterator(ScoreDirector<Schedule> scoreDirector) {
        focusedAllocationMap = scoreDirector.getWorkingSolution().getFocusedAllocationSet();
        focusedAllocationIterator = focusedAllocationMap.values().iterator();
        return new Iterator<>() {
            @Override
            public boolean hasNext() {
                return focusedAllocationIterator.hasNext();
            }

            @Override
            public Move<Schedule> next() {
                Allocation allocation = focusedAllocationIterator.next();
                return new SetValueMove(
                        "RemoveMove",
                        Collections.singletonList(allocation),
                        Collections.singletonList(new AllocationValues().setPlanningStartDate(null))
                );
            }
        };
    }

    @Override
    public Iterator<? extends Move<Schedule>> createRandomMoveIterator(ScoreDirector<Schedule> scoreDirector, Random workingRandom) {
        focusedAllocationMap = scoreDirector.getWorkingSolution().getFocusedAllocationSet();

        return new Iterator<>() {
            @Override
            public boolean hasNext() {
                return true;
            }

            @Override
            public Move<Schedule> next() {
                focusedAllocationIterator = focusedAllocationMap.values().iterator();
                int nextInt = workingRandom.nextInt(focusedAllocationMap.size());
                for (int i = 0; i < nextInt - 1; ++i) focusedAllocationIterator.next();
                return new SetValueMove(
                        "RemoveMove",
                        Collections.singletonList(focusedAllocationIterator.next()),
                        Collections.singletonList(new AllocationValues().setPlanningStartDate(null))
                );
            }
        };
    }
}
