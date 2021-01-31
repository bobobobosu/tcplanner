package bo.tc.tcplanner.domain.planningstructures;

import org.optaplanner.core.api.domain.solution.cloner.DeepPlanningClone;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@DeepPlanningClone
public class ResourceTotal implements Serializable {
    private final HashMap<ResourceTotalKey, ResourceTotalValue> resourceTotalMap;

    public ResourceTotal() {
        resourceTotalMap = new HashMap<>();
    }

    public ResourceTotal(ResourceTotal other) {
        resourceTotalMap = new HashMap<>();
        other.resourceTotalMap.forEach((k, v) -> resourceTotalMap.put(k, new ResourceTotalValue(v)));
    }

    public ResourceTotalValue getResource(ResourceTotalKey resourceTotalKey) {
        if (!resourceTotalMap.containsKey(resourceTotalKey)) return new ResourceTotalValue();
        return resourceTotalMap.get(resourceTotalKey);
    }

    public ResourceTotal addResourceWithProgress(
            ResourceTotalKey resourceTotalKey,
            ResourceTotalValue resourceTotalValue,
            int originalProgressDelta, int progressDelta) {
        add(resourceTotalKey, resourceTotalValue.getScaled(originalProgressDelta, progressDelta));
        return this;
    }

    public ResourceTotalValue getResourceWithProgress(ResourceTotalKey resourceTotalKey, int originalProgressDelta, int progressDelta) {
        return getResource(resourceTotalKey).getScaled(originalProgressDelta, progressDelta);
    }

    public ResourceTotal add(ResourceTotalKey resourceTotalKey, ResourceTotalValue resourceTotal) {
        if (!resourceTotalMap.containsKey(resourceTotalKey)) {
            resourceTotalMap.put(resourceTotalKey, resourceTotal);
        } else {
            resourceTotalMap.get(resourceTotalKey).plus(resourceTotal);
        }
        if (resourceTotalMap.get(resourceTotalKey).isEmpty()) resourceTotalMap.remove(resourceTotalKey);
        return this;
    }

    public ResourceTotal apply(ResourceTotalKey resourceTotalKey, ResourceTotalValue resourceTotal) {
        if (!resourceTotalMap.containsKey(resourceTotalKey)) {
            resourceTotalMap.put(resourceTotalKey, resourceTotal);
        } else {
            resourceTotalMap.get(resourceTotalKey).apply(resourceTotal);
        }
        if (resourceTotalMap.get(resourceTotalKey).isEmpty()) resourceTotalMap.remove(resourceTotalKey);
        return this;
    }

    public boolean containsKey(ResourceTotalKey resourceTotalKey) {
        return resourceTotalMap.containsKey(resourceTotalKey);
    }

    public void remove(ResourceTotalKey resourceTotalKey) {
        resourceTotalMap.remove(resourceTotalKey);
    }

    public Set<Map.Entry<ResourceTotalKey, ResourceTotalValue>> entrySet() {
        return resourceTotalMap.entrySet();
    }

//    public HashMap<ResourceTotalKey, ResourceTotalValue> getResourceTotalMap() {
//        return resourceTotalMap;
//    }
//
//    public ResourceTotal setResourceTotalMap(HashMap<ResourceTotalKey, ResourceTotalValue> resourceTotalMap) {
//        this.resourceTotalMap = resourceTotalMap;
//        return this;
//    }


    @Override
    public String toString() {
        return resourceTotalMap.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ResourceTotal that = (ResourceTotal) o;
        return Objects.equals(resourceTotalMap, that.resourceTotalMap);
    }

    @Override
    public int hashCode() {
        return Objects.hash(resourceTotalMap);
    }
}
