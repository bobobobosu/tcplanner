package bo.tc.tcplanner.domain.filters;

import bo.tc.tcplanner.domain.planningstructures.Allocation;
import bo.tc.tcplanner.domain.planningstructures.Schedule;
import org.optaplanner.core.api.score.ScoreExplanation;
import org.optaplanner.core.api.score.ScoreManager;
import org.optaplanner.core.api.score.buildin.hardmediumsoftlong.HardMediumSoftLongScore;
import org.optaplanner.core.api.score.director.ScoreDirector;
import org.optaplanner.core.impl.heuristic.selector.common.decorator.SelectionFilter;

import static bo.tc.tcplanner.app.SolverCore.ScheduleSolver.getScoringScoreManager;

public class ReinitializeAllocationFilter implements SelectionFilter<Schedule, Allocation> {
    ScoreManager<Schedule, HardMediumSoftLongScore> scoreManager = getScoringScoreManager();

    @Override
    public boolean accept(ScoreDirector<Schedule> scoreDirector, Allocation selection) {
        return selection.isFocused() && changingmighesolve(scoreDirector, selection);
    }

    public boolean changingmighesolve(ScoreDirector<Schedule> scoreDirector, Allocation allocation) {
        try {
            ScoreExplanation<Schedule, HardMediumSoftLongScore> scoreExplanation = scoreManager.explainScore(scoreDirector.getWorkingSolution());
            HardMediumSoftLongScore thisAllocationScore = scoreExplanation.getIndictmentMap().get(allocation).getScore();
            if (thisAllocationScore.getHardScore() < 0) return true;

            HardMediumSoftLongScore nextAllocationScore = scoreExplanation.getIndictmentMap().get(allocation).getScore();
            return nextAllocationScore.getHardScore() < 0;
        } catch (NullPointerException ex) {
            return false;
        }
    }
}
