package bo.tc.tcplanner.app.SolverCore;

import bo.tc.tcplanner.PropertyConstants;
import bo.tc.tcplanner.domain.planningstructures.Schedule;
import org.optaplanner.core.api.score.ScoreManager;
import org.optaplanner.core.api.score.buildin.hardmediumsoftlong.HardMediumSoftLongScore;
import org.optaplanner.core.api.solver.Solver;
import org.optaplanner.core.api.solver.SolverFactory;
import org.optaplanner.core.config.solver.SolverConfig;
import org.optaplanner.core.config.solver.termination.TerminationConfig;

public class ScheduleSolver {
    // Config
    Solver<Schedule> solver_CH;
    Solver<Schedule> solver_FAST;
    Solver<Schedule> solver_ACCURATE;
    Solver<Schedule> solver_REDUCE;

    // Result
    public ScheduleLatest scheduleLatest;

    // State
    boolean continuetosolve = false;
    Solver<Schedule> currentSolver;


    public class ScheduleLatest {
        public Schedule schedule = null;
        public SolverScoreDelta solverScoreDelta = new SolverScoreDelta();
        public StringBuilder scoreexplaination = new StringBuilder();
    }

    public void solve(Schedule schedule) {
        // Initialize
        scheduleLatest.schedule = schedule;
        continuetosolve = true;
        scheduleLatest.solverScoreDelta.update(null);

        //Solve Hard
        do {
//            if (continuetosolve) {
//                scheduleLatest.schedule.solverPhase = PropertyConstants.SolverPhase.CH;
//                setCurrentSolver(solver_CH).solve(scheduleLatest.schedule);
//            }
            if (continuetosolve) {
                scheduleLatest.schedule.solverPhase = PropertyConstants.SolverPhase.FAST;
                setCurrentSolver(solver_FAST).solve(scheduleLatest.schedule);
            }
            if (continuetosolve) {
                scheduleLatest.schedule.solverPhase = PropertyConstants.SolverPhase.ACCURATE;
                setCurrentSolver(solver_ACCURATE).solve(scheduleLatest.schedule);
            }
        } while (continuetosolve && scheduleLatest.schedule.getScore().getHardScore() < 0);

        //Solve Soft
        if (continuetosolve) {
            scheduleLatest.schedule.solverPhase = PropertyConstants.SolverPhase.CH_REDUCE;
            setCurrentSolver(solver_CH).solve(scheduleLatest.schedule);
        }
        if (continuetosolve) {
            scheduleLatest.schedule.solverPhase = PropertyConstants.SolverPhase.REDUCE;
            setCurrentSolver(solver_REDUCE).solve(scheduleLatest.schedule);
        }

        synchronized (scheduleLatest.scoreexplaination) {
            scheduleLatest.scoreexplaination.setLength(0);
            scheduleLatest.scoreexplaination.append(getScoringScoreManager().explainScore(scheduleLatest.schedule));
            scheduleLatest.scoreexplaination.notifyAll();
        }
    }

    public Solver<Schedule> setCurrentSolver(Solver<Schedule> solver) {
        currentSolver = solver;
        return currentSolver;
    }

    public void terminate() {
        continuetosolve = false;
        if (solver_CH.isSolving()) solver_CH.terminateEarly();
        if (solver_FAST.isSolving()) solver_FAST.terminateEarly();
        if (solver_ACCURATE.isSolving()) solver_ACCURATE.terminateEarly();
        if (solver_REDUCE.isSolving()) solver_REDUCE.terminateEarly();
        while (solver_REDUCE.isSolving() || solver_FAST.isSolving() ||
                solver_ACCURATE.isSolving() || solver_REDUCE.isSolving()) {
        }
    }

    private void setSolverListener(Solver<Schedule> solver) {
        solver.addEventListener(bestSolutionChangedEvent -> {
            scheduleLatest.schedule = bestSolutionChangedEvent.getNewBestSolution();
            scheduleLatest.solverScoreDelta.update(getScoringScoreManager().updateScore(scheduleLatest.schedule));
            synchronized (scheduleLatest.solverScoreDelta) {
                scheduleLatest.solverScoreDelta.notify();
            }
        });
    }

    public ScheduleSolver() {
        SolverFactory<Schedule> solverFactory;
        SolverConfig solverConfig0 = SolverConfig.createFromXmlResource("solverPhase0.xml");
        SolverConfig solverConfig1 = SolverConfig.createFromXmlResource("solverPhase1.xml");
        SolverConfig solverConfig2 = SolverConfig.createFromXmlResource("solverPhase2.xml");

        // Solve Phase 0 : construction heuristic
        /*
        From template, remove local search phase & termination config
         */
        solverConfig0.withTerminationConfig(
                new TerminationConfig());
        solverFactory = SolverFactory.create(solverConfig0);
        solver_CH = solverFactory.buildSolver();
        setSolverListener(solver_CH);

        // Solve Phase 1 : fast
        /*
        From template, remove construction heuristic phase & termination config and set accepted count limit 350
         */
        solverConfig1.withTerminationConfig(
                new TerminationConfig()
                        .withUnimprovedSecondsSpentLimit(3L)
                        .withBestScoreFeasible(true));
        solverFactory = SolverFactory.create(solverConfig1);
        solver_FAST = solverFactory.buildSolver();
        setSolverListener(solver_FAST);

        // Solve Phase 2 : accurate
        /*
        From template, remove construction heuristic phase & termination config & pick early type
         */
        solverConfig2.withTerminationConfig(
                new TerminationConfig()
                        .withUnimprovedSecondsSpentLimit(90L)
                        .withBestScoreFeasible(true));
        solverFactory = SolverFactory.create(solverConfig2);
        solver_ACCURATE = solverFactory.buildSolver();
        setSolverListener(solver_ACCURATE);

        // Solve Phase 3 : optimize
        /*
        Same as Phase 2
         */
        solverConfig2.withTerminationConfig(
                new TerminationConfig()
                        .withUnimprovedMinutesSpentLimit(5L));
        solverFactory = SolverFactory.create(solverConfig2);
        solver_REDUCE = solverFactory.buildSolver();
        setSolverListener(solver_REDUCE);

        scheduleLatest = new ScheduleLatest();
    }

    public static ScoreManager<Schedule, HardMediumSoftLongScore> getScoringScoreManager() {
        SolverFactory<Schedule> solverFactory = SolverFactory.createFromXmlResource("solverPhase1.xml");
        return ScoreManager.create(solverFactory);
    }
}
