package bo.tc.tcplanner.domain.score;

import bo.tc.tcplanner.PropertyConstants;
import bo.tc.tcplanner.domain.planningstructures.Allocation;
import org.optaplanner.core.api.score.buildin.hardmediumsoftlong.HardMediumSoftLongScore;
import org.optaplanner.core.api.score.stream.Constraint;
import org.optaplanner.core.api.score.stream.ConstraintFactory;
import org.optaplanner.core.api.score.stream.ConstraintProvider;

import java.time.Duration;
import java.util.function.Function;

import static bo.tc.tcplanner.app.SolverCore.DroolsTools.locationRestrictionCheck;
import static org.optaplanner.core.api.score.stream.Joiners.equal;

public class ScheduleConstraintProvider implements ConstraintProvider {
    @Override
    public Constraint[] defineConstraints(ConstraintFactory constraintFactory) {
        return new Constraint[]{
                checkDependencyId(constraintFactory),
                checkTimeOverlapping(constraintFactory),
                checkDeadline(constraintFactory),
                checkAliveline(constraintFactory),
                checkScheduleAfter(constraintFactory),
                checkPreviousStandstill(constraintFactory),
                checkRequirementsDeficit(constraintFactory),
                checkCapacityRequirements(constraintFactory),
                timeRequirement(constraintFactory),
                dummyJob(constraintFactory),
                checkExcessResource(constraintFactory),
                timeAdvisory(constraintFactory),
                changedJob(constraintFactory),
                punishFragmentation(constraintFactory),
                laterTheBetter(constraintFactory),
                earlierTheBetter(constraintFactory)
        };
    }

    // ************************************************************************
    // Hard constraints
    // ************************************************************************
    private Constraint checkDependencyId(ConstraintFactory factory) {
        return factory.from(Allocation.class)
                .filter(allocation -> allocation.isFocused())
                .join(Integer.class).filter(((allocation, integer) ->
                        allocation.getTimelineEntry().getTimelineProperty().getDependencyIdList().contains(integer)))
                .join(factory.from(Allocation.class).filter(Allocation::isFocused),
                        equal(((a, b) -> b),
                                (allocation -> allocation.getTimelineEntry().getTimelineProperty().getTimelineid())))

                .penalizeLong("checkDependencyId", HardMediumSoftLongScore.ONE_HARD,
                        (a, b, c) -> allocationWeight_raw(a) * 100);

    }

    private Constraint checkTimeOverlapping(ConstraintFactory factory) {
        return factory.from(Allocation.class)
                .filter(allocation -> allocation.isFocused() &&
                        allocation.getNextStartDate() != null &&
                        allocation.getEndDate().isAfter(allocation.getNextStartDate()))
                .penalizeLong("checkTimeOverlapping", HardMediumSoftLongScore.ONE_HARD,
                        (a -> (long) (allocationWeight_raw(a) *
                                ((double) Duration.between(
                                        a.getNextStartDate(), a.getEndDate()).toMinutes()))));
    }

    private Constraint checkDeadline(ConstraintFactory factory) {
        return factory.from(Allocation.class)
                .filter(allocation -> allocation.isFocused() &&
                        allocation.getTimelineEntry().getChronoProperty().getZonedDeadline().isBefore(allocation.getEndDate()))
                .penalizeLong("checkDeadline", HardMediumSoftLongScore.ONE_HARD,
                        (a -> (long) (allocationWeight_raw(a) *
                                ((double) Duration.between(
                                        a.getTimelineEntry().getChronoProperty().getZonedDeadline(),
                                        a.getEndDate()).toMinutes()))));

    }

    private Constraint checkAliveline(ConstraintFactory factory) {
        return factory.from(Allocation.class)
                .filter(allocation -> allocation.isFocused() &&
                        allocation.getTimelineEntry().getChronoProperty().getZonedAliveline().isAfter(allocation.getStartDate()))
                .penalizeLong("checkAliveline", HardMediumSoftLongScore.ONE_HARD,
                        (a -> (long) (allocationWeight_raw(a) *
                                ((double) Duration.between(
                                        a.getStartDate(),
                                        a.getTimelineEntry().getChronoProperty().getZonedAliveline()
                                ).toMinutes()))));
    }

    private Constraint checkScheduleAfter(ConstraintFactory factory) {
        return factory.from(Allocation.class)
                .filter(allocation -> allocation.isFocused() &&
                        allocation.getTimelineEntry().getTimelineProperty().getPlanningWindowType().equals(
                                PropertyConstants.PlanningWindowTypes.types.Draft.name()) &&
                        allocation.getStartDate().isBefore(
                                allocation.getSchedule().getProblemTimelineBlock().getZonedBlockScheduleAfter()))
                .penalizeLong("checkScheduleAfter", HardMediumSoftLongScore.ONE_HARD,
                        (a -> (long) (allocationWeight_raw(a) *
                                ((double) Duration.between(
                                        a.getStartDate(),
                                        a.getSchedule().getProblemTimelineBlock().getZonedBlockScheduleAfter()
                                ).toMinutes() / 10))));
    }

    private Constraint checkPreviousStandstill(ConstraintFactory factory) {
        return factory.from(Allocation.class)
                .filter(allocation -> allocation.isFocused() && allocation.getWeight() > 1 &&
                        !locationRestrictionCheck(
                                allocation.getSchedule().getOriginalLocationHierarchyMap(),
                                allocation.getPreviousStandstill(),
                                allocation.getTimelineEntry().getHumanStateChange().getCurrentLocation()))
                .penalizeLong("checkPreviousStandstill", HardMediumSoftLongScore.ONE_HARD,
                        a -> allocationWeight_raw(a) * 100L);
    }

