package bo.tc.tcplanner.domain.filters;

import bo.tc.tcplanner.domain.planningstructures.Allocation;
import bo.tc.tcplanner.domain.planningstructures.Schedule;
import org.optaplanner.core.api.score.buildin.hardmediumsoftlong.HardMediumSoftLongScore;
import org.optaplanner.core.impl.heuristic.selector.common.decorator.SelectionFilter;
import org.optaplanner.core.impl.score.director.ScoreDirector;

public class ReinitializeAllocationFilter implements SelectionFilter<Schedule, Allocation> {

    @Override
    public boolean accept(ScoreDirector<Schedule> scoreDirector, Allocation selection) {
        return selection.isFocused() && changingmighesolve(scoreDirector, selection);
    }

    public boolean changingmighesolve(ScoreDirector<Schedule> scoreDirector, Allocation allocation) {
        try {
            HardMediumSoftLongScore thisAllocationScore = (HardMediumSoftLongScore)
                    scoreDirector.getIndictmentMap().get(allocation).getScore();
            if (thisAllocationScore.getHardScore() < 0) return true;

            HardMediumSoftLongScore nextAllocationScore = (HardMediumSoftLongScore)
                    scoreDirector.getIndictmentMap().get(allocation).getScore();
            return nextAllocationScore.getHardScore() < 0;
        } catch (NullPointerException ex) {
            return false;
        }
    }
}
