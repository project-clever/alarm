package com.alarm.adapter;

import com.alarm.alphabets.HammerAction;
import com.alarm.alphabets.HammerOutput;
import com.alarm.config.AdapterConfig;
import com.alarm.tool.TestRunner;

public abstract class RowhammerAdapter<O extends HammerOutput> implements TestRunner<HammerAction,O> {
    protected AdapterConfig config;
    protected RowhammerAdapter(AdapterConfig config) {
        this.config = config;
    }

    public AdapterConfig getConfig() {
        return config;
    }
}
