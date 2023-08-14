package com.alarm.adapter.synthetic.EccSchemas;

import com.alarm.adapter.synthetic.components.ECCBase;
import com.alarm.adapter.synthetic.components.Loc;
import com.alarm.adapter.synthetic.components.Memory;
import com.alarm.exceptions.ECCCorrectionException;
import com.backblaze.erasure.RS;
import com.backblaze.erasure.ReedSolomon;

/**
 * ECC Implementation for Loc Addresses using Reed-Solomon Correction.
 * It extends the ECCBase class with the Location type set to generic.
 */
public class EccReedSolomon<V extends Loc<?>> extends ECCBase<V> {

    public EccReedSolomon(){super();}

    @Override
    public boolean validate(V loc, Memory<V> mem) throws ECCCorrectionException {
        String code = Integer.toBinaryString(mem.read(loc));
        int WORD_SIZE = RS.DATA_SHARDS;
        while (code.length() < WORD_SIZE)
            code = "0" + code;
        byte[] tmpBuffer = new byte[RS.PARITY_SHARDS];
        ReedSolomon reedSolomon = ReedSolomon.create(WORD_SIZE, RS.PARITY_SHARDS);
        boolean isValid = reedSolomon.isParityCorrect(map.get(loc), 0, RS.PARITY_SHARDS, tmpBuffer);
        if (!isValid)
            throw new ECCCorrectionException();
        return true;
    }

    @Override
    public boolean validateAll(Memory<V> mem) throws ECCCorrectionException {
        for (V l : mem.map.keySet()) {
            validate(l, mem);
        }
        return true;
    }

    @Override
    public void tweak(V loc, int position, byte val) {
        byte[][] tmp = map.get(loc);
        if (tmp == null)
            throw new IllegalArgumentException("Parameter can't be found in #tweak().");
        tmp[position][0] = val;
        map.put(loc, tmp);
    }

    @Override
    public void add(V loc, int val) {
        String in = Integer.toBinaryString(val);
        while (in.length() < RS.DATA_SHARDS)
            in = "0" + in;
        map.put(loc, RS.encode(in));
    }
}
