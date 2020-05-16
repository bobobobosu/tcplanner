/*
 * Copyright 2010 Red Hat, Inc. and/or its affiliates.
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

import bo.tc.tcplanner.PropertyConstants;
import bo.tc.tcplanner.PropertyConstants.SolverPhase;
import bo.tc.tcplanner.datastructure.TimelineEntry;
import bo.tc.tcplanner.domain.comparators.AllocationDifficultyComparator;
import bo.tc.tcplanner.domain.comparators.ProgressDeltaStrengthComparator;
import bo.tc.tcplanner.domain.comparators.StartDateStrengthComparator;
import bo.tc.tcplanner.domain.filters.ReinitializeAllocationFilter;
import bo.tc.tcplanner.domain.listeners.FocusedAllocationSetUpdatingVariableListener;
import bo.tc.tcplanner.domain.listeners.PlanningDurationVariableUpdatingListener;
import bo.tc.tcplanner.domain.listeners.ResourceStateChangeVariableListener;
import bo.tc.tcplanner.domain.valueranges.FocusedEndDatesValueRange;
import bo.tc.tcplanner.domain.valueranges.TimeRestrictDatesValueRange;
import bo.tc.tcplanner.persistable.AbstractPersistable;
import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import org.jetbrains.annotations.NotNull;
import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.api.domain.entity.PlanningPin;
import org.optaplanner.core.api.domain.solution.cloner.DeepPlanningClone;
import org.optaplanner.core.api.domain.valuerange.CountableValueRange;
import org.optaplanner.core.api.domain.valuerange.ValueRangeFactory;
import org.optaplanner.core.api.domain.valuerange.ValueRangeProvider;
import org.optaplanner.core.api.domain.variable.CustomShadowVariable;
import org.optaplanner.core.api.domain.variable.PlanningVariable;
import org.optaplanner.core.api.domain.variable.PlanningVariableReference;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.Map.Entry;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

@PlanningEntity(difficultyComparatorClass = AllocationDifficultyComparator.class)
public class Allocation extends AbstractPersistable implements Comparable<Allocation> {
    // Belongs-to relationship
    private Schedule schedule;
    // Pre-Solving Properties
    private int index;
    private int weight;
    private boolean pinned;
    private boolean solved;
    private TimelineEntry timelineEntry;

    // Planning variables: changes during planning, between score calculations.
    private Integer progressdelta; // out of 100
    private ZonedDateTime planningStartDate;
    // Shadow variables
    private String previousStandstill;
    private ZonedDateTime nextStartDate;
    private Duration plannedDuration;
    private ResourceTotal resourceTotal;

    public Allocation() {

    }

    @Override
    public Allocation removeVolatile() {
        timelineEntry.removeVolatile();
        return this;
    }

    @Override
    public Allocation removeEmpty() {
        return this;
    }

    @Override
    public boolean checkValid() {
        try {
            checkNotNull(schedule);
            checkNotNull(timelineEntry);
            checkNotNull(planningStartDate);
            checkArgument(progressdelta >= 0);
            checkArgument(progressdelta <= 100);
            checkArgument(schedule.checkValid());
            checkArgument(timelineEntry.checkValid());
            return true;
        } catch (IllegalArgumentException | NullPointerException ex) {
            throw new IllegalArgumentException(this.toString(), ex);
        }
    }

    public AllocationKey getAllocationKey() {
        return new AllocationKey(this);
    }

    public int addToListSetIndex(List<Allocation> allocationList) {
        this.index = allocationList.size();
        allocationList.add(this);
        return this.index;
    }

    public int getIndex() {
        return index;
    }

    @Override
    public String toString() {
        return this.getWeight() + "-" + timelineEntry;
    }

    @Override
    public int compareTo(@NotNull Allocation o) {
        return Comparator.comparing(Allocation::getStartDate, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparingInt(x -> x.getTimelineEntry().getTimelineProperty().getRownum())
                .thenComparing(AbstractPersistable::getId).compare(this, o);
    }

    @Override
    public boolean isVolatileFlag() {
        return this.volatileFlag;
    }

    @Override
    public Allocation setVolatileFlag(boolean volatileFlag) {
        super.setVolatileFlag(volatileFlag);
        return this;
    }

    public TimelineEntry getTimelineEntry() {
        return timelineEntry;
    }

    public void setTimelineEntry(TimelineEntry timelineEntry) {
        this.timelineEntry = timelineEntry;
    }

    @PlanningVariable(valueRangeProviderRefs =
            {"continuousPlanningStartDateRange", "discretePlanningStartDateRange", "timerestrictPlanningStartDateRange"},
            strengthComparatorClass = StartDateStrengthComparator.class,
            reinitializeVariableEntityFilter = ReinitializeAllocationFilter.class, nullable = true)
    public ZonedDateTime getPlanningStartDate() {
        return planningStartDate;
    }

    public void setPlanningStartDate(ZonedDateTime planningStartDate) {
        this.planningStartDate = planningStartDate;
    }

    @PlanningVariable(valueRangeProviderRefs = {
            "progressdeltaRange", "originalprogressdeltaRange"}, strengthComparatorClass = ProgressDeltaStrengthComparator.class,
            reinitializeVariableEntityFilter = ReinitializeAllocationFilter.class)
    public Integer getProgressdelta() {
        return progressdelta;
    }

    public void setProgressdelta(Integer progressdelta) {
        this.progressdelta = progressdelta;
    }

    @CustomShadowVariable(variableListenerRef = @PlanningVariableReference(variableName = "focusedAllocationSet"))
    public String getPreviousStandstill() {
        return previousStandstill;
    }

    public void setPreviousStandstill(String previousStandstill) {
        this.previousStandstill = previousStandstill;
    }

    //update by FocusedAllocationSetUpdatingVariableListener
    @CustomShadowVariable(variableListenerRef = @PlanningVariableReference(variableName = "focusedAllocationSet"))
    public ZonedDateTime getNextStartDate() {
        return nextStartDate;
    }

    public void setNextStartDate(ZonedDateTime nextStartDate) {
        this.nextStartDate = nextStartDate;
    }

    //update by FocusedAllocationSetUpdatingVariableListener and ResourceStateChangeVariableListener
    @DeepPlanningClone
    @CustomShadowVariable(variableListenerClass = ResourceStateChangeVariableListener.class, sources = {
            @PlanningVariableReference(variableName = "progressdelta")})
    public ResourceTotal getResourceTotal() {
        return resourceTotal;
    }

    public void setResourceTotal(ResourceTotal resourceTotal) {
        this.resourceTotal = resourceTotal;
    }

    //update by PlanningDurationVariableUpdatingListener
    @CustomShadowVariable(variableListenerClass = PlanningDurationVariableUpdatingListener.class,
            sources = {@PlanningVariableReference(variableName = "progressdelta")})
    public Duration getPlannedDuration() {
        return plannedDuration;
    }

    public void setPlannedDuration(Duration plannedDuration) {
        this.plannedDuration = plannedDuration;
    }

    //update by FocusedAllocationSetUpdatingVariableListener
    @CustomShadowVariable(variableListenerClass = FocusedAllocationSetUpdatingVariableListener.class,
            sources = {
                    @PlanningVariableReference(variableName = "planningStartDate")})
    public TreeMap<AllocationKey, Allocation> getFocusedAllocationSet() {
        return schedule.focusedAllocationSet;
    }

    public void setFocusedAllocationSet(TreeMap<AllocationKey, Allocation> focusedAllocationSet) {
        schedule.focusedAllocationSet = focusedAllocationSet;
    }

    @PlanningPin
    public boolean isPinned() {
        return pinned;
    }

    // ************************************************************************
    // Scores
    // ************************************************************************

    public Allocation setPinned(boolean pinned) {
        this.pinned = pinned;
        return this;
    }

    public long getResourceElementMapExcessScore() {
        return getTimelineEntry().getDeltaResourceTotalKeys().stream()
                .mapToLong(x -> {
                    long alive = getResourceTotal().getResource(x).getReserveAmt();
                    long capacity = (long) (schedule.getValueEntryMap().get(x.getResourceName()).getCapacity() * 100);
                    return (alive > capacity) ? capacity - alive : 0;
                }).sum();
    }

    public long getResourceElementMapDeficitScore() {
        return getTimelineEntry().getDeltaResourceTotalKeys().stream().mapToLong(x ->
                Math.min(0,
                        getResourceTotal().getResource(x).getDeficitAmt())).sum();
    }

    public ResourceTotal getThisResourceTotal() {
        if (resourceTotal == null) return null;
        ResourceTotal thisResourceTotal = new ResourceTotal();
        return resourceTotal;
    }

    public Map<String, ResourceTotalValue> getThisResourceTotalString() {
        ResourceTotal thisResourceTotal = resourceTotal;
        Map<String, ResourceTotalValue> thisResourceTotalString = new HashMap<>();
        thisResourceTotal.entrySet().forEach(x -> {
            thisResourceTotalString.putIfAbsent(x.getKey().getResourceName(), new ResourceTotalValue());
            thisResourceTotalString.put(x.getKey().getResourceName(),
                    thisResourceTotalString.get(x.getKey().getResourceName()).plus(x.getValue()));
        });
        return thisResourceTotalString;
    }

    public double getRealResourceDeficit(ResourceTotalKey resourceTotalKey) {
        if (resourceTotal == null) {
            return 0;
        } else {
            return getResourceTotal().getResource(resourceTotalKey).getRealDeficitAmt();
        }
    }

    public double getRealResourceReserve(ResourceTotalKey resourceTotalKey) {
        if (resourceTotal == null) {
            return 0;
        } else {
            return getResourceTotal().getResource(resourceTotalKey).getRealReserveAmt();
        }
    }

    private long getTimeRestrictionScore(String restriction) {
        Range<ZonedDateTime> thisRange = Range.closed(getStartDate(), getEndDate());
        RangeSet<ZonedDateTime> restrictionRangeSet = schedule.getTimeEntryMap()
                .get(restriction);
        RangeSet<ZonedDateTime> overlapRangeSet = restrictionRangeSet.subRangeSet(thisRange);
        if (restrictionRangeSet.isEmpty()) return plannedDuration.toMinutes();
        if (overlapRangeSet.isEmpty()) {
            Range<ZonedDateTime> containing = restrictionRangeSet.complement().rangeContaining(thisRange.lowerEndpoint());
            return -Math.min(
                    containing.hasLowerBound() ? Duration.between(containing.lowerEndpoint(), thisRange.upperEndpoint()).toMinutes() : Integer.MAX_VALUE,
                    containing.hasUpperBound() ? Duration.between(thisRange.lowerEndpoint(), containing.upperEndpoint()).toMinutes() : Integer.MAX_VALUE);
        } else {
            return overlapRangeSet.asRanges().stream().mapToLong(
                    i -> Duration.between(i.lowerEndpoint(), i.upperEndpoint()).toMinutes()).sum() -
                    plannedDuration.toMinutes();

        }
    }

    private boolean getTimeRestrictionMatch(String restriction) {
        Range<ZonedDateTime> thisRange = Range.closed(getStartDate(), getEndDate());
        RangeSet<ZonedDateTime> restrictionRangeSet = schedule.getTimeEntryMap()
                .get(restriction);
        return restrictionRangeSet.encloses(thisRange);
    }

    public boolean getRequirementTimerangeMatch() {
        return getTimeRestrictionMatch(timelineEntry.getHumanStateChange().getRequirementTimerange());
    }

    public boolean getAdviceTimerangeMatch() {
        return getTimeRestrictionMatch(timelineEntry.getHumanStateChange().getAdviceTimerange());
    }

    public long getRequirementTimerangeScore() {
        return getTimeRestrictionScore(timelineEntry.getHumanStateChange().getRequirementTimerange());
    }

    public long getAdviceTimerangeScore() {
        return getTimeRestrictionScore(timelineEntry.getHumanStateChange().getAdviceTimerange());
    }

    // ************************************************************************
    // Ranges
    // ************************************************************************
    @ValueRangeProvider(id = "continuousPlanningStartDateRange")
    public CountableValueRange<ZonedDateTime> getPlanningStartDateRange() {
        // Built-in hard constraint for delay
        if (timelineEntry.getChronoProperty().getDraggable() == 0 &&
                timelineEntry.getChronoProperty().getZonedStartTime() != null)
            return ValueRangeFactory.createTemporalValueRange(
                    timelineEntry.getChronoProperty().getZonedStartTime(),
                    timelineEntry.getChronoProperty().getZonedStartTime().plusMinutes(1),
                    1, ChronoUnit.MINUTES);

        if (isFocused()) {
            long minutesOfEach;
            if (schedule.solverPhase == SolverPhase.FAST) {
                minutesOfEach = 10;
            } else if (schedule.solverPhase == SolverPhase.CH || schedule.solverPhase == SolverPhase.CH_REDUCE) {
                minutesOfEach = 20;
            } else {
                minutesOfEach = 1;
            }

            ZonedDateTime valueStartDate = Optional.ofNullable(
                    getFocusedAllocationSet().lowerEntry(this.getAllocationKey()))
                    .filter(x -> x.getValue().isFocused())
                    .map(x -> x.getValue().getEndDate())
                    .orElse(schedule.getProblemTimelineBlock().getZonedBlockStartTime());
            ZonedDateTime valueEndDate = Optional.ofNullable(nextStartDate)
                    .orElse(schedule.getProblemTimelineBlock().getZonedBlockEndTime());

            long numOfMinutes = Math.max(1,
                    Duration.between(valueStartDate, valueEndDate).dividedBy(Duration.ofMinutes(minutesOfEach)));
            return ValueRangeFactory.createTemporalValueRange(
                    valueStartDate, valueStartDate.plusMinutes(numOfMinutes * minutesOfEach),
                    minutesOfEach, ChronoUnit.MINUTES);
        } else {
            return ValueRangeFactory.createTemporalValueRange(
                    schedule.getProblemTimelineBlock().getZonedBlockStartTime(),
                    schedule.getProblemTimelineBlock().getZonedBlockStartTime().plusMinutes(1),
                    1, ChronoUnit.MINUTES);
        }
    }

    @ValueRangeProvider(id = "discretePlanningStartDateRange")
    public CountableValueRange<ZonedDateTime> getDiscretePlanningStartDateRange() {
        return new FocusedEndDatesValueRange(this);
    }

    @ValueRangeProvider(id = "timerestrictPlanningStartDateRange")
    public CountableValueRange<ZonedDateTime> getTimerestrictPlanningStartDateRange() {
        // original value is included here
        return new TimeRestrictDatesValueRange(this);
    }

    @ValueRangeProvider(id = "progressdeltaRange")
    public CountableValueRange<Integer> getProgressDeltaRange() {
        // Built-in hard constraint for progressdelta
        if (timelineEntry.getChronoProperty().getSplittable() == 0) {
            return ValueRangeFactory.createIntValueRange(100, 101, 1);
        }
        if (schedule.solverPhase == SolverPhase.CH) {
            return ValueRangeFactory.createIntValueRange(0, 200, 100);
        } else if (schedule.solverPhase == SolverPhase.FAST) {
            return ValueRangeFactory.createIntValueRange(0, 125, 25);
        } else {
            return ValueRangeFactory.createIntValueRange(0, 101, 1);
        }
    }

    @ValueRangeProvider(id = "originalprogressdeltaRange")
    public List<Integer> getOriginalProgressDeltaRange() {
        return Collections.singletonList(progressdelta);
    }


    // ************************************************************************
    // Complex methods
    // ************************************************************************
    public ZonedDateTime getStartDate() {
        // Built-in hard constraint for delay
        if (timelineEntry.getChronoProperty().getDraggable() == 0 &&
                timelineEntry.getChronoProperty().getZonedStartTime() != null) {
            return timelineEntry.getChronoProperty().getZonedStartTime();
        }
        return planningStartDate;
    }

    public ZonedDateTime getEndDate() {
        return getStartDate().plus(plannedDuration);
    }

    public double getProgressPercent() {
        // Built-in hard constraint for progressdelta
        return getTimelineEntry().getChronoProperty().getSplittable() == 0 ?
                getTimelineEntry().getProgressChange().getProgressDelta() :
                (double) getProgressdelta() / 100;
    }

    public Allocation getNextAllocation() {
        Entry<AllocationKey, Allocation> entry = getFocusedAllocationSet().higherEntry(this.getAllocationKey());
        return entry != null ? entry.getValue() : null;
    }

    public Allocation getPrevAllocation() {
        Entry<AllocationKey, Allocation> entry = getFocusedAllocationSet().lowerEntry(this.getAllocationKey());
        return entry != null ? entry.getValue() : null;
    }


    public Schedule getSchedule() {
        return schedule;
    }

    public Allocation setSchedule(Schedule schedule) {
        this.schedule = schedule;
        return this;
    }

    public Integer getWeight() {
        return weight;
    }

    public Allocation setWeight(Integer weight) {
        this.weight = weight;
        return this;
    }


    public boolean isOld() {
        return timelineEntry.getTimelineProperty().getPlanningWindowType().equals(PropertyConstants.PlanningWindowTypes.types.Published.name()) ||
                timelineEntry.getTimelineProperty().getPlanningWindowType().equals(PropertyConstants.PlanningWindowTypes.types.History.name());
    }

    public boolean isHistory() {
        return timelineEntry.getTimelineProperty().getPlanningWindowType().equals(PropertyConstants.PlanningWindowTypes.types.History.name());
    }

    public boolean isFocused() {
        return planningStartDate != null;
    }

    public boolean isSolved() {
        return solved;
    }

    public void setSolved(boolean solved) {
        this.solved = solved;
    }

}