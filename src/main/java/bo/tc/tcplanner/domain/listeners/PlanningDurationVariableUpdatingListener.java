package bo.tc.tcplanner.domain.listeners;

import bo.tc.tcplanner.domain.planningstructures.Allocation;
import org.optaplanner.core.impl.domain.variable.listener.VariableListener;
import org.optaplanner.core.impl.score.director.ScoreDirector;

public class PlanningDurationVariableUpdatingListener implements VariableListener<Allocation> {

    @Override
    public void beforeEntityAdded(ScoreDirector scoreDirector, Allocation allocation) {
    }

    @Override
    public void afterEntityAdded(ScoreDirector scoreDirector, Allocation allocation) {
        ListenerTools.updatePlanningDuration(scoreDirector, allocation);
    }

    @Override
    public void beforeVariableChanged(ScoreDirector scoreDirector, Allocation allocation) {
    }

    @Override
    public void afterVariableChanged(ScoreDirector scoreDirector, Allocation allocation) {
        ListenerTools.updatePlanningDuration(scoreDirector, allocation);
    }

    @Override
    public void beforeEntityRemoved(ScoreDirector scoreDirector, Allocation allocation) {
    }

    @Override
    public void afterEntityRemoved(ScoreDirector scoreDirector, Allocation allocation) {
    }

}
