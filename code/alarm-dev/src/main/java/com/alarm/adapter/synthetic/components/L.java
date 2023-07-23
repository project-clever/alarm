package com.alarm.adapter.synthetic.components;

import java.util.Objects;

/**
 * A concrete class representing a specific type of Location, where the location value is of type Integer.
 * Extends the abstract class Loc<Integer>.
 */
public class L extends Loc<Integer> {

    public L(int loc) {
        super(loc);
    }

    @Override
    public String toString() {
        return this.getValue().toString();
    }


    @Override
    public boolean equals(Object loc) {
        if (loc instanceof L) {
            L s = (L) loc;
            return Objects.equals(this.getValue(), s.getValue());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return (41 * (41 + this.getValue()) + getValue());
    }

    /**
     * {@inheritDoc}
     * @return The integer distance (absolute) difference between the location values.
     */
    @Override
    public int distance(Loc<Integer> otherLocation) {
        if (otherLocation == null)
            throw new IllegalArgumentException("Parameter give to distance() is null!");
        return Math.abs(this.getValue() - otherLocation.getValue());
    }
}