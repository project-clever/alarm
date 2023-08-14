package com.alarm.adapter.synthetic.extendedcls;

import com.alarm.adapter.synthetic.components.Memory;

import java.util.HashMap;
import java.util.HashSet;

/**
 * A concrete class representing a Memory of type Integer.
 * Extends the generic class {@code Memory<Loc<Integer>>}.
 */
public class MemIntAddress extends Memory<LocIntAddress> {


    public MemIntAddress(int size) {
        super(size);
        //Load MEM map with L classes and associated value of zero
        for (int i = 0; i<size; i++){
            map.put(new LocIntAddress(i), 0);
        }
    }

    public MemIntAddress(HashMap<LocIntAddress, Integer> map) {
        super(map);
    }

    @Override
    public HashSet<LocIntAddress> neighbours(LocIntAddress loc){
        HashSet<LocIntAddress> out = new HashSet<>();
        for (LocIntAddress l : map.keySet()) {
            if (loc.distance(l) <= BLAST_RADIUS && loc.distance(l) > 0)
                out.add(l);
        }
        return out;
    }
}