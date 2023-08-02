package com.alarm.adapter.synthetic.components;

import com.alarm.adapter.synthetic.extendedcls.Mem;
import com.alarm.exceptions.ECCCorrectionException;
import com.alarm.exceptions.ECCDetectionException;

import java.util.HashMap;

/**
 * An abstract base class representing Error-Correcting Code (ECC) operations for a specific type of Location.
 *
 * @param <V> The type of Location to be used as keys in the ECC map, must extend the abstract class Loc.
 */
public abstract class ECCBase<V extends Loc<?>>{
    protected HashMap<V, byte[][]> map;

    public ECCBase(){
        map = new HashMap<>();
    }

    /**
     * Validates the ECC data for the specified Location in the context of the given Mem object.
     *
     * @param loc The Location for which to validate the ECC data.
     * @param mem The Mem object containing the data for validation.
     * @return True if the ECC data is valid for the given Location, false otherwise.
     * @throws ECCCorrectionException If an error is detected and not correctable by ECC.
     * @throws ECCDetectionException If an error arises while detecting by ECC.
     */
    public abstract boolean validate(V loc, Memory<V> mem) throws ECCCorrectionException, ECCDetectionException;

    /**
     * Validates the ECC data for all Locations in the context of the given Mem object.
     *
     * @param mem The Mem object containing the data for validation.
     * @return True if the ECC data is valid for all Locations, false otherwise.
     * @throws ECCCorrectionException If an error is detected and not correctable by ECC.
     * @throws ECCDetectionException If an error arises while detecting by ECC.
     */
    public abstract boolean validateAll(Mem mem) throws ECCCorrectionException, ECCDetectionException;

    /**
     * Tweaks the ECC data for the specified Location at the given position with the provided value.
     *
     * @param loc The Location for which to tweak the ECC data.
     * @param position The position within the ECC data to be tweaked.
     * @param val The value to be set at the specified position.
     */
    public abstract void tweak(V loc, int position, byte val);

    /**
     * Adds ECC data for the specified Location with the given value.
     *
     * @param loc The Location for which to add the ECC data.
     * @param val The value to be added as ECC data.
     */
    public abstract void add(V loc, int val);

    /**
     * Retrieves the ECC data for the specified Location.<br/>
     * (It may return a null value if the key is mapped to a null value)
     *
     * @param loc The Location for which to retrieve the ECC data.
     * @return The ECC data as a 2D byte array.
     */
    public byte[][] get(V loc) {
        return this.map.get(loc);
    }
}
