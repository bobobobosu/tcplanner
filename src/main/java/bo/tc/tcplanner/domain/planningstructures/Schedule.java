/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package bo.tc.tcplanner.domain.planningstructures;

import bo.tc.tcplanner.PropertyConstants.SolverPhase;
import bo.tc.tcplanner.datastructure.*;
import bo.tc.tcplanner.persistable.AbstractPersistable;
import com.google.common.collect.Lists;
import org.optaplanner.core.api.domain.solution.PlanningEntityCollectionProperty;
import org.optaplanner.core.api.domain.solution.PlanningScore;
import org.optaplanner.core.api.domain.solution.PlanningSolution;
import org.optaplanner.core.api.domain.solution.drools.ProblemFactCollectionProperty;
import org.optaplanner.core.api.domain.solution.drools.ProblemFactProperty;
import org.optaplanner.core.api.score.buildin.hardmediumsoftlong.HardMediumSoftLongScore;
import org.optaplanner.core.impl.score.director.ScoreDirector;

import java.io.Serializable;
import java.util.*;

import static bo.tc.tcplanner.app.SolverCore.ScheduleSolver.getScoringScoreDirector;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

@PlanningSolution
public class Schedule extends AbstractPersistable {

    public Special special;
    public TreeMap<AllocationKey, Allocation> focusedAllocationSet;
    // Settings
    public SolverPhase solverPhase;
    // Domain
    private List<Allocation> allocationList;
    private List<TimelineEntry> timelineEntryList;
    // Objects
    private TimelineBlock problemTimelineBlock;
    private ValueEntryMap valueEntryMap;
    private TimeEntryMap timeEntryMap;
    private ValueEntryMap originalValueEntryMap;
    private TimeHierarchyMap originalTimeHierarchyMap;
    private LocationHierarchyMap originalLocationHierarchyMap;
    // Easy Access
    private Map<Integer, HashSet<Integer>> jobIdx2jobcloneIdxMap;
    private Map<ResourceTotalKey, HashSet<Integer>> resourceTotalKeyallocationIdxMap;
    private Map<TimelineEntry, HashSet<Integer>> timelineEntry2AllocationIdxMap;

    //    @XStreamConverter(BendableScoreXStreamConverter.class)

    private HardMediumSoftLongScore score;

    public Schedule() {
        //Initialize
        timelineEntryList = new ArrayList<TimelineEntry>();
        allocationList = new ArrayList<>();
        special = new Special();
    }

    // Source allocation should always be the first
    public Allocation getSourceAllocation() {
        return allocationList.get(0);
    }

    // Sink allocation should always be the second
    public Allocation getSinkAllocation() {
        return allocationList.get(1);
    }

    @Override
    public boolean checkValid() {
        try {
            checkNotNull(allocationList);
            checkNotNull(timelineEntryList);
            checkNotNull(problemTimelineBlock);
            checkNotNull(valueEntryMap);
            checkNotNull(timeEntryMap);
            checkNotNull(originalLocationHierarchyMap);
            checkNotNull(jobIdx2jobcloneIdxMap);
            checkNotNull(resourceTotalKeyallocationIdxMap);
            checkNotNull(timelineEntry2AllocationIdxMap);
            checkArgument(allocationList.stream().allMatch(Allocation::checkValid));
            checkArgument(timelineEntryList.stream().allMatch(TimelineEntry::checkValid));
            checkArgument(valueEntryMap.checkValid());
            checkArgument(timeEntryMap.checkValid());
            checkArgument(originalLocationHierarchyMap.checkValid());
            return true;
        } catch (IllegalArgumentException | NullPointerException ex) {
            throw new IllegalArgumentException(this.toString(), ex);
        }
    }

    @Override
    public Schedule removeVolatile() {
        timelineEntryList.removeIf(AbstractPersistable::isVolatileFlag);
        timelineEntryList.forEach(TimelineEntry::removeVolatile);
        valueEntryMap.forEach((k, v) -> v.removeVolatile());
        valueEntryMap.entrySet().removeIf(x -> x.getValue().isVolatileFlag());
        allocationList.removeIf(AbstractPersistable::isVolatileFlag);
        allocationList.forEach(Allocation::removeVolatile);
        return this;
    }

    @Override
    public AbstractPersistable removeEmpty() {
        return this;
    }

    @ProblemFactProperty
    public ValueEntryMap getValueEntryMap() {
        return valueEntryMap;
    }

    public Schedule setValueEntryMap(ValueEntryMap valueEntryMap) {
        this.valueEntryMap = valueEntryMap;
        return this;
    }

    @ProblemFactProperty
    public LocationHierarchyMap getOriginalLocationHierarchyMap() {
        return originalLocationHierarchyMap;
    }

    public Schedule setOriginalLocationHierarchyMap(LocationHierarchyMap originalLocationHierarchyMap) {
        this.originalLocationHierarchyMap = originalLocationHierarchyMap;
        return this;
    }

