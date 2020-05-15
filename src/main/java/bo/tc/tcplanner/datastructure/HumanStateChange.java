package bo.tc.tcplanner.datastructure;

import bo.tc.tcplanner.persistable.AbstractPersistable;

import java.util.Objects;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public class HumanStateChange extends AbstractPersistable {
    //location change
    String currentLocation;
    String movetoLocation;
    //time change
    double duration;
    String requirementTimerange;
    String adviceTimerange;

    public HumanStateChange() {
        super();
    }

    public HumanStateChange(HumanStateChange other) {
        super(other);
        this.setCurrentLocation(other.currentLocation);
        this.setMovetoLocation(other.movetoLocation);
        this.setDuration(other.duration);
        this.setRequirementTimerange(other.requirementTimerange);
        this.setAdviceTimerange(other.adviceTimerange);
    }

    @Override
    public HumanStateChange removeVolatile() {
        return this;
    }

    @Override
    public HumanStateChange removeEmpty() {
        return this;
    }

    @Override
    public boolean checkValid() {
        try {
            checkNotNull(currentLocation);
            checkNotNull(movetoLocation);
            checkArgument(duration >= 0);
            checkNotNull(requirementTimerange);
            checkNotNull(adviceTimerange);
            return true;
        } catch (IllegalArgumentException | NullPointerException ex) {
            throw new IllegalArgumentException(this.toString(), ex);
        }
    }

    public String getCurrentLocation() {
        return currentLocation;
    }

    public HumanStateChange setCurrentLocation(String currentLocation) {
        this.currentLocation = currentLocation;
        return this;
    }

    public String getMovetoLocation() {
        return movetoLocation;
    }

    public HumanStateChange setMovetoLocation(String movetoLocation) {
        this.movetoLocation = movetoLocation;
        return this;
    }

    public double getDuration() {
        return duration;
    }

    public HumanStateChange setDuration(double duration) {
        this.duration = duration;
        return this;
    }

    public String getRequirementTimerange() {
        return requirementTimerange;
    }

    public HumanStateChange setRequirementTimerange(String requirementTimerange) {
        this.requirementTimerange = requirementTimerange;
        return this;
    }

    public String getAdviceTimerange() {
        return adviceTimerange;
    }

    public HumanStateChange setAdviceTimerange(String adviceTimerange) {
        this.adviceTimerange = adviceTimerange;
        return this;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        HumanStateChange that = (HumanStateChange) o;
        return Double.compare(that.duration, duration) == 0 &&
                currentLocation.equals(that.currentLocation) &&
                movetoLocation.equals(that.movetoLocation) &&
                requirementTimerange.equals(that.requirementTimerange) &&
                adviceTimerange.equals(that.adviceTimerange);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), currentLocation, movetoLocation, duration, requirementTimerange, adviceTimerange);
    }
}
