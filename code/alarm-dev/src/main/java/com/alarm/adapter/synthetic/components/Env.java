package com.alarm.adapter.synthetic.components;

import com.alarm.adapter.synthetic.MemoryModel;

import java.util.HashMap;

/**
 * Environment Definition
 */
public class Env {
    public int clocks;
    public CountRowVars counter;

    public Env() {
        this.clocks = 0;
        this.counter = new CountRowVars();
    }

    public Env(int clocks, CountRowVars counter) {
        this.clocks = clocks;
        this.counter = counter;
    }

    public void checkClocks() {
        this.clocks = this.clocks + 1;

        if (this.clocks >= MemoryModel.REFRESH_INTERVAL) {
            this.clocks = 1;
            this.resetCounters();
        }
    }

    public void tickCounter(Loc loc, int v) {
        if (this.counter.map.containsKey(loc)) {
            int tmp = this.counter.map.get(loc);
            this.counter.map.put(loc, tmp + v);
        } else {
            this.counter.map.put(loc, v);
        }
    }

    public void resetCounter(Loc loc, int v) {
        this.counter.map.put(loc, v);
    }

    public void resetCounters() {
        for (Loc loc : this.counter.map.keySet()) {
            resetCounter(loc, 0);
        }
    }

    /**
     * Row Counter Definition
     */
    public static class CountRowVars {
        public HashMap<Loc, Integer> map;

        public CountRowVars() {
            this.map = new HashMap<Loc, Integer>();
        }

        public CountRowVars(HashMap<Loc, Integer> map) {
            this.map = map;
        }
    }
    // END OF Row Counter Definition
}