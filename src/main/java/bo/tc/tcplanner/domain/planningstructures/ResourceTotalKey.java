package bo.tc.tcplanner.domain.planningstructures;

import bo.tc.tcplanner.datastructure.LocationHierarchyMap;

import java.io.Serializable;
import java.util.Objects;
import java.util.Set;

import static bo.tc.tcplanner.app.SolverCore.DroolsTools.locationRestrictionCheck;

public class ResourceTotalKey implements Serializable {
    private String resourceName;
    private String location;
    private Set<Integer> timelineId;

    public String getResourceName() {
        return resourceName;
    }

    public ResourceTotalKey setResourceName(String resourceName) {
        this.resourceName = resourceName;
        return this;
    }

    public String getLocation() {
        return location;
    }

    public ResourceTotalKey setLocation(String location) {
        this.location = location;
        return this;
    }

    public Set<Integer> getTimelineId() {
        return timelineId;
    }

    public ResourceTotalKey setTimelineId(Set<Integer> timelineId) {
        this.timelineId = timelineId;
        return this;
    }

    public boolean isAlso(ResourceTotalKey other, LocationHierarchyMap locationHierarchyMap) {
        return resourceName.equals(other.resourceName) &&
                other.timelineId.containsAll(timelineId) &&
                locationRestrictionCheck(locationHierarchyMap, location, other.location);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ResourceTotalKey that = (ResourceTotalKey) o;
        return Objects.equals(resourceName, that.resourceName) &&
                Objects.equals(location, that.location) &&
                Objects.equals(timelineId, that.timelineId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(resourceName, location, timelineId);
    }

    @Override
    public String toString() {
        return "ResourceTotalKey{" +
                "resourceName:" + resourceName +
                ", location:" + location+
                ", timelineId:" + timelineId +
                '}';
    }
}
