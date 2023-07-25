package com.alarm.oracles;

import de.learnlib.api.SUL;
import de.learnlib.api.query.Query;
import de.learnlib.oracle.membership.SULOracle;
import net.automatalib.words.Word;

import java.util.Collection;
import java.util.Collections;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class TestRunnerSULOracle<I,O> extends SULOracle<I,O> {
    private TestRunnerSUL<I,O> testRunnerSUL;

    public TestRunnerSULOracle(TestRunnerSUL<I, O> testRunnerSUL) {

        super(testRunnerSUL);
        this.testRunnerSUL = testRunnerSUL;
    }

    @Override
    public void processQueries(Collection<? extends Query<I, Word<O>>> queries) {
        for (Query<I,Word<O>> q: queries)
            q.answer(answerQuery(q.getPrefix(), q.getSuffix()));
    }

    @Override
    public Word<O> answerQuery(Word<I> prefix, Word<I> suffix) {
        //System.out.println("Answering query: " + prefix + " - " + suffix);
        testRunnerSUL.pre();
        testRunnerSUL.setInitialTestPrefix(prefix);

        Word<O> result =  Word.fromList(suffix
                .stream()
                .map((s) -> testRunnerSUL.step(s))
                .collect(Collectors.toList()));
        testRunnerSUL.post();
        return result;
    }
}
