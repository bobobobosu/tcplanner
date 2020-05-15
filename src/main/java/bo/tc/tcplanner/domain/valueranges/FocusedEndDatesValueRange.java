package bo.tc.tcplanner.domain.valueranges;

import bo.tc.tcplanner.domain.planningstructures.Allocation;
import bo.tc.tcplanner.domain.planningstructures.AllocationKey;
import org.optaplanner.core.api.domain.valuerange.CountableValueRange;

import java.time.ZonedDateTime;
import java.util.Iterator;
import java.util.Random;
import java.util.SortedMap;

public class FocusedEndDatesValueRange implements CountableValueRange<ZonedDateTime> {
    SortedMap<AllocationKey, Allocation> availableAllocationSet;

    public FocusedEndDatesValueRange(Allocation allocation) {
        this.availableAllocationSet = allocation.getFocusedAllocationSet().tailMap(
                allocation.getFocusedAllocationSet().lowerKey(new AllocationKey().setZonedDateTime(
                        allocation.getSchedule().getProblemTimelineBlock().getZonedBlockScheduleAfter()
                ))).headMap(allocation.getSchedule().getSinkAllocation().getPrevAllocation().getAllocationKey());
    }

    @Override
    public long getSize() {
        return availableAllocationSet.size() - 2; //except source & sink;
    }

    @Override
    public ZonedDateTime get(long index) {
        Iterator<Allocation> allocationIterator = availableAllocationSet.values().iterator();
        allocationIterator.next(); //skip source
        while (--index > 0) allocationIterator.next();
        return allocationIterator.next().getEndDate();
    }

    @Override
    public Iterator<ZonedDateTime> createOriginalIterator() {
        return new Iterator<>() {
            Iterator<Allocation> focusedAllocationIterator = availableAllocationSet.values().iterator();

            @Override
            public boolean hasNext() {
                return focusedAllocationIterator.hasNext();
            }

            @Override
            public ZonedDateTime next() {
                return focusedAllocationIterator.next().getEndDate();
            }
        };
    }

    @Override
    public boolean isEmpty() {
        return availableAllocationSet.isEmpty();
    }

    @Override
    public boolean contains(ZonedDateTime value) {
        return false;
    }

    @Override
    public Iterator<ZonedDateTime> createRandomIterator(Random workingRandom) {
        return new Iterator<>() {
            @Override
            public boolean hasNext() {
                return true;
            }

            @Override
            public ZonedDateTime next() {
                int nextInt = workingRandom.nextInt(availableAllocationSet.size());
                Iterator<Allocation> focusedAllocationIterator = availableAllocationSet.values().iterator();
                for (int i = 0; i < nextInt - 1; ++i) focusedAllocationIterator.next();
                return focusedAllocationIterator.next().getEndDate();
            }
        };
    }


}
