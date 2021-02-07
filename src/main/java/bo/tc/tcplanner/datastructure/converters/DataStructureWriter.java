package bo.tc.tcplanner.datastructure.converters;

import bo.tc.tcplanner.PropertyConstants;
import bo.tc.tcplanner.datastructure.*;
import bo.tc.tcplanner.domain.planningstructures.Allocation;
import bo.tc.tcplanner.domain.planningstructures.Schedule;
import com.google.common.collect.Sets;
import org.optaplanner.core.api.score.ScoreExplanation;
import org.optaplanner.core.api.score.ScoreManager;
import org.optaplanner.core.api.score.buildin.hardmediumsoftlong.HardMediumSoftLongScore;
import org.optaplanner.core.api.score.constraint.Indictment;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static bo.tc.tcplanner.PropertyConstants.defaultTimeZone;
import static bo.tc.tcplanner.PropertyConstants.dtf_TimelineEntry;
import static bo.tc.tcplanner.app.SolverCore.ScheduleSolver.getScoringScoreManager;
import static bo.tc.tcplanner.app.SolverCore.Toolbox.hardConstraintMatchToString;
import static bo.tc.tcplanner.app.SolverCore.Toolbox.jacksonDeepCopy;

public class DataStructureWriter {
    public TimelineBlock generateTimelineBlockScore(Schedule result) {
        TimelineBlock timelineBlock = generateTimelineBlock(result);
        ScoreManager<Schedule, HardMediumSoftLongScore> scoreManager = getScoringScoreManager();
        ScoreExplanation<Schedule, HardMediumSoftLongScore> scoreExplanation = scoreManager.explainScore(result);
        Map<Integer, Indictment> breakByTasks = new HashMap<>();
        for (Map.Entry<Object, Indictment<HardMediumSoftLongScore>> indictmentEntry : scoreExplanation.getIndictmentMap().entrySet()) {
            if (indictmentEntry.getValue().getJustification() instanceof Allocation &&
                    indictmentEntry.getValue().getScore().getHardScore() < 0) {
                Allocation matchAllocation = (Allocation) indictmentEntry.getValue().getJustification();
                if (matchAllocation.getTimelineEntry().getTimelineProperty().getTimelineid() != null)
                    breakByTasks.put(matchAllocation.getTimelineEntry().getTimelineProperty().getTimelineid(), indictmentEntry.getValue());
            }
        }

        for (TimelineEntry timelineEntry : timelineBlock.getTimelineEntryList()) {
            timelineEntry.setScore(breakByTasks.containsKey(timelineEntry.getTimelineProperty().getTimelineid()) ?
                    hardConstraintMatchToString(breakByTasks.get(timelineEntry.getTimelineProperty().getTimelineid()).getConstraintMatchSet()) : "");
        }
        return timelineBlock;
    }

