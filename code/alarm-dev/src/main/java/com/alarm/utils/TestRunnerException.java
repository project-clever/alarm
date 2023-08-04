package com.alarm.utils;

public class TestRunnerException extends Exception {

    public TestRunnerException(String testDescription) {
        super("Error running test: " + testDescription);
    }
}


