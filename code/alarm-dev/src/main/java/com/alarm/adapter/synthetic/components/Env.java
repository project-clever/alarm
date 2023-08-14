package com.alarm.adapter.synthetic.components;

import java.util.HashMap;

/**
 * Environment Definition
 * This class represents an environment definition with clock management and row counters.
 *
 * @param <V> The type of Location to be used as keys in the row counter, must extend the abstract class Loc.
 */
public class Env<V extends Loc<?>> {

    /**
     * The time between two regular refreshes
     */
    public final int REFRESH_INTERVAL;

    /**
     * The current number of clocks.
     */
    public int clocks;

    /**
     * Row counter for keeping track of counters associated with each location.
     */
    public CountRowVars<V> counter;

    /**
     * Constructor to create an environment with the specified refresh interval.
     *
     * @param refreshInterval The time between two regular refreshes.
     */
    public Env(int refreshInterval) {
        this(0, refreshInterval, new CountRowVars<>());
    }

    /**
     * Constructor to create an environment with the specified clocks and row counter.
     *
     * @param clocks The current number of clocks.
     * @param refreshInterval The time between two regular refreshes
     * @param counter The row counter containing counters associated with each location.
     */
    public Env(int clocks, int refreshInterval, CountRowVars<V> counter) {
        this.clocks = clocks;
        REFRESH_INTERVAL = refreshInterval;
        this.counter = counter;
    }

    /**
     * Increases the clock count and resets the counters if the refresh interval is reached.
     */
    public void checkClocks() {
        clocks += 1;

        if (clocks >= REFRESH_INTERVAL) {
            clocks = 1;
            resetCounters();
        }
    }

    /**
     * Increments the counter value for the specified location.
     *
     * @param loc The Location for which to increment the counter value.
     * @param v The value to increment the counter by.
     */
    public void tickCounter(V loc, int v) {
        if (counter.map.containsKey(loc)) {
            int tmp = counter.map.get(loc);
            counter.map.put(loc, tmp + v);
        } else {
            counter.map.put(loc, v);
        }
    }

    /**
     * Resets the counter value for the specified location.
     *
     * @param loc The Location for which to reset the counter value.
     * @param v The value to set as the new counter value.
     */
    public void resetCounter(V loc, int v) {
        counter.map.put(loc, v);
    }

    /**
     * Resets all the counters in the environment.
     */
    public void resetCounters() {
        counter.map.forEach(this::resetCounter);
    }

    /**
     * Row Counter Definition
     * This class represents a row counter to keep track of counters associated with each location.
     *
     * @param <V> The type of Location to be used as keys in the row counter, must extend the abstract class Loc.
     */
    public static class CountRowVars<V extends Loc<?>> {
        /**
         * The map that associates each location with its counter value.
         */
        public HashMap<V, Integer> map;

        /**
         * Constructor to create a row counter with the specified size.
         */
        public CountRowVars() {
            map = new HashMap<>();
        }

        /**
         * Constructor to create a row counter with the specified map.
         *
         * @param map The map containing counters associated with each location.
         */
        public CountRowVars(HashMap<V, Integer> map) {
            this.map = map;
        }
    }
    // END OF Row Counter Definition
}
