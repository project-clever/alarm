package com.alarm.oracles;

import de.learnlib.api.oracle.MembershipOracle.MealyMembershipOracle;
import de.learnlib.api.query.Query;
import net.automatalib.words.Word;

import java.io.PrintWriter;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

/**
 * A membership oracle which executes each query multiple times in order to
 * handle non-determinism. In case the runs result in different outputs, it can
 * perform probabilistic sanitization. This entails running the query many
 * times, and computing the answer with the highest likelihood.
 *
 * If the likelihood is greater than a threshold the answer is returned,
 * otherwise an exception is thrown.
 *
 * This oracle provides a foundation for other oracles which may want to re-run
 * queries.
 *
 * @author Paul Fiterau
 */
public class SamplingSULOracle<I, O> implements MealyMembershipOracle<I, O> {

    private final double PROBABILISTIC_MIN_MULTIPLIER = 0.8;
    private final double probabilisticThreshold;
//    private static final double PASSABLE_PROBABILISTIC_THRESHOLD = 0.4;

    private final int runs;
    private PrintWriter log;

    protected MealyMembershipOracle<I, O> sulOracle;


    public SamplingSULOracle(int runs,
                             double probabilisticThreshold,
                             MealyMembershipOracle<I, O> sulOracle,
                             PrintWriter log) {
        this.sulOracle = sulOracle;
        this.runs = runs;
        this.probabilisticThreshold = probabilisticThreshold;
        this.log = log;
    }

    @Override
    public void processQueries(Collection<? extends Query<I, Word<O>>> queries) {
        for (Query<I, Word<O>> q : queries) {
            processQuery(q);
        }
    }

    public void processQuery(Query<I, Word<O>> q) {
        Word<O> output = getProbabilisticOutput(q);
        q.answer(output);
    }


    private Word<O> getProbabilisticOutput(Query<I, Word<O>> query) {
        log.println("Performing probabilistic sanitization");
        log.flush();

        LinkedHashMap<Word<O>, Integer> frequencyMap = new LinkedHashMap<>();
        Word<O> mostCommonOutput = null;

        for (int i = 0; i < runs; i++) {

            // update frequency map
            Word<O> answer = sulOracle.answerQuery(query.getPrefix(), query.getSuffix());

            if (!frequencyMap.containsKey(answer)) {
                frequencyMap.put(answer, 1);
            } else {
                frequencyMap.put(answer, frequencyMap.get(answer) + 1);
            }

            // after running enough tests, we can check whether we can return an
            // acceptable answer
            if (i >= runs * PROBABILISTIC_MIN_MULTIPLIER) {
                Entry<Word<O>, Integer> mostCommonEntry = frequencyMap
                        .entrySet().stream()
                        .max(new Comparator<Entry<Word<O>, Integer>>() {
                            public int compare(Entry<Word<O>, Integer> arg0,
                                    Entry<Word<O>, Integer> arg1) {
                                return arg0.getValue().compareTo(
                                        arg1.getValue());
                            }
                        }).get();
                mostCommonOutput = mostCommonEntry.getKey();
                double likelihood = (double) mostCommonEntry.getValue()
                        / (i + 1);

                log.println("Most likely answer has likelihood " + likelihood
                        + " after " + (i + 1) + " runs");
                if (likelihood >= probabilisticThreshold) {
                    log.println("Answer deemed to be in acceptable range, returning answer");
                    log.flush();
                    return mostCommonEntry.getKey();
                }

            }
        }

        log.flush();

        // we get here after exhausting the number of tests, without having
        // found an answer that is acceptable
        // **For now return the next best value**
        return mostCommonOutput;

    }

}
