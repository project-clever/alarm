package com.alarm.adapter.synthetic.TrrPolicies;

import com.alarm.adapter.synthetic.components.Loc;
import com.alarm.adapter.synthetic.components.TRRBase;

import java.util.HashMap;


/**
 * Static TRR policy where the TRR:
 * <ul>
 *     <li>Tracks only given Rows</li>
 *     <li>Blast radius is default 1 unless specified in constructor.</li>
 *     <li>Refresh rate is default 6500 unless specified in constructor.</li>
 *     <li>TRR keeps track of Locations given till they meet the threshold.</li>
 *     <li>TRR Threshold is default 100 unless specified in constructor.</li>
 * </ul>
 */
public class TrrStatic<V extends Loc<?>> extends TRRBase<V> {

    public final int trrThreshold;

    public TrrStatic(HashMap<V, Integer> map,int trrThreshold, int radius, int refreshInterval){
        super(map.size(), map, radius, refreshInterval);
        this.trrThreshold = trrThreshold;
    }

    public TrrStatic(HashMap<V, Integer> map){
        super(map.size(),1, 6_500);
        trrThreshold = 100;
    }

    /**
     *
     * @throws IllegalArgumentException if the Loc given can't be found within the map.
     */
    @Override
    public boolean checkSingleCounter(V loc) {
        Integer counterValue = map.get(loc);
        if(counterValue == null)
            throw new IllegalArgumentException("Parameter can't be found in #checkSingleCounter()");
        return counterValue < 100;
    }

    /**
     * TickCounter() for this class keeps track of only given Locations till it reaches the threshold,
     * in which then the Location is reset back to zero and removed from the map.
     * @param loc The Location for which to tick the counter.
     * @param v The value by which to increment the counter.
     */
    @Override
    public void tickCounter(V loc, int v) {
        if (!map.containsKey(loc))
            return;

        //Increment location counter by v.
        map.put(loc, map.get(loc)+v);
        if(checkSingleCounter(loc))
            resetCounter(loc);
    }

    @Override
    public int calLocCode(V loc, int n) {
        return 0;
    }
}
