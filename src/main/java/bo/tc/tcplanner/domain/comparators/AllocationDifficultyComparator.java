package bo.tc.tcplanner.domain.comparators;

import bo.tc.tcplanner.domain.planningstructures.Allocation;

import java.util.Comparator;

public class AllocationDifficultyComparator implements Comparator<Allocation> {
    @Override
    public int compare(Allocation o1, Allocation o2) {
        return o2.getWeight().compareTo(o1.getWeight());
    }
}
