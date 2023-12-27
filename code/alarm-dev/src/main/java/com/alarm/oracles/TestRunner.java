package com.alarm.oracles;

import com.alarm.utils.TestRunnerException;
import net.automatalib.words.Word;

/**
 * Interface defining an API for running tests on a SUL.
 *
 * @param <I>
 * @param <O>
 */
public interface TestRunner<I,O> {

    /**
     * Sets up test
     */
    void setup();

    /**
     * Cleans up after running test
     */
    void cleanup();

    /**
     * Runs a given test.
     *
     * @param test the test to be run
     * @return the test output
     * @throws TestRunnerException if anything went wrong while running the test
     */
    O runTest(Word<I> test) throws TestRunnerException;
}
