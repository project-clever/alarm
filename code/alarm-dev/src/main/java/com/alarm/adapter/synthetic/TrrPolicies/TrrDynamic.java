package com.alarm.adapter.synthetic.TrrPolicies;

import com.alarm.adapter.synthetic.components.Loc;
import com.alarm.adapter.synthetic.components.TRRBase;

/**
 * A class representing a Target-Row Refresh (TRR) implementation with a dynamic policy for a specific type of Location.
 * It extends the TRRBase class and provides functionalities related to dynamic TRR policy.
 *
 * @param <V> The type of Location to be used as keys in the TRR map, must extend the abstract class Loc.
 */
public class TrrDynamic<V extends Loc<?>> extends TRRBase<V> {
    @Override
    public boolean checkSingleCounter(V loc) {
        if (!map.containsKey(loc)) {
            if (map.size() < counters) {
                map.put(loc, 1);
            } else
                return true;
        }
        return map.get(loc) < 400;
    }

    @Override
    public void tickCounter(V loc, int v) {
        if (map.containsKey(loc)) {
            int tmp = map.get(loc);
            map.put(loc, tmp + v);
        } else if (map.size() < counters) {
            map.put(loc, v);
        } else {
            int min = Integer.MAX_VALUE;
            V minKey = null;
            for (V l : map.keySet()) {
                if (map.get(l) < min) {
                    min = map.get(l);
                    minKey = l;
                }
            }
            map.remove(minKey);
            map.put(loc, v);
        }
    }

    @Override
    public int calLocCode(V loc, int n) {
        return 0;
    }
}
