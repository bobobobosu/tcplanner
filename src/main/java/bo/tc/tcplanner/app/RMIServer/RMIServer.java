package bo.tc.tcplanner.app.RMIServer;

import bo.tc.tcplanner.PropertyConstants;
import bo.tc.tcplanner.app.SolverCore.GuiSolverThread;
import bo.tc.tcplanner.app.SolverCore.SolverProgress;
import bo.tc.tcplanner.app.SolverCore.SolverScoreDelta;
import bo.tc.tcplanner.app.TCApp;
import bo.tc.tcplanner.domain.planningstructures.Schedule;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import java.util.stream.Collectors;

public class RMIServer extends UnicastRemoteObject implements RMIInterface {
    GuiSolverThread solverThread;
    TCApp application;

    public RMIServer(GuiSolverThread solverThread, TCApp application) throws RemoteException {
        super();
        this.solverThread = solverThread;
        this.application = application;
    }

    @Override
    public void startSolver(Schedule schedule) throws RemoteException {
        solverThread.terminate();
        solverThread.setCurrentSchedule(schedule);
        solverThread.solve(GuiSolverThread.SolverModes.RESUME);
    }

    @Override
    public void stopSolver() throws RemoteException {
        solverThread.terminate();
    }

    @Override
    public void resetSolver() throws RemoteException {
        solverThread.solve(GuiSolverThread.SolverModes.RESET);
    }

    @Override
    public Schedule getCurrentSchedule() throws RemoteException {
        return solverThread.getCurrentSchedule();
    }

    @Override
    public String getConsoleBuffer() throws RemoteException {
        synchronized (application.getAppStatusLock().get("console")) {
            try {
                application.getAppStatusLock().get("console").wait();
            } catch (InterruptedException ignored) {
            }
            return application.flushConsole();
        }
    }

    @Override
    public String getScoreExplaination() throws RemoteException {
        synchronized (application.getAppStatusLock().get("scoreexplaination")) {
            try {
                application.getAppStatusLock().get("scoreexplaination").wait();
            } catch (InterruptedException ignored) {
            }
            return ((StringBuilder) application.getAppStatusLock().get("scoreexplaination")).toString();
        }
    }

    @Override
    public SolverProgress getSolvingProgress() throws RemoteException {
        synchronized (application.getAppStatusLock().get("currentScoreDelta")) {
            try {
                application.getAppStatusLock().get("currentScoreDelta").wait();
            } catch (InterruptedException ignored) {
            }
            List<String> newTimelineEntries = solverThread.getCurrentSchedule().focusedAllocationSet.values().stream()
                    .filter(x -> x.getTimelineEntry().getTimelineProperty().getPlanningWindowType()
                            .equals(PropertyConstants.PlanningWindowTypes.types.Draft.name()))
                    .map(x -> x.getTimelineEntry().getTitle()).distinct().collect(Collectors.toList());

            SolverProgress solverProgress = new SolverProgress();
            solverProgress.setScheduleId(solverThread.getCurrentSchedule().getId());
            solverProgress.setSolverScoreDelta((SolverScoreDelta) application.getAppStatusLock().get("currentScoreDelta"));
            solverProgress.setUnsolvedEntities(Schedule.unsolvedEntityCount(solverThread.getCurrentSchedule()));
            solverProgress.setTotalEntities(solverThread.getCurrentSchedule().focusedAllocationSet.size());
            solverProgress.setSolvePhaseName(solverThread.getCurrentSchedule().solverPhase.name());
            solverProgress.setAddedTimelineEntries(newTimelineEntries);
            return solverProgress;
        }
    }

    @Override
    public boolean ping() throws RemoteException {
        return true;
    }
}
