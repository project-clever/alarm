package com.alarm.adapter.synthetic.components;


public interface Loc extends Comparable<Loc> {
    public static String eval(Loc l) throws IllegalArgumentException {
        if (l instanceof L) {
            L s = (L) l;
            return "Loc" + s.loc;
        } else {
            throw new IllegalArgumentException("Invalid Loc!");
        }
    }

    public static int getValue(Loc l) throws IllegalArgumentException {
        if (l instanceof L) {
            L s = (L) l;
            return s.loc;
        } else {
            throw new IllegalArgumentException("Invalid Loc!");
        }
    }
}