package bo.tc.tcplanner.domain.planningstructures;

import java.io.Serializable;
import java.util.Objects;

public class ResourceTotalValue implements Serializable {
    private int reserveAmt = 0;
    private int deficitAmt = 0;

    public ResourceTotalValue() {

    }


    public ResourceTotalValue(int reserveAmt, int deficitAmt) {
        this.reserveAmt = reserveAmt;
        this.deficitAmt = deficitAmt;
    }

    public ResourceTotalValue(ResourceTotalValue resourceTotalValue) {
        this.reserveAmt = resourceTotalValue.reserveAmt;
        this.deficitAmt = resourceTotalValue.deficitAmt;
    }

    public int getReserveAmt() {
        return reserveAmt;
    }

    public ResourceTotalValue setReserveAmt(int reserveAmt) {
        this.reserveAmt = reserveAmt;
        return this;
    }

    public int getDeficitAmt() {
        return deficitAmt;
    }


    public double getRealDeficitAmt() {
        return deficitAmt / 100d;
    }

    public double getRealReserveAmt() {
        return reserveAmt / 100d;
    }

    public ResourceTotalValue setDeficitAmt(int deficitAmt) {
        this.deficitAmt = deficitAmt;
        return this;
    }

    public ResourceTotalValue deficitLimit(int limit) {
        this.deficitAmt = Math.max(this.getReserveAmt(), limit);
        return this;
    }

    public ResourceTotalValue capacityLimit(int limit) {
        this.reserveAmt = Math.min(this.getReserveAmt(), limit);
        return this;
    }

    public boolean isEmpty() {
        return reserveAmt == 0 && deficitAmt == 0;
    }

    @Override
    public String toString() {
        return "+" + reserveAmt + "-" + -deficitAmt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ResourceTotalValue that = (ResourceTotalValue) o;
        return reserveAmt == that.reserveAmt &&
                deficitAmt == that.deficitAmt;
    }

    @Override
    public int hashCode() {
        return Objects.hash(reserveAmt, deficitAmt);
    }

    // Complex methods
    public ResourceTotalValue apply(ResourceTotalValue other) {
        int balance = reserveAmt + other.deficitAmt;
        setReserveAmt(other.reserveAmt + Math.max(balance, 0));
        setDeficitAmt(Math.min(balance, 0));
        return this;
    }

    public ResourceTotalValue getApplied(ResourceTotalValue other) {
        return new ResourceTotalValue(this).apply(other);
    }

    public ResourceTotalValue applyWithCap(ResourceTotalValue other, int cap) {
        int balance = reserveAmt + other.deficitAmt;
        setReserveAmt(Math.min(other.reserveAmt + Math.max(balance, 0), cap));
        setDeficitAmt(Math.min(balance, 0));
        return this;
    }

    public ResourceTotalValue getAppliedWithCap(ResourceTotalValue other, int cap) {
        return new ResourceTotalValue(this).applyWithCap(other, cap);
    }


    public ResourceTotalValue plus(ResourceTotalValue other) {
        setReserveAmt(reserveAmt + other.reserveAmt);
        setDeficitAmt(deficitAmt + other.deficitAmt);
        return this;
    }

    public ResourceTotalValue getPlused(ResourceTotalValue other) {
        return new ResourceTotalValue(this).plus(other);
    }

    public ResourceTotalValue scale(int originalProgressDelta, int progressDelta) {
        if(originalProgressDelta == 0) {
            setReserveAmt(0);
            setDeficitAmt(0);
        }else {
            setReserveAmt(reserveAmt * progressDelta / originalProgressDelta);
            setDeficitAmt(deficitAmt * progressDelta / originalProgressDelta);
        }
        return this;
    }

    public ResourceTotalValue getScaled(int originalProgressDelta, int progressDelta) {
        return new ResourceTotalValue(this).scale(originalProgressDelta, progressDelta);
    }
}
