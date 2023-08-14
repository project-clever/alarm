package com.alarm.adapter.synthetic.EccSchemas;

import com.alarm.adapter.synthetic.components.ECCBase;
import com.alarm.adapter.synthetic.components.Loc;
import com.alarm.adapter.synthetic.components.Memory;
import com.alarm.exceptions.ECCDetectionException;
import com.alarm.helper.HammingCode;


/**
 * A class representing Error-Correcting Code (ECC) operations using Hamming Code for a specific type of Location.
 * todo NOTE: This class is an attempt to use the Hamming class to validate Locations
 * @param <V> The type of Location to be used as keys in the ECC map, must extend the abstract class Loc.
 */
public class EccHamming<V extends Loc<?>> extends ECCBase<V> {

    /**
     * Default constructor for EccHamming.
     * Initializes the ECC map.
     */
    public EccHamming() {
        super();
    }

    @Override
    public boolean validate(V loc, Memory<V> mem) throws ECCDetectionException {
        byte[][] eccData = map.get(loc);
        if (eccData == null)
            throw new ECCDetectionException("ECC data not found for location " + loc.val());

        int receivedValue = mem.read(loc);
        int codeSize = eccData[0].length;

        int[] receivedCode = HammingCode.getArr(receivedValue, codeSize);

        for (int i = 0; i < receivedCode.length; i++) {
            if (receivedCode[i] != eccData[0][i])
                throw new ECCDetectionException("Error detected using Hamming Code at #validate().");
        }
        return true;
    }

    @Override
    public boolean validateAll(Memory<V> mem) throws ECCDetectionException {
        for (V l : mem.map.keySet()) {
            validate(l, mem);
        }
        return true;
    }

    @Override
    public void tweak(V loc, int position, byte val) {
        throw new UnsupportedOperationException("Tweaking is NOT supported for Hamming Codes");
    }

    @Override
    public void add(V loc, int val) {
        int[] hammingCode = HammingCode.getCode(Integer.toBinaryString(val));
        byte[][] byteArrayCode = new byte[1][hammingCode.length];
        for (int i = 0; i < hammingCode.length; i++) {
            byteArrayCode[0][i] = (byte) hammingCode[i];
        }
        map.put(loc, byteArrayCode);
    }

}

