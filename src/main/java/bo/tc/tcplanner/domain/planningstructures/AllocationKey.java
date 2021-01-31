package bo.tc.tcplanner.domain.planningstructures;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.UUID;

import static java.util.Comparator.comparing;

public class AllocationKey implements Comparable<AllocationKey>, Serializable {
    UUID id;
    ZonedDateTime zonedDateTime;
    int rownum;

    public AllocationKey() {

    }

    public AllocationKey(Allocation allocation) {
        this.id = allocation.getId();
        this.zonedDateTime = allocation.getConstrainedStartDate();
        this.rownum = allocation.getTimelineEntry().getTimelineProperty().getRownum();
    }

    @Override
    public int compareTo(@NotNull AllocationKey o) {
        return comparing(AllocationKey::getZonedDateTime, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(AllocationKey::getRownum)
                .thenComparing(AllocationKey::getId).compare(this, o);
    }

    public UUID getId() {
        return id;
    }

    public ZonedDateTime getZonedDateTime() {
        return zonedDateTime;
    }

    public int getRownum() {
        return rownum;
    }

    public AllocationKey setId(UUID id) {
        this.id = id;
        return this;
    }

    public AllocationKey setZonedDateTime(ZonedDateTime zonedDateTime) {
        this.zonedDateTime = zonedDateTime;
        return this;
    }

    public AllocationKey setRownum(int rownum) {
        this.rownum = rownum;
        return this;
    }
}