    @ProblemFactProperty
    public TimeEntryMap getTimeEntryMap() {
        return timeEntryMap;
    }

    public Schedule setTimeEntryMap(TimeEntryMap timeEntryMap) {
        this.timeEntryMap = timeEntryMap;
        return this;
    }

    @ProblemFactCollectionProperty
    public List<TimelineEntry> getTimelineEntryList() {
        return timelineEntryList;
    }

    public Schedule setTimelineEntryList(List<TimelineEntry> timelineEntryList) {
        this.timelineEntryList = timelineEntryList;
        return this;
    }

    @PlanningEntityCollectionProperty
    public List<Allocation> getAllocationList() {
        return allocationList;
    }

    public Schedule setAllocationList(List<Allocation> allocationList) {
        this.allocationList = allocationList;
        return this;
    }

    @PlanningScore()
    public HardMediumSoftLongScore getScore() {
        return score;
    }

    public void setScore(HardMediumSoftLongScore score) {
        this.score = score;
    }

    public TimelineBlock getProblemTimelineBlock() {
        return problemTimelineBlock;
    }

    public Schedule setProblemTimelineBlock(TimelineBlock problemTimelineBlock) {
        this.problemTimelineBlock = problemTimelineBlock;
        return this;
    }

    public List<Allocation> getFocusedAllocationList() {
        return Lists.newArrayList(focusedAllocationSet.values());
    }

    public TreeMap<AllocationKey, Allocation> getFocusedAllocationSet() {
        return focusedAllocationSet;
    }

    public void setFocusedAllocationSet(TreeMap<AllocationKey, Allocation> focusedAllocationSet) {
        this.focusedAllocationSet = focusedAllocationSet;
    }


    // Easy Access
    @ProblemFactProperty
    public Map<Integer, HashSet<Integer>> getJobIdx2jobcloneIdxMap() {
        return jobIdx2jobcloneIdxMap;
    }

    public Schedule setJobIdx2jobcloneIdxMap(Map<Integer, HashSet<Integer>> jobIdx2jobcloneIdxMap) {
        this.jobIdx2jobcloneIdxMap = jobIdx2jobcloneIdxMap;
        return this;
    }

    @ProblemFactProperty
    public Map<ResourceTotalKey, HashSet<Integer>> getResourceTotalKeyallocationIdxMap() {
        return resourceTotalKeyallocationIdxMap;
    }

    public Schedule setResourceTotalKeyallocationIdxMap(Map<ResourceTotalKey, HashSet<Integer>> resourceTotalKeyallocationIdxMap) {
        this.resourceTotalKeyallocationIdxMap = resourceTotalKeyallocationIdxMap;
        return this;
    }

    @ProblemFactProperty
    public Map<TimelineEntry, HashSet<Integer>> getTimelineEntry2AllocationIdxMap() {
        return timelineEntry2AllocationIdxMap;
    }

    public Schedule setTimelineEntry2AllocationIdxMap(Map<TimelineEntry, HashSet<Integer>> timelineEntry2AllocationIdxMap) {
        this.timelineEntry2AllocationIdxMap = timelineEntry2AllocationIdxMap;
        return this;
    }

    @ProblemFactProperty
    public TimeHierarchyMap getOriginalTimeHierarchyMap() {
        return originalTimeHierarchyMap;
    }

    public Schedule setOriginalTimeHierarchyMap(TimeHierarchyMap originalTimeHierarchyMap) {
        this.originalTimeHierarchyMap = originalTimeHierarchyMap;
        return this;
    }

    @ProblemFactProperty
    public ValueEntryMap getOriginalValueEntryMap() {
        return originalValueEntryMap;
    }

    public Schedule setOriginalValueEntryMap(ValueEntryMap originalValueEntryMap) {
        this.originalValueEntryMap = originalValueEntryMap;
        return this;
    }


    public class Special implements Serializable {
        public HumanStateChange dummyHumamStateChange;
        public ProgressChange dummyProgressChange;
        public ResourceStateChange dummyResourceStateChange;
        public ChronoProperty dummyChronoProperty;
        public TimelineProperty dummyTimelineProperty;
        public String dummyLocation;
        public String dummyTime;
    }

    public static long unsolvedEntityCount(Schedule schedule) {
        ScoreDirector<Schedule> scheduleScoreDirector = getScoringScoreDirector();
        scheduleScoreDirector.setWorkingSolution(schedule);
        scheduleScoreDirector.calculateScore();
        return schedule.focusedAllocationSet.values().stream()
                .filter(x -> scheduleScoreDirector.getIndictmentMap().containsKey(x) &&
                        ((HardMediumSoftLongScore) scheduleScoreDirector.getIndictmentMap().get(x).getScore())
                                .getHardScore() < 0).count();
    }
}
