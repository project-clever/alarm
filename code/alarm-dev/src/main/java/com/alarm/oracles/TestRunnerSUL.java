package com.alarm.oracles;

import com.alarm.tool.TestRunner;
import de.learnlib.api.SUL;
import net.automatalib.words.Word;

/**
 * A SUL that allows running a test consisting of multiple tests
 *
 * @author Matteo Sammartino
 */
public class TestRunnerSUL<I,O> implements SUL<I, O> {
    private TestRunner<I,O> testRunner;
    private Word<I> testPrefix;


    /**
     * Intialises a TestRunnerSul with the given TestRunner
     * @param testRunner Object of type {@link alarm.com.tool.TestRunner} used to run tests
     */
    public TestRunnerSUL(TestRunner<I,O> testRunner) {
        this.testRunner = testRunner;
        testPrefix = null;
    }

    /**
     * Sets initial test prefix for {@link #step}
     * @param prefix initial test prefix
     */
    public void setInitialTestPrefix(Word<I> prefix) {
        this.testPrefix = prefix;
    }

    /**
     * Sets up test runner and clears initial prefix
     */
    @Override
    public void pre() {
        testRunner.setup();
        testPrefix = Word.epsilon();
    }

    /**
     * Performs test runner cleanup
     */
    @Override
    public void post() {
        testRunner.cleanup();
    }

    /**
     * Runs a test consisting of the current test prefix and in.
     * This test is used as prefix for next step.
     * @param in
     *         input to the SUL
     *
     * @return test result
     */
    @Override
    public O step(I in) {
        Word<I> test = testPrefix.append(in);
        System.out.println("Running: " + test);

        testRunner.setup();
        O result = testRunner.runTest(test);
        testRunner.cleanup();

        testPrefix = test;
        return result;
    }
}
