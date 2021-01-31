package bo.tc.tcplanner.domain.filters;

import bo.tc.tcplanner.domain.planningstructures.Allocation;
import bo.tc.tcplanner.domain.planningstructures.Schedule;
import org.optaplanner.core.impl.heuristic.selector.common.decorator.SelectionFilter;
import org.optaplanner.core.impl.score.director.ScoreDirector;

public class StartTimeCanChangeFilter implements SelectionFilter<Schedule, Allocation> {
    @Override
    public boolean accept(ScoreDirector<Schedule> scoreDirector, Allocation allocation) {
        return FilterTools.StartTimeCanChange(allocation);
    }
}
