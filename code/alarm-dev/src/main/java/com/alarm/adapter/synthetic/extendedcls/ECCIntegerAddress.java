package com.alarm.adapter.synthetic.extendedcls;

import com.alarm.adapter.synthetic.components.ECCBase;
import com.alarm.adapter.synthetic.components.Memory;
import com.alarm.exceptions.ECCCorrectionException;
import com.backblaze.erasure.RS;
import com.backblaze.erasure.ReedSolomon;

/**
 * ECC Implementation for Integer Addresses.
 * This class represents an ECC implementation where Location is represented by integers.
 * It extends the ECCBase class with the Location type set to L.
 * Note: The size of the 2D array for ECC data is determined by RS.TOTAL_SHARDS and RS.PARITY_SHARDS defined in the RS class.
 */
public class ECCIntegerAddress extends ECCBase<L> {

    /**
     * Constructor to create an ECC implementation for integer addresses with the specified size.
     *
     * @param size The size of the ECC implementation, used to initialize the ECC data for integer addresses.
     */
    public ECCIntegerAddress(int size) {
        super();
        int numRows = RS.TOTAL_SHARDS;
        int numCols = RS.PARITY_SHARDS;
        for (int i = 0; i < size; i++) {
            map.put(new L(i), new byte[numRows][numCols]);
        }
    }


    @Override
    public boolean validate(L loc, Memory<L> mem) throws ECCCorrectionException {
        String code = Integer.toBinaryString(mem.read(loc));
        int WORD_SIZE = RS.DATA_SHARDS;
        while (code.length() < WORD_SIZE)
            code = "0" + code;
        byte[] tmpBuffer = new byte[RS.PARITY_SHARDS];
        ReedSolomon reedSolomon = ReedSolomon.create(WORD_SIZE, RS.PARITY_SHARDS);
        boolean isValid = reedSolomon.isParityCorrect(this.map.get(loc), 0, RS.PARITY_SHARDS, tmpBuffer);
        if (!isValid)
            throw new ECCCorrectionException();
        return true;
    }

    @Override
    public boolean validateAll(Mem mem) throws ECCCorrectionException{
        for (L l : mem.map.keySet()) {
            validate(l, mem);
        }
        return true;
    }

    @Override
    public void tweak(L loc, int position, byte val) {
        byte[][] tmp = map.get(loc);
        tmp[position][0] = val;
        map.put(loc, tmp);
    }

    @Override
    public void add(L loc, int val) {
        String in = Integer.toBinaryString(val);
        while (in.length() < RS.DATA_SHARDS)
            in = "0" + in;
        this.map.put(loc, RS.encode(in));
    }
}
