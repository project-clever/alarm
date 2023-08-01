package com.alarm.adapter.synthetic.extendedcls;

import com.alarm.adapter.synthetic.components.TRRBase;

/**
 * TRR Implementation for Integer Addresses.
 * This class represents a TRR implementation where Location is represented by integers.
 * It extends the TRRBase class with the Location type set to L.
 */
public class TRRIntegerAddress extends TRRBase<L> {

    /**
     * Constructor to create a TRR implementation for integer addresses with the specified size.
     *
     * @param size The size of the TRR implementation, used to initialize the TRR data for integer addresses.
     */
    public TRRIntegerAddress(int size){
        super();
        for (int i = 0; i<size; i++){
            map.put(new L(i), 0);
        }
    }
    @Override
    public boolean checkSingleCounter(L loc) {
        return map.get(loc) < 3_000; //3_000 represents a TRR Threshold
    }

    @Override
    public void tickCounter(L loc, int v) {
        if (map.containsKey(loc)) {
            int tmp = map.get(loc);
            this.map.put(loc, tmp + v);
        } else if (map.size() < counters) {
            map.put(loc, v);
        } else {
            int min = Integer.MAX_VALUE;
            L minKey = null;
            for (L l : map.keySet()) {
                if (map.get(l) < min) {
                    min = this.map.get(l);
                    minKey = l;
                }
            }
            map.remove(minKey);
            map.put(loc, v);
        }
    }

    @Override
    public int calLocCode(L loc, int n) {
        int v = loc.getValue();
        int m = 16; //modulo set value
        return (v % m) ^ (n % m);

    }
}
