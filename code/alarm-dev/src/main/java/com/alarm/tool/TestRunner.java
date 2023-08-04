package com.alarm.tool;

import com.alarm.utils.TestRunnerException;
import net.automatalib.words.Word;

public interface TestRunner<I,O> {

    public void setup();
    public void cleanup();
    public O runTest(Word<I> test) throws TestRunnerException;
}
