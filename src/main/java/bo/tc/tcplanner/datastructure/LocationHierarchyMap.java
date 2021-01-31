package bo.tc.tcplanner.datastructure;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;

import static com.google.common.base.Preconditions.checkArgument;

public class LocationHierarchyMap extends HashMap<String, HashSet<String>> {
    public boolean checkValid() {
        try {
            checkArgument(this.entrySet().stream().allMatch(x -> x.getValue().stream().allMatch(Objects::nonNull)));
            return true;
        } catch (IllegalArgumentException | NullPointerException ex) {
            throw new IllegalArgumentException(this.toString(), ex);
        }
    }

    public LocationHierarchyMap() {
    }

    public LocationHierarchyMap(LocationHierarchyMap locationHierarchyMap) {
        locationHierarchyMap.forEach((k, v) -> this.put(k, new HashSet<>(v)));
    }
}
