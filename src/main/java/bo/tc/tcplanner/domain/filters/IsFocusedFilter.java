package bo.tc.tcplanner.domain.filters;

import bo.tc.tcplanner.domain.planningstructures.Allocation;
import bo.tc.tcplanner.domain.planningstructures.Schedule;
import org.optaplanner.core.api.score.director.ScoreDirector;
import org.optaplanner.core.impl.heuristic.selector.common.decorator.SelectionFilter;

public class IsFocusedFilter implements SelectionFilter<Schedule, Allocation> {
    @Override
    public boolean accept(ScoreDirector<Schedule> scoreDirector, Allocation allocation) {
        return FilterTools.IsFocused(allocation);
    }
}
