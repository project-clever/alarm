package com.alarm.alphabets;

public class HammerOutput implements TerminatingSymbol {
    protected final HammerResult result;

//    public final static HammerOutput FLIP = new HammerOutput(HammerResult.FLIP);
//    public final static HammerOutput OK = new HammerOutput(HammerResult.OK);
//    public final static HammerOutput TRR = new HammerOutput(HammerResult.TRR);
//    public final static HammerOutput ECC = new HammerOutput(HammerResult.ECC);

    public HammerOutput(HammerResult result) {
        this.result = result;
    }

    public HammerResult getResult() {
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        HammerOutput that = (HammerOutput) o;

        return result == that.result;
    }

    @Override
    public int hashCode() {
        return result != null ? result.hashCode() : 0;
    }

    @Override
    public boolean isTerminating() {
        return result == HammerResult.FLIP;
    }
}
