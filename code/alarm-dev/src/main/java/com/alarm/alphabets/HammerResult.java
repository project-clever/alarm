package com.alarm.alphabets;

public class HammerResult {
    public static final HammerResult FLIP = new HammerResult("FLIP");
    public static final HammerResult OK = new HammerResult("OK");
    public static final HammerResult ECC = new HammerResult("ECC");
    public static final HammerResult TRR = new HammerResult("TRR");
    private final String label;
    private double probability;

    private HammerResult(String label) {
        this.label = label;
        this.probability = 1.0;
    }

    public HammerResult withProbability(double probability) {
        this.probability = probability;
        return this;
    }

    public String toString() {
        return label + ": " + probability;
    }

}
