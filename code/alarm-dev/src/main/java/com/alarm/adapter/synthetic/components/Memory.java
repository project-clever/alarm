package com.alarm.adapter.synthetic.components;

import java.util.HashMap;
import java.util.HashSet;

//NOTE:
//Some variables are still hardcoded from MemoryModel as they are taken from other classes (Mentioned in the javadocs too)

/**
 * Abstract representation of Generic Memory
 * @param <V> The type of Location to be used as keys in map, must extend the abstract class Loc.
 */
public abstract class Memory<V extends Loc<?>> {

    public static final int[] FLIP_BITS = { 0, 1, 3, 7, 15, 31, 63, 127, 255, 511, 1023 }; //Kept from Learner as reference

    public HashMap<V, Integer> map;

    public final int BLAST_RADIUS = 1; //default value
    public final int map_size;

    public Memory(int size){
        map = new HashMap<>(size);
        map_size = size;
    }

    public Memory(HashMap<V, Integer> map){
        this.map = map;
        map_size = map.size();
    }

    /**
     * Reads the integer value associated with the specified key {@code loc} from the map.
     *
     * @param loc The key for which the integer value is read.
     * @return The integer value associated with the key {@code loc}
     */
    public int read(V loc) {
        return this.map.get(loc);
    }

    /**
     * Writes an integer value {@code n} to the specified key {@code loc} in the map.
     * If {@code loc} is {@code null}, the method does nothing and returns immediately.
     * The method updates the map, associating the key {@code loc} with the provided integer value {@code n}.
     *
     * @param loc The key to which the integer value is written.
     * @param n   The integer value to be written to the map.
     */
    public void write(V loc, int n) {
        if (loc == null)
            return;
        this.map.put(loc, n);
    }

    /**
     * Performs a bitwise flip operation on the value associated with the specified key.
     * The method calculates the XOR of the current value at the key with a bitmask provided
     * by {@code MemoryModel.FLIP_BITS[v]}.
     * If {@code v} is outside the valid range of indices for {@code MemoryModel.FLIP_BITS}
     * it could throw ArrayOutOfBoundsException
     *
     * @param loc The key for which the bitwise flip operation is performed.
     * @param v   The index to access the bitmask from {@code MemoryModel.FLIP_BITS}.
     */
    public void flip(V loc, int v){
        if (loc == null)
            return;
        int tmp = this.map.get(loc);
        this.map.put(loc, tmp ^ FLIP_BITS[v]);
    }

    /**
     * Returns a HashSet of neighboring keys (Loc objects) within the specified blast radius of the given key.
     * The method iterates through the keys in the map and checks if the distance between each key and the provided key
     * {@code loc} is less than or equal to {@code MemoryModel.BLAST_RADIUS} and greater than 0.
     *
     * @param loc The key for which the neighboring keys are sought.
     * @return A HashSet of neighboring keys (Loc objects) within the blast radius of {@code loc}.
     *         An empty HashSet is returned if there are no neighbors or if the input {@code loc} is not present in the map.
     */
    public abstract HashSet<V> neighbours(V loc);
}
