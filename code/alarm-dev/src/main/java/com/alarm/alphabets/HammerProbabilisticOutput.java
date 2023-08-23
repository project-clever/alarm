package com.alarm.alphabets;

/**
 * Output symbol detailing the output probability
 */

public class HammerProbabilisticOutput extends HammerOutput {
    private double probability;

    public HammerProbabilisticOutput(HammerResult result) {
        super(result);
        this.probability = 1.0;
    }

    public HammerProbabilisticOutput withProbability(double probability) {
        this.probability = probability;
        return this;
    }

    public String toString() {
        return getResult() + ": " + probability;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        HammerProbabilisticOutput that = (HammerProbabilisticOutput) o;

        return Double.compare(probability, that.probability) == 0;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        long temp;
        temp = Double.doubleToLongBits(probability);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }


}
