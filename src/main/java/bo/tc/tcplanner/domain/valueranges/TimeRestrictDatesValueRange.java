package bo.tc.tcplanner.domain.valueranges;

import bo.tc.tcplanner.domain.planningstructures.Allocation;
import bo.tc.tcplanner.domain.planningstructures.AllocationKey;
import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import org.optaplanner.core.api.domain.valuerange.CountableValueRange;

import java.time.ZonedDateTime;
import java.util.Iterator;
import java.util.Random;
import java.util.SortedMap;

public class TimeRestrictDatesValueRange implements CountableValueRange<ZonedDateTime> {
    RangeSet<ZonedDateTime> timeRestrictRanges;

    public TimeRestrictDatesValueRange(Allocation allocation) {
        timeRestrictRanges = allocation.getSchedule().getTimeEntryMap().get(allocation.getTimelineEntry().getHumanStateChange().getRequirementTimerange());
    }

    @Override
    public long getSize() {
        return timeRestrictRanges.asRanges().size(); //except source & sink;
    }

    @Override
    public ZonedDateTime get(long index) {
        Iterator<Range<ZonedDateTime>> timeIterator = timeRestrictRanges.asRanges().iterator();
        timeIterator.next(); //skip source
        while (--index > 0) timeIterator.next();
        return timeIterator.next().lowerEndpoint();
    }

    @Override
    public Iterator<ZonedDateTime> createOriginalIterator() {
        return new Iterator<>() {
            final Iterator<Range<ZonedDateTime>> timeIterator = timeRestrictRanges.asRanges().iterator();

            @Override
            public boolean hasNext() {
                return timeIterator.hasNext();
            }

            @Override
            public ZonedDateTime next() {
                return timeIterator.next().lowerEndpoint();
            }
        };
    }

    @Override
    public boolean isEmpty() {
        return timeRestrictRanges.asRanges().isEmpty();
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
                int nextInt = workingRandom.nextInt(timeRestrictRanges.asRanges().size());
                Iterator<Range<ZonedDateTime>> timeIterator = timeRestrictRanges.asRanges().iterator();
                for (int i = 0; i < nextInt - 1; ++i) timeIterator.next();
                return timeIterator.next().lowerEndpoint();
            }
        };
    }


}
