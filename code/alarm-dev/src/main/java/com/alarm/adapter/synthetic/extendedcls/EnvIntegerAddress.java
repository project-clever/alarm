package com.alarm.adapter.synthetic.extendedcls;

import com.alarm.adapter.synthetic.components.Env;

/**
 * Environment Definition for Integer Addresses
 * This class represents an environment definition with clock management and row counters
 * for a specific type of Location, where the Location is represented by integers.
 */
public class EnvIntegerAddress extends Env<L> {

    /**
     * Constructor to create an environment for integer addresses with the specified size.
     *
     * @param size The size of the environment, used to initialize the row counter for integer addresses.
     */
    public EnvIntegerAddress(int size) {
        super(size);
        for (int i = 0; i < size; i++) {
            counter.map.put(new L(i), 0);
        }
    }
}
