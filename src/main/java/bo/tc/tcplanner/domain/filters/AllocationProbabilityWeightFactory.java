package bo.tc.tcplanner.domain.filters;

import bo.tc.tcplanner.domain.planningstructures.Allocation;
import bo.tc.tcplanner.domain.planningstructures.Schedule;
import org.optaplanner.core.impl.heuristic.selector.common.decorator.SelectionProbabilityWeightFactory;
import org.optaplanner.core.impl.score.director.ScoreDirector;

public class AllocationProbabilityWeightFactory implements SelectionProbabilityWeightFactory<Schedule, Allocation> {

    @Override
    public double createProbabilityWeight(ScoreDirector<Schedule> scoreDirector, Allocation selection) {
        int focusedCnt = selection.getFocusedAllocationSet().size();
        int total = selection.getSchedule().getAllocationList().size();
        if (selection.isFocused()) {
            return 0.5 / total;
        } else {
            return 0.5 / (total - focusedCnt);
        }
//        if (selection.isFocused()) {
//            if (selection.getTimelineEntry().getTimelineProperty().getPlanningWindowType()
//                    .equals(PropertyConstants.PlanningWindowTypes.types.Draft.name())) return focused2dummyRatio;
//            return 1;
//        }
//        return focused2dummyRatio;
    }
}