    public TimelineBlock generateTimelineBlock(final Schedule result) {

        TimelineBlock oldTimelineBlock = (TimelineBlock) jacksonDeepCopy(result.getProblemTimelineBlock());

        TimelineBlock timelineBlock = new TimelineBlock()
                .setBlockStartTime(oldTimelineBlock.getBlockStartTime())
                .setBlockEndTime(oldTimelineBlock.getBlockEndTime())
                .setBlockScheduleAfter(oldTimelineBlock.getBlockScheduleAfter())
                .setOrigin("tcplannercore");

        // generate ids
        Map<Allocation, Integer> allocationRealidMap = new IdentityHashMap<>();
        Map<TimelineEntry, List<Allocation>> timelineEntryListMap =
                result.getAllocationList()
                        .stream()
                        .filter(x -> !x.getTimelineEntry().isVolatileFlag())
                        .collect(Collectors.groupingBy(Allocation::getTimelineEntry));
        AtomicInteger idcounter = new AtomicInteger(-1);
        result.getAllocationList().stream().filter(x -> !x.getTimelineEntry().isVolatileFlag()).forEach(x -> {
            if (x.getTimelineEntry().getTimelineProperty().getTimelineid() == null) {
                allocationRealidMap.put(x, idcounter.getAndDecrement());
            } else {
                allocationRealidMap.put(x, x.getTimelineEntry().getTimelineProperty().getTimelineid());
            }
        });

        // generate details
        List<Allocation> allocationList = result.getFocusedAllocationList();


        // create result TimelineEntry
        List<TimelineEntry> TEList = new ArrayList<>();
        for (int i = 0; i < allocationList.size(); i++) {
            Allocation allocation = allocationList.get(i);

            if (allocation.getTimelineEntry().isVolatileFlag()) continue;

            // Initialize
            TimelineEntry TE = new TimelineEntry();

            // Basic property
            TE.setTitle(allocation.getTimelineEntry().getTitle())
                    .setDescription(allocation.getTimelineEntry().getDescription())
                    .setExecutionMode(allocation.getTimelineEntry().getExecutionMode());

            // Chronological Property
            TE.setChronoProperty(new ChronoProperty(allocation.getTimelineEntry().getChronoProperty())
                    .setStartTime(allocation.getConstrainedStartDate().withZoneSameInstant(
                            allocation.getTimelineEntry().getChronoProperty().getZonedStartTime() != null ?
                                    allocation.getTimelineEntry().getChronoProperty().getZonedStartTime().getZone() :
                                    defaultTimeZone).format(dtf_TimelineEntry)));


            // Timeline Property Reset timelineid
            TE.setTimelineProperty(new TimelineProperty(allocation.getTimelineEntry().getTimelineProperty())
                    .setTimelineid(allocationRealidMap.get(allocation)));

            // Progress Change
            TE.setProgressChange(new ProgressChange(allocation.getTimelineEntry().getProgressChange())
                    .setProgressDelta((double) allocation.getConstrainedProgressDelta() / 100));

            // Resource State Change
            TE.setResourceStateChange(new ResourceStateChange(allocation.getTimelineEntry().getResourceStateChange())
                    .removeVolatile()
                    .removeEmpty());

            // Human State Change
            TE.setHumanStateChange(new HumanStateChange(allocation.getTimelineEntry().getHumanStateChange())
                    .setDuration(allocation.getPlannedDuration().toMinutes())
            );

            TE.getTimelineProperty().setDependencyIdList(
                    new ArrayList<>(Sets.newHashSet(TE.getTimelineProperty().getDependencyIdList())));
            TEList.add(TE);

        }

        // Build Rownum
        Set<Integer> rownumList = TEList
                .stream()
                .filter(x -> x.getTimelineProperty().getRownum() > 0)
                .map(x -> x.getTimelineProperty().getRownum())
                .collect(Collectors.toCollection(TreeSet::new));

        Iterator<Integer> rownumIterator = rownumList.iterator();
        int tmprownum = Collections.min(rownumList) - 1;

        for (TimelineEntry timelineEntry : TEList) {
            if (timelineEntry.getTimelineProperty().getTimelineid() > 0 && rownumIterator.hasNext()) {
                tmprownum = rownumIterator.next();
            }
            timelineEntry.getTimelineProperty().setRownum(tmprownum);
        }

        // Add deleted
        result.getAllocationList().stream().filter(x ->
                !x.isFocused() && x.getTimelineEntry().getTimelineProperty().getTimelineid() != null)
                .forEach(x ->
                        TEList.add(new TimelineEntry()
                                .setTimelineProperty(new TimelineProperty()
                                        .setTimelineid(x.getTimelineEntry().getTimelineProperty().getTimelineid())
                                        .setPlanningWindowType(
                                                PropertyConstants.PlanningWindowTypes.types.Deleted.name())
                                ))
                );

        timelineBlock.setTimelineEntryList(TEList);

        int percentComplete = 100;
        timelineBlock.setScore(TEList.get(TEList.size() - 1).getTimelineProperty().getRownum() + "(" + percentComplete + "%)" +
                (result.getScore() != null ? result.getScore().toShortString() : ""));

        return timelineBlock;
    }
}
