package com.alarm.adapter.synthetic.components;

import java.util.HashMap;

/**
 * An abstract class representing a Target-Row Refresh (TRR) base for a specific type of Location.
 * It provides common functionalities related to TRR for different subclasses extending this class.
 *
 * @param <V> The type of Location to be used as keys in the TRR map, must extend the abstract class Loc.
 */
public abstract class TRRBase<V extends Loc<?>> {

    public final int REFRESH_INTERVAL;

    protected final int counters;

    protected final int radius;

    public HashMap<V, Integer> map;

    /**
     * Default constructor for TRRBase.
     * Initialises the TRR counters, radius, the map and refresh rate with default data.
     */
    protected TRRBase() {
        this(1,new HashMap<>(), 1, 6_500);
    }

    /**
     * Constructor for TRRBase with custom counters, and radius.
     *
     * @param counters The number of counters for the TRR.
     * @param radius The radius for the TRR.
     * @param refreshInterval The time between two regular refreshes.
     */
    protected TRRBase(int counters, int radius, int refreshInterval){
        this(counters, new HashMap<>(), radius, refreshInterval);
    }

    /**
     * Constructor for TRRBase with custom counters, radius, and a provided map.
     *
     * @param counters The number of counters for the TRR.
     * @param map The HashMap representing the TRR map with Location objects as keys and counter values as values.
     * @param radius The radius for the TRR.
     * @param refreshInterval The time between two regular refreshes.
     */
    protected TRRBase(int counters, HashMap<V, Integer> map, int radius, int refreshInterval) {
        this.counters = counters;
        this.radius = radius;
        this.map = map;
        REFRESH_INTERVAL = refreshInterval;
    }

    /**
     * Checks if a single counter associated with the given Location is valid for TRR.
     *
     * @param loc The Location for which to check the counter.
     * @return True if the counter is valid for TRR, false otherwise.
     */
    public abstract boolean checkSingleCounter(V loc);

    /**
     * Ticks the counter associated with the given Location by incrementing it by the specified value.
     *
     * @param loc The Location for which to tick the counter.
     * @param v The value by which to increment the counter.
     */
    public abstract void tickCounter(V loc, int v);


    /**
     * Resets the counter associated with the given Location to zero.
     *
     * @param loc The Location for which to reset the counter.
     */
    public void resetCounter(V loc){
        if (map.containsKey(loc)) {
            map.put(loc, 0);
        }
    }

    /**
     * Resets all counters in the TRR map to zero.
     */
    public void resetCounters(){
        map.keySet().forEach(this::resetCounter);
    }

    /**
     * Checks the clocks and resets counters if the clock value is greater than or equal to the refresh interval.
     *
     * @param clock The current clock value to be checked against the refresh interval.
     */
    public void checkClocks(int clock){
        if (clock >= REFRESH_INTERVAL)
            resetCounters();
    }

    /**
     * Calculates the Location code for Time-Restricted Refresh (TRR).
     *
     * @param loc The Location for which to calculate the code.
     * @param n The number to use for the modulo operation.
     * @return The calculated Location code as an integer.
     */
    public abstract int calLocCode(V loc, int n);
}

