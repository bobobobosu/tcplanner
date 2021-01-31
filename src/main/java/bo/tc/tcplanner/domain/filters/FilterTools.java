package bo.tc.tcplanner.domain.filters;

import bo.tc.tcplanner.domain.planningstructures.Allocation;

public class FilterTools {
    public static boolean StartTimeCanChange(Allocation allocation) {
        if (allocation.isPinned()) return false;
        return !allocation.isHistory();
        // Draggable TimelineEntry is implemented using getStartTime() built-in hard constraint and
        // checkTimeOverlapping score so here we allow it to change
    }

    public static boolean ProgressDeltaCanChange(Allocation allocation) {
        if (allocation.isPinned()) return false;
        return !allocation.isHistory();
        // Splittable is implemented using getProgressDeltaRange built-in hard constraint and checkSplittable score
        // so here we allow it to change
    }

    public static boolean IsFocused(Allocation allocation) {
        return allocation.isFocused();
    }
}