    private Constraint checkRequirementsDeficit(ConstraintFactory factory) {
        return factory.from(Allocation.class)
                .filter(allocation -> allocation.isFocused())
                .penalizeLong("checkRequirementsDeficit", HardMediumSoftLongScore.ONE_HARD,
                        a -> allocationWeight_resource(a, -a.getResourceElementMapDeficitScore()));
    }

    private Constraint checkCapacityRequirements(ConstraintFactory factory) {
        return factory.from(Allocation.class)
                .filter(allocation -> allocation.isFocused())
                .penalizeLong("checkCapacityRequirements", HardMediumSoftLongScore.ONE_HARD,
                        a -> allocationWeight_resource(a, -a.getResourceElementMapExcessScore()));
    }

    private Constraint timeRequirement(ConstraintFactory factory) {
        return factory.from(Allocation.class)
                .filter(allocation -> allocation.isFocused() &&
                        allocation.getRequirementTimerangeMatch())
                .join(Long.class, equal(Allocation::getRequirementTimerangeScore, Function.identity()))
                .filter((allocation, b) -> b < 5)
                .penalizeLong("timeRequirement", HardMediumSoftLongScore.ONE_HARD,
                        ((a, aLong) -> allocationWeight_raw(a) *
                                aLong));
    }

    // ************************************************************************
    // Medium constraints
    // ************************************************************************

    private Constraint dummyJob(ConstraintFactory factory) {
        return factory.from(Allocation.class)
                .filter(allocation -> allocation.isFocused())
                .penalizeLong("dummyJob", HardMediumSoftLongScore.ONE_MEDIUM,
                        a -> 100);
    }

    private Constraint checkExcessResource(ConstraintFactory factory) {
        return factory.from(Allocation.class)
                .filter(allocation -> allocation.isFocused())
                .join(Long.class, equal(Allocation::getResourceElementMapExcessScore, Function.identity()))
                .filter((allocation, b) -> b < 0)
                .penalizeLong("checkExcessResource", HardMediumSoftLongScore.ONE_MEDIUM,
                        ((a, aLong) -> allocationWeight_resource(a, -aLong)));
    }

    private Constraint timeAdvisory(ConstraintFactory factory) {
        return factory.from(Allocation.class)
                .filter(allocation -> allocation.isFocused() &&
                        allocation.getAdviceTimerangeMatch())
                .join(Long.class, equal(Allocation::getAdviceTimerangeScore, Function.identity()))
                .filter((allocation, b) -> b < -5)
                .penalizeLong("timeAdvisory", HardMediumSoftLongScore.ONE_MEDIUM,
                        ((a, aLong) -> allocationWeight_raw(a) *
                                -aLong));
    }

    // ############################################################################
    // Soft constraints
    // ############################################################################

    private Constraint changedJob(ConstraintFactory factory) {
        return factory.from(Allocation.class)
                .filter(allocation -> allocation.isFocused() &&
                        allocation.getTimelineEntry().getTimelineProperty().getPlanningWindowType().equals(
                                PropertyConstants.PlanningWindowTypes.types.Draft.name()
                        ))
                .penalizeLong("changedJob", HardMediumSoftLongScore.ONE_SOFT,
                        a -> 100);
    }

    private Constraint punishFragmentation(ConstraintFactory factory) {
        return factory.from(Allocation.class)
                .filter(allocation -> allocation.isFocused() &&
                        allocation.getProgressdelta() < 100)
                .penalizeLong("punishFragmentation", HardMediumSoftLongScore.ONE_SOFT,
                        a -> 100 - a.getProgressdelta());
    }

    private Constraint laterTheBetter(ConstraintFactory factory) {
        return factory.from(Allocation.class)
                .filter(allocation -> allocation.isFocused() &&
                        allocation.getTimelineEntry().getChronoProperty().getGravity() == 1)
                .rewardLong("laterTheBetter", HardMediumSoftLongScore.ONE_SOFT,
                        (a -> allocationWeight_raw(a) *
                                (Duration.between(a.getSchedule().getSourceAllocation().getStartDate(),
                                        a.getStartDate())).toMinutes()));
    }

    private Constraint earlierTheBetter(ConstraintFactory factory) {
        return factory.from(Allocation.class)
                .filter(allocation -> allocation.isFocused() &&
                        allocation.getTimelineEntry().getChronoProperty().getGravity() == -1)
                .penalizeLong("earlierTheBetter", HardMediumSoftLongScore.ONE_SOFT,
                        (a -> allocationWeight_raw(a) *
                                (Duration.between(a.getSchedule().getSourceAllocation().getStartDate(),
                                        a.getStartDate())).toMinutes()));
    }

    private long allocationWeight_resource(Allocation allocation, long score) {

        return score +
                (score > 0 ? allocation.getWeight() : 0) +
                (score > 0 ? allocation.getTimelineEntry().getChronoProperty().getPriority() : 0);
//        return allocation.getTimelineEntry().getChronoProperty().getPriority() + allocation.getWeight();
    }

    private int allocationWeight_raw(Allocation allocation) {
        return allocation.getTimelineEntry().getChronoProperty().getPriority();
    }
}
