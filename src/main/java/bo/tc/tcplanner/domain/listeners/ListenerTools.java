package bo.tc.tcplanner.domain.listeners;

import bo.tc.tcplanner.PropertyConstants;
import bo.tc.tcplanner.datastructure.LocationHierarchyMap;
import bo.tc.tcplanner.datastructure.ResourceElement;
import bo.tc.tcplanner.datastructure.ResourceElementMap;
import bo.tc.tcplanner.domain.planningstructures.*;
import org.optaplanner.core.api.score.director.ScoreDirector;

import java.time.Duration;
import java.util.*;

public class ListenerTools {
    public static boolean updatePlanningDuration(ScoreDirector scoreDirector, Allocation allocation) {
        boolean changed;
        int progressDelta100 = allocation.getTimelineEntry().getProgressChange().getProgressDelta100();
        double multiplier = progressDelta100 > 0 ? (double) allocation.getConstrainedProgressDelta() / progressDelta100 : 0;
        Duration duration = Duration.ofSeconds(
                (long) (allocation.getTimelineEntry().getHumanStateChange().getDuration() * 60 * multiplier));
        changed = allocation.getPlannedDuration() == null || allocation.getPlannedDuration().equals(duration);
        if (scoreDirector != null) scoreDirector.beforeVariableChanged(allocation, "plannedDuration");
        allocation.setPlannedDuration(duration);
        if (scoreDirector != null) scoreDirector.afterVariableChanged(allocation, "plannedDuration");
        return changed;
    }

    public static void updateNextStartDate(ScoreDirector scoreDirector, Allocation allocation, Allocation nextAllocation) {
        if (scoreDirector != null) scoreDirector.beforeVariableChanged(allocation, "nextStartDate");
        allocation.setNextStartDate(nextAllocation != null ? nextAllocation.getConstrainedStartDate() : null);
        if (scoreDirector != null) scoreDirector.afterVariableChanged(allocation, "nextStartDate");
    }

    public static boolean updateAllocationPreviousStandstill(ScoreDirector scoreDirector, Allocation prevAllocation, Allocation allocation) {

        boolean changed;
        if (allocation.isFocused()) {
            if (prevAllocation != null) {
                LocationHierarchyMap locationHierarchyMap = allocation.getSchedule().getOriginalLocationHierarchyMap();
                String previousPreviousStandStill = prevAllocation.getPreviousStandstill();
                String currentLocation = prevAllocation.getTimelineEntry().getHumanStateChange().getCurrentLocation();
                String movetoLocation = prevAllocation.getTimelineEntry().getHumanStateChange().getMovetoLocation();

                String finalLocation = movetoLocation;
                if (locationHierarchyMap.containsKey(currentLocation) &&
                        locationHierarchyMap.get(currentLocation).contains(finalLocation))
                    finalLocation = currentLocation;
                if (locationHierarchyMap.containsKey(previousPreviousStandStill) &&
                        locationHierarchyMap.get(previousPreviousStandStill).contains(finalLocation))
                    finalLocation = previousPreviousStandStill;

                changed = allocation.getPreviousStandstill() == null ||
                        !allocation.getPreviousStandstill().equals(finalLocation);

                if (scoreDirector != null) scoreDirector.beforeVariableChanged(allocation, "previousStandstill");
                allocation.setPreviousStandstill(prevAllocation.equals(allocation.getSchedule().getSourceAllocation()) ?
                        allocation.getTimelineEntry().getHumanStateChange().getCurrentLocation() :
                        finalLocation);
                if (scoreDirector != null) scoreDirector.afterVariableChanged(allocation, "previousStandstill");
                return changed;
            } else {
                changed = allocation.getPreviousStandstill() == null ||
                        !allocation.getPreviousStandstill().equals(allocation.getSchedule().special.dummyLocation);
                if (scoreDirector != null) scoreDirector.beforeVariableChanged(allocation, "previousStandstill");
                allocation.setPreviousStandstill(allocation.getSchedule().special.dummyLocation);
                if (scoreDirector != null) scoreDirector.afterVariableChanged(allocation, "previousStandstill");
                return changed;
            }
        } else {
            if (scoreDirector != null) scoreDirector.beforeVariableChanged(allocation, "previousStandstill");
            allocation.setPreviousStandstill(null);
            if (scoreDirector != null) scoreDirector.afterVariableChanged(allocation, "previousStandstill");
            return true;
        }
    }

