package com.alarm.tool;

public class LearnerConfig {
    public int randomSeed = 46_346_293;
    public double resetProbability = 0.05;
    public Algorithms algorithm = Algorithms.LSTAR;
    public EqOracle eqORacle = EqOracle.RANDOM_WALK;

    public enum Algorithms { TTT, LSTAR }
    public enum EqOracle {RANDOM_WALK, WP_METHOD, RANDOM_WP_METHOD}
}
