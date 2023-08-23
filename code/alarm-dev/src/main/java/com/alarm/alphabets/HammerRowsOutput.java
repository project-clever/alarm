package com.alarm.alphabets;

import java.util.HashSet;
import java.util.Set;

/**
 * Output symbol detailing in which rows flips occurred
 */
public class HammerRowsOutput extends HammerOutput {
    private Set<Integer> rowsWithFlips;

    public HammerRowsOutput(HammerResult result) {
        super(result);
        rowsWithFlips = new HashSet<>();
    }

    public HammerRowsOutput withFlipsLocations(Set<Integer> rows) {
        // TODO: Deep copy??
        this.rowsWithFlips = rows;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        HammerRowsOutput that = (HammerRowsOutput) o;

        return rowsWithFlips.equals(that.rowsWithFlips);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + rowsWithFlips.hashCode();
        return result;
    }

    @Override
    public String toString() {
        String s = getResult().toString();
        if (getResult() == HammerResult.FLIP)
            s += ":" + rowsWithFlips.toString();

        return s;
    }
}
