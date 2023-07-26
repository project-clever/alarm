package com.alarm.tool;

import net.automatalib.words.Word;

public interface TestRunner<I,O> {

    public void setup();
    public void cleanup();
    public O runTest(Word<I> test);
}
