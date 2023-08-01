package com.alarm.adapter.synthetic.extendedcls;

import com.alarm.adapter.synthetic.components.ECCBase;
import com.alarm.adapter.synthetic.components.Memory;
import com.alarm.adapter.synthetic.extendedcls.L;
import com.alarm.adapter.synthetic.extendedcls.Mem;
import com.alarm.exceptions.ECCCorrectionException;
import com.backblaze.erasure.RS;
import com.backblaze.erasure.ReedSolomon;

public final class EccSchemas {
    public final ECCBase<L> BASE_CONFIG = new ECCIntegerAddress(3_000);


}