    public static void applyResourceTotalMap(ScoreDirector scoreDirector,
                                             Allocation allocation,
                                             Allocation startingAllocation) {
        // collect affected allocations
        Map<ResourceTotalKey, TreeMap<AllocationKey, Allocation>> filteredResourceAllocations = new HashMap<>();
        allocation.getTimelineEntry().getDeltaResourceTotalKeys().forEach(x -> filteredResourceAllocations.put(x, new TreeMap<>()));
        allocation.getFocusedAllocationSet().forEach((k, v) ->
                allocation.getTimelineEntry().getDeltaResourceTotalKeys().forEach(x -> {
                    if (allocation.getSchedule().getResourceTotalKeyallocationIdxMap().get(x).contains(v.getIndex())) {
                        filteredResourceAllocations.get(x).put(k, v);
                    }
                }));

        // set new values for this changed allocation
        if (!allocation.isFocused()) {
            if (scoreDirector != null) scoreDirector.beforeVariableChanged(allocation, "resourceTotal");
            allocation.setResourceTotal(null);
            if (scoreDirector != null) scoreDirector.afterVariableChanged(allocation, "resourceTotal");
        } else {
            ResourceTotal resourceTotal = new ResourceTotal();
            allocation.getTimelineEntry().getDeltaResourceTotalKeys().forEach(x -> {
                // get previous values
                Map.Entry<AllocationKey, Allocation> prevEntry =
                        filteredResourceAllocations.get(x).lowerEntry(allocation.getAllocationKey());
                ResourceTotalValue resourceTotalValue = prevEntry == null ?
                        new ResourceTotalValue() : new ResourceTotalValue(prevEntry.getValue().getResourceTotal().getResource(x));
                resourceTotal.add(
                        x,
                        resourceTotalValue
                                .applyWithCap(
                                        allocation.getTimelineEntry().getDeltaResourceTotal_hundredpercent()
                                                .getResourceWithProgress(x, 100,
                                                        (allocation.getConstrainedProgressDelta())),// use getProgressPercent for splittable hard constraint
                                        (int) Math.round(allocation.getSchedule().getValueEntryMap()
                                                .get(x.getResourceName()).getCapacity() * 100)));
            });
            if (scoreDirector != null) scoreDirector.beforeVariableChanged(allocation, "resourceTotal");
            allocation.setResourceTotal(resourceTotal);
            if (scoreDirector != null) scoreDirector.afterVariableChanged(allocation, "resourceTotal");
        }

        // apply to affected allocations
        if (startingAllocation != null) {
            Map<Allocation, ResourceTotal> resultResourceTotals = new HashMap<>();
            allocation.getTimelineEntry().getDeltaResourceTotalKeys().forEach(k -> {
                // get previous values and apply (so it be for current allocation)
                Map.Entry<AllocationKey, Allocation> prevEntry =
                        filteredResourceAllocations.get(k).lowerEntry(startingAllocation.getAllocationKey());
                Allocation prevAlloction = prevEntry == null ? null : prevEntry.getValue();
                ResourceTotalValue resourceTotalValue = prevEntry == null ?
                        new ResourceTotalValue() : new ResourceTotalValue(prevAlloction.getResourceTotal().getResource(k));
                for (Allocation dirtyAllocation :
                        filteredResourceAllocations.get(k).tailMap(startingAllocation.getAllocationKey()).values()) {
                    resourceTotalValue.applyWithCap(dirtyAllocation.getTimelineEntry().getDeltaResourceTotal_hundredpercent()
                                    .getResourceWithProgress(
                                            k, 100,
                                            (dirtyAllocation.getConstrainedProgressDelta())) // use getProgressPercent for splittable hard constraint
                            , (int) Math.round(allocation.getSchedule().getValueEntryMap()
                                    .get(k.getResourceName()).getCapacity() * 100));

                    if (!resultResourceTotals.containsKey(dirtyAllocation)) {
                        resultResourceTotals.put(dirtyAllocation, new ResourceTotal(dirtyAllocation.getResourceTotal()));
                    }
                    resultResourceTotals.get(dirtyAllocation).remove(k); // remove dirty value
                    resultResourceTotals.get(dirtyAllocation).add(k, new ResourceTotalValue(resourceTotalValue));
                    prevAlloction = dirtyAllocation;
                }
            });
            resultResourceTotals.forEach((k, v) -> {
                if (scoreDirector != null) scoreDirector.beforeVariableChanged(k, "resourceTotal");
                k.setResourceTotal(v);
                if (scoreDirector != null) scoreDirector.afterVariableChanged(k, "resourceTotal");
            });
        }
    }

