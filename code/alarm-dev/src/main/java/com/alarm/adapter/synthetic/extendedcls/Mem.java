package com.alarm.adapter.synthetic.extendedcls;

import com.alarm.adapter.synthetic.components.Memory;
import com.alarm.adapter.synthetic.extendedcls.L;

import java.util.HashMap;
import java.util.HashSet;

/**
 * A concrete class representing a Memory of type Integer.
 * Extends the generic class {@code Memory<Loc<Integer>>}.
 */
public class Mem extends Memory<L> {


    public Mem(int size) {
        super(size);
        //Load MEM map with L classes and associated value of zero
        for (int i = 0; i<size; i++){
            map.put(new L(i), 0);
        }
    }

    public Mem(HashMap<L, Integer> map) {
        super(map);
    }

    @Override
    public HashSet<L> neighbours(L loc){
        HashSet<L> out = new HashSet<>();
        for (L l : this.map.keySet()) {
            if (loc.distance(l) <= BLAST_RADIUS && loc.distance(l) > 0)
                out.add(l);
        }
        return out;
    }
}