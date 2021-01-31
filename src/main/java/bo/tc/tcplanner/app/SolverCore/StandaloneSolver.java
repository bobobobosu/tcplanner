package bo.tc.tcplanner.app.SolverCore;

import bo.tc.tcplanner.datastructure.LocationHierarchyMap;
import bo.tc.tcplanner.datastructure.TimeHierarchyMap;
import bo.tc.tcplanner.datastructure.TimelineBlock;
import bo.tc.tcplanner.datastructure.ValueEntryMap;
import bo.tc.tcplanner.datastructure.converters.DataStructureBuilder;
import bo.tc.tcplanner.datastructure.converters.DataStructureWriter;
import bo.tc.tcplanner.domain.planningstructures.Schedule;

public class StandaloneSolver {
    // User Data
    ValueEntryMap valueEntryMap;
    TimeHierarchyMap timeHierarchyMap;
    LocationHierarchyMap locationHierarchyMap;
    // Current result
    ScheduleSolver scheduleSolver = new ScheduleSolver();

    public void solveTimelineBlock(TimelineBlock timelineBlock) {
        scheduleSolver.solve(new DataStructureBuilder(
                valueEntryMap, timelineBlock, timeHierarchyMap, locationHierarchyMap, false)
                .constructChainProperty().getSchedule());
    }

    public void solveSchedule(Schedule schedule) {
        scheduleSolver.solve(schedule);
    }

    public void setUserData(ValueEntryMap valueEntryMap,
                            TimeHierarchyMap timeHierarchyMap,
                            LocationHierarchyMap locationHierarchyMap) {
        this.valueEntryMap = valueEntryMap;
        this.timeHierarchyMap = timeHierarchyMap;
        this.locationHierarchyMap = locationHierarchyMap;
    }

    public Schedule getBestSchedule() {
        return scheduleSolver.scheduleLatest.schedule;
    }

    public TimelineBlock getBestTimelineBlock() {
        return new DataStructureWriter().generateTimelineBlockScore(scheduleSolver.scheduleLatest.schedule);
    }

    public void stop() {
        scheduleSolver.terminate();
    }

    public void start() {
        scheduleSolver.solve(scheduleSolver.scheduleLatest.schedule);
    }
}
