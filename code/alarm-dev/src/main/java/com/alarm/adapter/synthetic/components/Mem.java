package com.alarm.adapter.synthetic.components;

import com.alarm.adapter.synthetic.MemoryModel;

import java.util.HashMap;
import java.util.HashSet;

//NOTE:
//Some variables are still hardcoded from MemoryModel as they are taken from other classes

/**
 * Memory Definition
 */
public class Mem {
    public HashMap<Loc, Integer> map;

		public Mem() {
        this.map = new HashMap<Loc, Integer>();
    }

		public Mem(HashMap<Loc, Integer> map) {
        this.map = map;
    }

    public int read(Loc loc) {
        return this.map.get(loc);
    }

    public void write(Loc loc, int n) {
        this.map.put(loc, n);
    }

    public void flip(Loc loc, int v) {
        int tmp = this.map.get(loc);
        this.map.put(loc, tmp ^ MemoryModel.FLIP_BITS[v]);
    }

    public HashSet<Loc> neighbours(Loc loc) {
        HashSet<Loc> out = new HashSet<>();
        for (Loc l : this.map.keySet()) {
            if (distance(loc, l) <= MemoryModel.BLAST_RADIUS && distance(loc, l) > 0)
                out.add(l);
        }
        return out;
    }

    public int distance(Loc l1, Loc l2) {
        int l1_v = Loc.getValue(l1);
        int l2_v = Loc.getValue(l2);
        return Math.abs(l1_v - l2_v);
    }
}