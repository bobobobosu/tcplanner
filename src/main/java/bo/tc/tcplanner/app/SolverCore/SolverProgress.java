package bo.tc.tcplanner.app.SolverCore;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

public class SolverProgress implements Serializable {
    SolverScoreDelta solverScoreDelta;
    long totalEntities;
    long unsolvedEntities;
    String solvePhaseName;
    List<String> addedTimelineEntries;
    UUID scheduleId;

    public SolverScoreDelta getSolverScoreDelta() {
        return solverScoreDelta;
    }

    public void setSolverScoreDelta(SolverScoreDelta solverScoreDelta) {
        this.solverScoreDelta = solverScoreDelta;
    }

    public long getTotalEntities() {
        return totalEntities;
    }

    public void setTotalEntities(long totalEntities) {
        this.totalEntities = totalEntities;
    }

    public long getUnsolvedEntities() {
        return unsolvedEntities;
    }

    public void setUnsolvedEntities(long unsolvedEntities) {
        this.unsolvedEntities = unsolvedEntities;
    }

    public List<String> getAddedTimelineEntries() {
        return addedTimelineEntries;
    }

    public void setAddedTimelineEntries(List<String> addedTimelineEntries) {
        this.addedTimelineEntries = addedTimelineEntries;
    }

    public String getSolvePhaseName() {
        return solvePhaseName;
    }

    public void setSolvePhaseName(String solvePhaseName) {
        this.solvePhaseName = solvePhaseName;
    }

    public UUID getScheduleId() {
        return scheduleId;
    }

    public void setScheduleId(UUID scheduleId) {
        this.scheduleId = scheduleId;
    }
}
