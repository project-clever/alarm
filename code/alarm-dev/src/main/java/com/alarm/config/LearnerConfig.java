package com.alarm.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Arrays;

public class LearnerConfig {
    public long randomSeed = 46_346_293;
    public double resetProbability = 0.05;
    public int eqOracleMaxSteps = 100;
    public int runsPerQuery = 10;
    public double samplingThreshold = 0.6;
    public Algorithms algorithm = Algorithms.LSTAR;
    public EqOracle eqOracle = EqOracle.RANDOM_WALK;
    public int minRow = 0;
    public int maxRow = 10;
    public List<Integer> readCounts = new ArrayList<Integer>();
    public List<Integer> bitFlips = new ArrayList<Integer>();
    public boolean visualiseAllHypotheses;
    public boolean visualiseLearntModel;
    public String dotOutputPath;

    public enum Algorithms { TTT, LSTAR }
    public enum EqOracle {RANDOM_WALK, WP_METHOD, RANDOM_WP_METHOD}
}
