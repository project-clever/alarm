package com.alarm.tool;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Arrays;

public class LearnerConfig {
    public int randomSeed = 46_346_293;
    public double resetProbability = 0.05;
    public Algorithms algorithm = Algorithms.LSTAR;
    public EqOracle eqORacle = EqOracle.RANDOM_WALK;
    public int minRow = 0;
    public int maxRow = 10;
    public List<Integer> readCounts = new ArrayList<Integer>();
    public List<Integer> bitFlips = new ArrayList<Integer>();
    public List<String>  outputAlphabet = new ArrayList<String>();

    public enum Algorithms { TTT, LSTAR }
    public enum EqOracle {RANDOM_WALK, WP_METHOD, RANDOM_WP_METHOD}
}
