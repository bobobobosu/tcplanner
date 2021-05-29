package bo.tc.tcplanner.datastructure.converters;

import bo.tc.tcplanner.PropertyConstants;
import bo.tc.tcplanner.datastructure.*;
import bo.tc.tcplanner.domain.filters.ProgressDeltaCanChangeFilter;
import bo.tc.tcplanner.domain.planningstructures.Allocation;
import bo.tc.tcplanner.domain.planningstructures.AllocationKey;
import bo.tc.tcplanner.domain.planningstructures.ResourceTotal;
import bo.tc.tcplanner.domain.planningstructures.Schedule;
import org.optaplanner.core.api.score.ScoreExplanation;
import org.optaplanner.core.api.score.ScoreManager;
import org.optaplanner.core.api.score.buildin.hardmediumsoftlong.HardMediumSoftLongScore;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static bo.tc.tcplanner.PropertyConstants.*;
import static bo.tc.tcplanner.app.SolverCore.DroolsTools.getConstrintedTimeRange;
import static bo.tc.tcplanner.app.SolverCore.ScheduleSolver.getScoringScoreManager;
import static bo.tc.tcplanner.domain.listeners.ListenerTools.*;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public class DataStructureBuilder {
    Schedule schedule;
    List<Allocation> fullAllocationList;

    public DataStructureBuilder(ValueEntryMap valueEntryMap,
                                TimelineBlock timelineBlock,
                                TimeHierarchyMap timeHierarchyMap,
                                LocationHierarchyMap locationHierarchyMap,
                                boolean minimal) throws IllegalArgumentException {
        checkNotNull(valueEntryMap);
        checkNotNull(timelineBlock);
        checkNotNull(timeHierarchyMap);
        checkNotNull(locationHierarchyMap);
        checkArgument(valueEntryMap.checkValid());
        checkArgument(timelineBlock.checkValid());
        checkArgument(timeHierarchyMap.checkValid());
        checkArgument(locationHierarchyMap.checkValid());

        schedule = new Schedule()
                .setValueEntryMap(new ValueEntryMap(valueEntryMap))
                .setTimeEntryMap(new TimeEntryMap())
                .setOriginalValueEntryMap(valueEntryMap)
                .setOriginalTimeHierarchyMap(timeHierarchyMap)
                .setOriginalLocationHierarchyMap(new LocationHierarchyMap(locationHierarchyMap))
                .setProblemTimelineBlock(new TimelineBlock(timelineBlock));
        schedule.getTimeEntryMap().putAll(timeHierarchyMap.keySet().stream().collect(Collectors.toMap(
                x -> x,
                x -> getConstrintedTimeRange(timeHierarchyMap, x,
                        timelineBlock.getZonedBlockStartTime(),
                        timelineBlock.getZonedBlockEndTime())
        )));

        // Set Constants
        schedule.special.dummyLocation = PropertyConstants.dummyLocation;
        schedule.special.dummyTime = PropertyConstants.dummyTime;
        schedule.special.dummyHumamStateChange = new HumanStateChange()
                .setDuration(0)
                .setCurrentLocation(schedule.special.dummyLocation)
                .setMovetoLocation(schedule.special.dummyLocation)
                .setRequirementTimerange(schedule.special.dummyTime)
                .setAdviceTimerange(schedule.special.dummyTime);
        schedule.special.dummyProgressChange = new ProgressChange()
                .setProgressDelta(dummyProgressDelta)
                .setProgressLog(new ArrayList<>())
                .setProgressPreset(new ArrayList<>());
        schedule.special.dummyResourceStateChange = new ResourceStateChange()
                .setResourceChange(new ResourceElementMap())
                .setMode(PropertyConstants.ResourceStateChangeTypes.types.delta.name());
        schedule.special.dummyChronoProperty = new ChronoProperty()
                .setDraggable(1)
                .setSubstitutable(1)
                .setSplittable(1)
                .setGravity(0)
                .setStartTime(schedule.getProblemTimelineBlock().getBlockStartTime())
                .setDeadline(schedule.getProblemTimelineBlock().getBlockEndTime())
                .setAliveline(schedule.getProblemTimelineBlock().getBlockStartTime())
                .setPriority(defaultPriority);
        schedule.special.dummyTimelineProperty = new TimelineProperty()
                .setRownum(0)
                .setTimelineid(null)
                .setDependencyIdList(new ArrayList<>())
                .setTaskChainIdList(new ArrayList<>())
                .setPlanningWindowType(PropertyConstants.PlanningWindowTypes.types.Draft.name());

        // Set Planning Facts
        Allocation sourceAllocation = new Allocation()
                .setVolatileFlag(true)
                .setSchedule(schedule);
        sourceAllocation.setTimelineEntry(new TimelineEntry()
                .setVolatileFlag(true)
                .setTitle("source")
                .setDescription("")
                .setExecutionMode(0)
                .setHumanStateChange(schedule.special.dummyHumamStateChange)
                .setProgressChange(new ProgressChange().setProgressDelta(1))
                .setResourceStateChange(new ResourceStateChange(schedule.special.dummyResourceStateChange))
                .setChronoProperty(new ChronoProperty()
                        .setDraggable(0).setSubstitutable(0).setSplittable(0).setGravity(0)
                        .setStartTime(schedule.getProblemTimelineBlock().getZonedBlockStartTime().minusNanos(1).toString())
                        .setDeadline(schedule.getProblemTimelineBlock().getZonedBlockEndTime().toString())
                        .setAliveline(schedule.getProblemTimelineBlock().getZonedBlockStartTime().minusNanos(1).toString())
                        .setPriority(defaultPriority))
                .setTimelineProperty(new TimelineProperty(schedule.special.dummyTimelineProperty)
                        .setPlanningWindowType(PropertyConstants.PlanningWindowTypes.types.History.name())));
        Allocation sinkAllocation = new Allocation()
                .setVolatileFlag(true)
                .setSchedule(schedule);
        sinkAllocation.setTimelineEntry(new TimelineEntry()
                .setVolatileFlag(true)
                .setTitle("sink")
                .setDescription("")
                .setExecutionMode(0)
                .setHumanStateChange(schedule.special.dummyHumamStateChange)
                .setProgressChange(new ProgressChange().setProgressDelta(1))
                .setResourceStateChange(new ResourceStateChange(schedule.special.dummyResourceStateChange))
                .setChronoProperty(new ChronoProperty()
                        .setDraggable(0).setSubstitutable(0).setSplittable(0).setGravity(0)
                        .setStartTime(schedule.getProblemTimelineBlock().getZonedBlockEndTime().plusNanos(1).toString())
                        .setDeadline(schedule.getProblemTimelineBlock().getZonedBlockEndTime().plusNanos(1).toString())
                        .setAliveline(schedule.getProblemTimelineBlock().getZonedBlockStartTime().toString())
                        .setPriority(defaultPriority))
                .setTimelineProperty(new TimelineProperty(schedule.special.dummyTimelineProperty)
                        .setRownum(Integer.MAX_VALUE)
                        .setPlanningWindowType(PropertyConstants.PlanningWindowTypes.types.History.name())));

        // This allocation passes through all filters, this prevents selectors throw `n must be positive` exception
        Allocation dummyAllocation = new Allocation()
                .setVolatileFlag(true)
                .setSchedule(schedule);
        dummyAllocation.setTimelineEntry(new TimelineEntry()
                .setVolatileFlag(true)
                .setTitle("dummy")
                .setDescription("")
                .setExecutionMode(0)
                .setHumanStateChange(schedule.special.dummyHumamStateChange)
                .setProgressChange(new ProgressChange().setProgressDelta(1))
                .setResourceStateChange(new ResourceStateChange(schedule.special.dummyResourceStateChange))
                .setChronoProperty(new ChronoProperty()
                        .setDraggable(1).setSubstitutable(1).setSplittable(1).setGravity(0)
                        .setStartTime(schedule.getProblemTimelineBlock().getZonedBlockEndTime().plusNanos(1).toString())
                        .setDeadline(schedule.getProblemTimelineBlock().getZonedBlockEndTime().plusNanos(1).toString())
                        .setAliveline(schedule.getProblemTimelineBlock().getZonedBlockStartTime().toString())
                        .setPriority(defaultPriority))
                .setTimelineProperty(new TimelineProperty(schedule.special.dummyTimelineProperty)
                        .setRownum(Integer.MAX_VALUE)
                        .setPlanningWindowType(PropertyConstants.PlanningWindowTypes.types.History.name())));

        // Initialize Lists with dummy facts
        // first and second allocation must be source and sink
        sourceAllocation.addToListSetIndex(schedule.getAllocationList());
        sinkAllocation.addToListSetIndex(schedule.getAllocationList());
        dummyAllocation.addToListSetIndex(schedule.getAllocationList());
        // First, second timelineEntry must follow this order
        schedule.setTimelineEntryList(new ArrayList<>(
                Arrays.asList(
                        sourceAllocation.getTimelineEntry(),
                        sinkAllocation.getTimelineEntry())));
        schedule.setJobIdx2jobcloneIdxMap(new HashMap<>());

        // Add TimelineEntries from valueEntryMap
        // Add jobs from ValueEntryMap
        schedule.getValueEntryMap().entrySet().stream()
                .filter(x -> Arrays.asList("task", "resource").contains(x.getValue().getType()))
                .forEach(y -> {
                    for (int i = 0; i < y.getValue().getResourceStateChangeList().size(); i++) {
                        TimelineEntry timelineEntry = new TimelineEntry()
                                .setExecutionMode(i)
                                .setTitle(y.getKey())
                                .setDescription("")
                                .setProgressChange(y.getValue().getProgressChangeList().get(i)
                                        .setProgressLog(new ArrayList<>()))
                                .setHumanStateChange(y.getValue().getHumanStateChangeList().get(i))
                                .setResourceStateChange(y.getValue().getResourceStateChangeList().get(i))
                                .setChronoProperty(new ChronoProperty(y.getValue().getChronoProperty())
                                        .setStartTime(schedule.getProblemTimelineBlock().getBlockStartTime())
                                        .setDeadline(schedule.getProblemTimelineBlock().getBlockEndTime())
                                        .setAliveline(schedule.getProblemTimelineBlock().getBlockStartTime())
                                        .setPriority(defaultPriority))
                                .setTimelineProperty(new TimelineProperty()
                                        .setRownum(0)
                                        .setTimelineid(null)
                                        .setDependencyIdList(new ArrayList<>())
                                        .setTaskChainIdList(new ArrayList<>())
                                        .setPlanningWindowType(PropertyConstants.PlanningWindowTypes.types.Draft.name()));
                        schedule.getTimelineEntryList().add(timelineEntry);
//                        if (!minimal) {
                        IntStream.range(0, 300).forEach(x -> {
                            Allocation allocation = new Allocation()
                                    .setVolatileFlag(false)
                                    .setSchedule(schedule);
                            allocation.setTimelineEntry(timelineEntry);
                            allocation.addToListSetIndex(schedule.getAllocationList());
                        });
//                        }
                    }
                });

        // add jobs from ProblemTimelineBlock
        schedule.getProblemTimelineBlock().getTimelineEntryList().stream()
                .filter(x -> !x.getTimelineProperty().getPlanningWindowType()
                        .equals(PropertyConstants.PlanningWindowTypes.types.Deleted.name()))
                .forEach(y -> {
                    // timeline job resource
                    ResourceElement jobResourceElement = new ResourceElement()
                            .setVolatileFlag(true)
                            .setAmt(y.getProgressChange().getProgressDelta100())
                            .setLocation(schedule.special.dummyLocation)
                            .setPriorityTimelineIdList(new ArrayList<>());
                    schedule.getValueEntryMap().put(y.getTimelineProperty().getTimelineid().toString(),
                            new ValueEntry().setVolatileFlag(true).setCapacity((double) y.getProgressChange().getProgressDelta100()).setType("task").setClassification("*"));


                    // add real job
                    TimelineEntry timelineEntry;
                    timelineEntry = new TimelineEntry(y);
                    timelineEntry.getResourceStateChange().addResourceElementToChange(y.getTimelineProperty().getTimelineid().toString(),
                            jobResourceElement);
                    Allocation allocation = new Allocation().setSchedule(schedule);
                    allocation.setTimelineEntry(timelineEntry);
                    schedule.getTimelineEntryList().add(timelineEntry);
                    allocation.addToListSetIndex(schedule.getAllocationList());

                    // add job clone for splittable entries
                    if (timelineEntry.getChronoProperty().getSplittable() == 1) {
                        TimelineEntry timelineEntryClone;
                        timelineEntryClone = new TimelineEntry(timelineEntry)
                                .setChronoProperty(new ChronoProperty(y.getChronoProperty())
                                        .setDraggable(1)
                                        .setDraggable(1)
                                        .setSubstitutable(1))
                                .setTimelineProperty(new TimelineProperty(y.getTimelineProperty())
                                        .setTimelineid(null)
                                        .setPlanningWindowType(PropertyConstants.PlanningWindowTypes.types.Draft.name()));
                        schedule.getTimelineEntryList().add(timelineEntryClone);

                        // Update Map
                        schedule.getJobIdx2jobcloneIdxMap().put(allocation.getIndex(), new HashSet<>());
                        if (!minimal) {
                            IntStream.range(0, 100).forEach(x -> {
                                Allocation allocation1 = new Allocation()
                                        .setVolatileFlag(true)
                                        .setSchedule(schedule);
                                allocation1.setTimelineEntry(timelineEntryClone);
                                allocation1.addToListSetIndex(schedule.getAllocationList());
                                schedule.getJobIdx2jobcloneIdxMap().get(allocation.getIndex()).add(allocation1.getIndex());
                            });
                        }
                    }
                });

        // Set pinned
        schedule.getAllocationList().forEach(x -> x.setPinned(false));
        schedule.getAllocationList().stream().filter(
                x -> ((x.getTimelineEntry().getChronoProperty().getDraggable() == 0) &&
                        (x.getTimelineEntry().getChronoProperty().getSubstitutable() == 0) &&
                        (x.getTimelineEntry().getChronoProperty().getSplittable() == 0)) ||
                        x.getTimelineEntry().getTimelineProperty().getPlanningWindowType()
                                .equals(PlanningWindowTypes.types.History.name())
        ).forEach(x -> x.setPinned(true));

        // Set Sink Allocation Priority
        schedule.getSinkAllocation().getTimelineEntry().getChronoProperty().setPriority(schedule.getAllocationList().size());
        fullAllocationList = new ArrayList<>(schedule.getAllocationList());
    }

    public DataStructureBuilder constructChainProperty() {
        return constructChainProperty(schedule);
    }

    public DataStructureBuilder constructChainProperty(Schedule schedule) {
        Allocation prevAllocation, thisAllocation;

        // Set Planning Start Date
        schedule.getAllocationList().stream().filter(x ->
                Arrays.asList(PlanningWindowTypes.types.History.name(), PlanningWindowTypes.types.Published.name())
                        .contains(x.getTimelineEntry().getTimelineProperty().getPlanningWindowType()))
                .forEach(x -> x.setPlanningStartDate(x.getTimelineEntry().getChronoProperty().getZonedStartTime()));

        // Easy Access
        TreeMap<AllocationKey, Allocation> focusedAllocationSet = new TreeMap<>();
        schedule.getAllocationList().stream().filter(Allocation::isFocused).forEach(x -> focusedAllocationSet.put(
                x.getAllocationKey(), x
        ));
        schedule.focusedAllocationSet = focusedAllocationSet;
        List<Allocation> focusedAllocationList = new ArrayList<>(schedule.focusedAllocationSet.values());

        // Set Weight
        schedule.getAllocationList().forEach(x -> {
            x.setWeight(1);
            x.setSchedule(schedule);
        });
        schedule.getSinkAllocation().setWeight(focusedAllocationList.size());
        for (int i = 0; i < focusedAllocationList.size(); i++) {
            focusedAllocationList.get(i).setWeight(focusedAllocationList.size() - i);
        }

        // Set Scheduled Job requirement
        schedule.getSinkAllocation().getTimelineEntry().getResourceStateChange().setResourceChange(new ResourceElementMap());
        schedule.getAllocationList().stream()
                .filter(
                        x -> x.getTimelineEntry().getTimelineProperty().getPlanningWindowType()
                                .equals(PropertyConstants.PlanningWindowTypes.types.Published.name()) &&
                                x.getTimelineEntry().getChronoProperty().getSubstitutable() == 0)
                .forEach(x ->
                        schedule.getSinkAllocation().getTimelineEntry().getResourceStateChange().getResourceChange()
                                .put(x.getTimelineEntry().getTimelineProperty().getTimelineid().toString(),
                                        new ArrayList<>(Arrays.asList(
                                                new ResourceElement()
                                                        .setVolatileFlag(true)
                                                        .setAmt(-x.getTimelineEntry().getProgressChange().getProgressDelta100())
                                                        .setLocation(schedule.special.dummyLocation)
                                                        .setPriorityTimelineIdList(new ArrayList<>())))));

        // TimelineEntry to Allocation Map
        schedule.setTimelineEntry2AllocationIdxMap(new HashMap<>());
        schedule.getAllocationList().forEach(x -> {
            schedule.getTimelineEntry2AllocationIdxMap().putIfAbsent(x.getTimelineEntry(), new HashSet<>());
            schedule.getTimelineEntry2AllocationIdxMap().get(x.getTimelineEntry()).add(x.getIndex());
        });

        // resource to Allocation Map
        schedule.setResourceTotalKeyallocationIdxMap(new HashMap<>());
        schedule.getAllocationList().forEach(x -> {
            x.getTimelineEntry().getDeltaResourceTotalKeys().forEach(y -> {
                if (!schedule.getResourceTotalKeyallocationIdxMap().containsKey(y))
                    schedule.getResourceTotalKeyallocationIdxMap().put(y, new HashSet<>());
                schedule.getResourceTotalKeyallocationIdxMap().get(y).add(x.getIndex());
            });
        });

        // Set ProgressDelta
        ProgressDeltaCanChangeFilter progressDeltaCanChangeFilter = new ProgressDeltaCanChangeFilter();
        schedule.getAllocationList().forEach(x -> x.setProgressdelta(
                x.getTimelineEntry().getProgressChange().getProgressDelta100()));
        schedule.getAllocationList().stream().filter(x ->
                !x.isFocused() && progressDeltaCanChangeFilter.accept(null, x)).forEach(
                y -> y.setProgressdelta(1));

        // Set PreviousStandstill <null if not focused>
        schedule.getAllocationList().forEach(x -> x.setPreviousStandstill(null));
        if ((thisAllocation = focusedAllocationList.get(0)) != null) {
            prevAllocation = thisAllocation.getPrevAllocation();
            while (thisAllocation != null) {
                updateAllocationPreviousStandstill(null, prevAllocation, thisAllocation);
                thisAllocation = (prevAllocation = thisAllocation).getNextAllocation();
            }
        }

        // Set NextStartDate <null if not focused>
        schedule.getAllocationList().forEach(x -> x.setNextStartDate(null));
        thisAllocation = focusedAllocationList.get(0);
        while (thisAllocation != null) {
            updateNextStartDate(null, thisAllocation, thisAllocation = thisAllocation.getNextAllocation());
        }

        // Set PlannedDuration <never null>
        schedule.getAllocationList().forEach(x -> updatePlanningDuration(null, x));

        // set ResourceElementMap <null if not focused>
        schedule.getAllocationList().forEach(x -> x.setResourceTotal(
                x.isFocused() ? new ResourceTotal() : null));
        schedule.getResourceTotalKeyallocationIdxMap().forEach((k, v) -> {
            Allocation a = schedule.getAllocationList().get(v.iterator().next());
            applyResourceTotalMap(null, a, schedule.getSourceAllocation());
        });


        // set Solver Phase
        schedule.solverPhase = SolverPhase.ACCURATE;
        ScoreManager<Schedule, HardMediumSoftLongScore> scoreManager = getScoringScoreManager();
        ScoreExplanation<Schedule, HardMediumSoftLongScore> scoreExplanation = scoreManager.explainScore(schedule);
        return this;
    }

    public Schedule getSchedule() {
        return schedule;
    }

    public DataStructureBuilder setSchedule(Schedule schedule) {
        this.schedule = schedule;
        return this;
    }

    public List<Allocation> getFullAllocationList() {
        return fullAllocationList;
    }
}
