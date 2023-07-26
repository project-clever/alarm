package com.alarm.adapter.synthetic.components;

import java.util.HashMap;
import java.util.HashSet;

/**
 * A concrete class representing a Memory of type Integer.
 * Extends the generic class {@code Memory<Loc<Integer>>}.
 */
public class Mem extends Memory<L> {


    public Mem() {
        super();
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