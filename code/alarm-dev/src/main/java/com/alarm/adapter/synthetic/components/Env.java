package com.alarm.adapter.synthetic.components;

import java.util.HashMap;

/**
 * Environment Definition
 */
public class Env <V extends Loc<?>>{

    public final int REFRESH_INTERVAL = 6500; //Default value

    public int clocks;
    public CountRowVars<V> counter;

    public Env() {
        this.clocks = 0;
        this.counter = new CountRowVars<>();
    }

    public Env(int clocks, CountRowVars<V> counter) {
        this.clocks = clocks;
        this.counter = counter;
    }

    public void checkClocks() {
        this.clocks = this.clocks + 1;

        if (this.clocks >= REFRESH_INTERVAL) {
            this.clocks = 1;
            this.resetCounters();
        }
    }

    public void tickCounter(V loc, int v) {
        if (this.counter.map.containsKey(loc)) {
            int tmp = this.counter.map.get(loc);
            this.counter.map.put(loc, tmp + v);
        } else {
            this.counter.map.put(loc, v);
        }
    }

    public void resetCounter(V loc, int v) {
        this.counter.map.put(loc, v);
    }

    public void resetCounters() {
        counter.map.forEach(this::resetCounter);
    }

    /**
     * Row Counter Definition
     */
    public static class CountRowVars <V extends Loc<?>>{
        public HashMap<V, Integer> map;

        public CountRowVars() {
            this.map = new HashMap<>();
        }

        public CountRowVars(HashMap<V, Integer> map) {
            this.map = map;
        }
    }
    // END OF Row Counter Definition
}