    private static void addResourceElement(ResourceElementMap resourceElementMap, String key, ResourceElement resourceElement) {
        if (!resourceElementMap.containsKey(key)) resourceElementMap.put(key, new ArrayList<>());
        resourceElementMap.get(key).add(resourceElement);
    }

    private static int nextPositive(List<ResourceElement> resourceElementList, int startIdx) {
        while ((startIdx = startIdx + 1) < resourceElementList.size()) {
            if (resourceElementList.get(startIdx).getAmt() > 0) break;
        }
        return startIdx < resourceElementList.size() ? startIdx : -1;
    }

    private static int nextNegative(List<ResourceElement> resourceElementList, int startIdx) {
        while ((startIdx = startIdx + 1) < resourceElementList.size()) {
            if (resourceElementList.get(startIdx).getAmt() < 0) break;
        }
        return startIdx < resourceElementList.size() ? startIdx : -1;
    }

    static class ResourceChangeChain {
        List<ResourceElementMap> resultChain = new ArrayList<>();
        Map<Integer, ResourceElementMap> pushpullMap = new HashMap<>();
        Map<ResourceElement, Integer> resourceSourceMap = new IdentityHashMap<>();

        ResourceChangeChain(List<Allocation> focusedAllocationList, Set<String> dirty) {
            // initialization
            for (int i = 0; i < focusedAllocationList.size(); i++) {
                pushpullMap.put(i, new ResourceElementMap());
                resultChain.add(new ResourceElementMap());
            }

            for (int i = 0; i < focusedAllocationList.size(); i++) {
                Allocation allocation = focusedAllocationList.get(i);
                int finalI = i;
                dirty.forEach(k -> {
                    ResourceElementMap resourceChange = allocation.getTimelineEntry().getResourceStateChange().getResourceChange();
                    if (!resourceChange.containsKey(k))
                        return;
                    resourceChange.get(k).forEach(x -> {
                        // create resourceElement
                        double realAmt = x.getAmt() *
                                ((double) allocation.getConstrainedProgressDelta() /
                                        allocation.getTimelineEntry().getProgressChange().getProgressDelta100());
                        ResourceElement resourceElement = new ResourceElement(x)
                                .setAmt(!Double.isNaN(realAmt) ? realAmt : 0)
                                .setDependedTimelineIdList(new HashSet<>())
                                .setSuppliedTimelineIdList(new HashSet<>())
                                .setPriorityTimelineIdList(
                                        allocation.getTimelineEntry().getTimelineProperty().getDependencyIdList());

                        // populate resource source Map
                        resourceSourceMap.put(resourceElement, finalI);

                        // populate resultChain and pushpullMap
                        List<Integer> applicableList = new ArrayList<>();
                        for (int j = 0; j < focusedAllocationList.size(); j++) {
                            Allocation allocation1 = focusedAllocationList.get(j);
                            if (!focusedAllocationList.get(j).getTimelineEntry().getResourceStateChange()
                                    .getResourceChange().containsKey(k)) continue;
                            boolean isAbs = allocation1.getTimelineEntry().getResourceStateChange().getMode()
                                    .equals(PropertyConstants.ResourceStateChangeTypes.types.absolute.name());
                            if (j <= finalI && isAbs) applicableList.clear();
                            if (j > finalI && isAbs) break;
                            applicableList.add(j);
                        }
                        for (int j : applicableList) {
                            if (j <= finalI && x.getAmt() <= 0)
                                addResourceElement(pushpullMap.get(j), k, resourceElement);
                            if (j == finalI && x.getAmt() <= 0)
                                addResourceElement(resultChain.get(j), k, resourceElement);
                            if (j >= finalI && x.getAmt() > 0) {
                                addResourceElement(resultChain.get(j), k, resourceElement);
                                addResourceElement(pushpullMap.get(j), k, resourceElement);
                            }
                        }
                    });
                });
            }
        }
    }
}
