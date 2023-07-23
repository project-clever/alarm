package com.alarm.adapter.synthetic.components;



import java.util.HashMap;

/**
 * A concrete class representing a Memory of type Integer.
 * Extends the generic class Memory<Integer>.
 */
public class Mem extends Memory<Integer> {


    public Mem() {
        super();
    }

    public Mem(HashMap<Loc<Integer>, Integer> map) {
        super(map);
    }

}