package bo.tc.tcplanner.app.SolverCore;

import bo.tc.tcplanner.app.DataServer.FileServer;
import bo.tc.tcplanner.app.TCApp;
import bo.tc.tcplanner.datastructure.converters.DataStructureBuilder;
import bo.tc.tcplanner.domain.planningstructures.Schedule;
import com.google.common.base.Throwables;

import static bo.tc.tcplanner.app.SolverCore.Toolbox.displayTray;

public class GuiSolverThread extends Thread {

    // Objects
    FileServer fileServer;
    TCApp application;

    // locks
    private final SolverLock solverLock = new SolverLock();

    // Current
    ScheduleSolver scheduleSolver = new ScheduleSolver();


    public static class SolverLock {
        public SolverModes mode = SolverModes.RESTART;
    }

    public enum SolverModes {
        RESTART, // populate current schedule problemTimelineBlock and start solve
        RESUME, // start solve with currentSchedule
        RESET // populate current schedule problemTimelineBlock
    }


    @Override
    public void run() {
        while (true) {
            //Wait
            synchronized (solverLock) {
                try {
                    solverLock.wait();
                    displayTray("Solving Started", "Good Luck");

                    // free up memory
                    System.gc();
                    // Override notification locks
                    scheduleSolver.scheduleLatest.solverScoreDelta =
                            ((SolverScoreDelta) getApplication().getAppStatusLock().get("currentScoreDelta"));
                    scheduleSolver.scheduleLatest.scoreexplaination =
                            (StringBuilder) getApplication().getAppStatusLock().get("scoreexplaination");
                    if (solverLock.mode.equals(SolverModes.RESET)) {
                        resetSchedule();
                    } else if (solverLock.mode.equals(SolverModes.RESTART)) {
                        resetSchedule();
                        scheduleSolver.solve(scheduleSolver.scheduleLatest.schedule);
                    } else {
                        scheduleSolver.solve(scheduleSolver.scheduleLatest.schedule);
                    }
                } catch (Exception ex) {
                    String msg = Throwables.getStackTraceAsString(ex);
                    System.err.println(msg);
                }
            }
        }
    }


    public void resetSchedule() {
        scheduleSolver.scheduleLatest.schedule = new DataStructureBuilder(
                scheduleSolver.scheduleLatest.schedule.getOriginalValueEntryMap(),
                scheduleSolver.scheduleLatest.schedule.getProblemTimelineBlock(),
                scheduleSolver.scheduleLatest.schedule.getOriginalTimeHierarchyMap(),
                scheduleSolver.scheduleLatest.schedule.getOriginalLocationHierarchyMap(), false)
                .constructChainProperty().getSchedule();
    }

    public void terminate() {
        scheduleSolver.terminate();
    }

    public void solve(SolverModes mode) {
        terminate();
        synchronized (solverLock) {
            solverLock.mode = mode;
            solverLock.notify();
        }
    }


    public FileServer getFileServer() {
        return fileServer;
    }

    public void setFileServer(FileServer fileServer) {
        this.fileServer = fileServer;
    }

    public TCApp getApplication() {
        return application;
    }

    public void setApplication(TCApp application) {
        this.application = application;
    }

    public Schedule getCurrentSchedule() {
        return scheduleSolver.scheduleLatest.schedule;
    }

    public void setCurrentSchedule(Schedule schedule) {
        scheduleSolver.scheduleLatest.schedule = schedule;
    }

}
