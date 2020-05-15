package bo.tc.tcplanner.datastructure;

import bo.tc.tcplanner.PropertyConstants;
import bo.tc.tcplanner.persistable.AbstractPersistable;

import java.util.ArrayList;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public class ResourceStateChange extends AbstractPersistable {
    //resource change
    ResourceElementMap resourceChange;
    //resource status
    ResourceElementMap resourceStatus;
    //change mode
    String mode; // "delta" or "absolute"

    public ResourceStateChange() {
        super();
    }

    public ResourceStateChange(ResourceStateChange other) {
        super(other);
        this.setResourceChange(new ResourceElementMap(other.resourceChange));
        if (other.getResourceStatus() != null)
            this.setResourceChange(new ResourceElementMap(other.resourceStatus));
        this.setMode(other.mode);
    }

    @Override
    public boolean checkValid() {
        try {
            checkNotNull(resourceChange);
            checkNotNull(mode);
            checkArgument(PropertyConstants.ResourceStateChangeTypes.isValid(mode));
            checkArgument(resourceChange.checkValid());
            if (resourceStatus != null)
                checkArgument(resourceStatus.checkValid());
            return true;
        } catch (IllegalArgumentException | NullPointerException ex) {
            throw new IllegalArgumentException(this.toString(), ex);
        }
    }

    @Override
    public ResourceStateChange removeVolatile() {
        resourceChange.removeVolatile();
        if (resourceStatus != null) {
            resourceStatus.removeVolatile();
        }
        return this;
    }

    public ResourceElementMap getResourceChange() {
        return resourceChange;
    }

    public ResourceStateChange setResourceChange(ResourceElementMap resourceChange) {
        this.resourceChange = resourceChange;
        return this;
    }

    public String getMode() {
        return mode;
    }

    public ResourceStateChange setMode(String mode) {
        this.mode = mode;
        return this;
    }

    public ResourceElementMap getResourceStatus() {
        return resourceStatus;
    }

    public ResourceStateChange setResourceStatus(ResourceElementMap resourceStatus) {
        this.resourceStatus = resourceStatus;
        return this;
    }

    public ResourceStateChange removeEmpty() {
        resourceChange.removeEmpty();
        if (!(resourceStatus == null)) resourceStatus.removeEmpty();
        return this;
    }


    public ResourceStateChange addResourceElementToChange(String key, ResourceElement resourceElement) {
        if (resourceChange == null) resourceChange = new ResourceElementMap();
        if (!resourceChange.containsKey(key)) resourceChange.put(key, new ArrayList<>());
        resourceChange.get(key).add(resourceElement);
        return this;
    }

    public ResourceStateChange addResourceElementToStatus(String key, ResourceElement resourceElement) {
        if (resourceStatus == null) resourceStatus = new ResourceElementMap();
        if (!resourceStatus.containsKey(key)) resourceStatus.put(key, new ArrayList<>());
        resourceStatus.get(key).add(resourceElement);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        ResourceStateChange that = (ResourceStateChange) o;

        if (!resourceChange.equals(that.resourceChange)) return false;
        return mode.equals(that.mode);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + resourceChange.hashCode();
        result = 31 * result + mode.hashCode();
        return result;
    }
}
