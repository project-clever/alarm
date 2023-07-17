package com.alarm.adapter.synthetic.components;

public class L implements Loc {
    int loc;

    public L(int loc) {
        this.loc = loc;
    }

    @Override
    public String toString() {
        return loc + "";
    }

    @Override
    public int compareTo(Loc l) {
        L s = (L) l;
        return loc - s.loc;
    }

    @Override
    public boolean equals(Object loc) {
        if (loc instanceof L) {
            L s = (L) loc;
            return this.loc == s.loc;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return (41 * (41 + this.loc) + this.loc);
    }

}