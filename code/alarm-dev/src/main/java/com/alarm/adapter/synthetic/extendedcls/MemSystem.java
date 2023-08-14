package com.alarm.adapter.synthetic.extendedcls;

import com.alarm.adapter.synthetic.components.ECCBase;
import com.alarm.adapter.synthetic.components.Env;
import com.alarm.adapter.synthetic.components.TRRBase;
import com.alarm.exceptions.TRRException;

import java.util.Random;


/**
 * The MemSystem class represents a memory system with optional Error-Correcting Code (ECC) and Target-Row Refresh (TRR) functionality.
 * It provides read and write operations on memory locations with TRR and ECC checks and supports various memory-related configurations.
 */
public class MemSystem{
    private ECCBase<LocIntAddress> ECC; //Optional
    private TRRBase<LocIntAddress> TRR; //Optional

    private EnvIntAddress ENV;
    private MemIntAddress MEM;

    //Data to be loaded from Config class
    public final int TRR_THRESHOLD;
    public final int TRR_COUNTERS;

    /** The minimal number of accesses between two refreshes to rows in the blast-radius, before a flip can happen*/
    public final int RH_THRESHOLD;

    /** The time between two regular refreshes (i.e., not in reaction to a potential Row-hammer attack)*/
    public final int REFRESH_INTERVAL;

    /** The maximum distance between attacker row and victim row that can make bit flips happen.*/
    public final int BLAST_RADIUS;
    public final int  TRR_RADIUS;
    public final boolean ECC_STATUS;
    public final int FLIP_PROBABILITY = 1;


    public MemSystem(int memSize, int trrThreshold, int trrCounter, int rhThreshold, int refreshInterval, int blastRadius,
                     int trrRadius, boolean eccStatus){
        //TODO:add params to MemSystem or load data from Config file
        TRR_THRESHOLD = trrThreshold;
        RH_THRESHOLD = rhThreshold;
        BLAST_RADIUS = blastRadius;
        TRR_COUNTERS = trrCounter;
        REFRESH_INTERVAL = refreshInterval;
        TRR_RADIUS = trrRadius;
        ECC_STATUS = eccStatus;

        MEM = new MemIntAddress(memSize);
        ENV = new EnvIntAddress(memSize, refreshInterval);

    }

    public MemSystem withTRR(TRRBase<LocIntAddress> trrPolicy)throws IllegalArgumentException{
        if (trrPolicy != null) {
            TRR = trrPolicy;
        } else {
            throw new IllegalArgumentException("Parameter can't be found in TrrPolicies or parameter may be Null.");
        }
        return this;
    }

    public MemSystem withECC(ECCBase<LocIntAddress> eccSchema) throws IllegalArgumentException{
        if (eccSchema != null) {
            ECC = eccSchema;
        } else {
            throw new IllegalArgumentException("Parameter can't be found in EccSchemas or parameter may be Null.");
        }
        return this;
    }

    /**
     * Reads data from a memory location with optional TRR checks.
     *
     * @param loc    The memory location to read from.
     * @param flip_v The flip_v value used during ECC operations.
     * @return The data read from the memory location.
     * @throws TRRException If a TRR-related exception occurs during the read operation.
     */
    public int read(LocIntAddress loc, int flip_v) throws TRRException{
        clock(loc, flip_v);
        ENV.resetCounter(loc, 0);
        if (TRR != null) TRR.resetCounter(loc);
        return MEM.read(loc);
    }

    /**
     * Writes data to a memory location with optional ECC and TRR checks.
     *
     * @param loc    The memory location to write to.
     * @param v      The data to write to the memory location.
     * @param flip_v The flip_v value used during ECC operations.
     * @throws TRRException If a TRR-related exception occurs during the write operation.
     */
    public void write(LocIntAddress loc, int v, int flip_v) throws TRRException{
        clock(loc, flip_v);
        MEM.write(loc, v);
        ENV.resetCounter(loc, 0);

        if (TRR != null) TRR.resetCounter(loc);
        if (ECC != null) ECC.add(loc, v);
    }

    /**
     * Performs clocking and TRR/ECC checks for a given memory location.
     *
     * @param loc    The memory location to perform clocking for.
     * @param flip_v The flip_v value used during ECC operations.
     * @throws TRRException If a TRR-related exception occurs during the clocking operation.
     */
    public void clock(LocIntAddress loc, int flip_v) throws TRRException{
        if (TRR != null) TRR.checkClocks(ENV.clocks);
        ENV.checkClocks();
        for (LocIntAddress l : MEM.neighbours(loc)) {
            if(TRR != null) {
                if (TRR.checkSingleCounter(l)) {
                    int attenuation_factor = BLAST_RADIUS - loc.distance(l) + 1;
                    ENV.tickCounter(l, attenuation_factor);
                    TRR.tickCounter(l, attenuation_factor);
                } else {
                    ENV.resetCounter(l, 0);
                    TRR.resetCounter(l);
                    read(l, flip_v);
                    throw new TRRException();
                }
            }
            Random r = new Random();
            double pr = r.nextDouble();
            int tmp = ENV.counter.map.get(l);
            if (tmp >= RH_THRESHOLD && pr <= FLIP_PROBABILITY) {
                MEM.flip(l, flip_v);
                if (ECC!= null) {
                    for (int i = 0; i < flip_v; i++)
                        ECC.tweak(l, i, (byte) 1);
                }
            }
        }
    }

    //Public Getters
    public ECCBase<LocIntAddress> getECC(){
        return ECC;
    }

    public TRRBase<LocIntAddress> getTRR(){
        return TRR;
    }

    public MemIntAddress getMEM() {
        return MEM;
    }

    public Env<LocIntAddress> getENV() {
        return ENV;
    }
}



