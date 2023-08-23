package com.alarm.oracles;

import de.learnlib.api.oracle.MembershipOracle;
import de.learnlib.api.oracle.MembershipOracle.MealyMembershipOracle;
import de.learnlib.api.query.Query;
import de.learnlib.filter.cache.sul.SULCache;
import de.learnlib.oracle.membership.SULOracle;
import net.automatalib.words.Word;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Collection;

/**
 * This class is adapted from {@link SULOracle}. Unfortunately, the
 * implementation of LearnLib's cache oracle {@link SULCache} is unstable (the
 * version 0.12.0 at least).
 *
 * The implementation adds terminating outputs functionality
 *
 * @author Paul Fiterau
 *
 * This implementation also avoids re-running prefixes of queries if corresponding
 * outputs are stored in the cache.
 *
 * @author Matteo Sammartino
 */
public class CachingSULOracle<I, O extends TerminatingSymbol> implements MealyMembershipOracle<I, O> {

    private ObservationTree<I, O> root;

    private MembershipOracle<I, Word<O>> sulOracle;

    public CachingSULOracle(MembershipOracle<I, Word<O>> sulOracle, ObservationTree<I, O> cache) {
        this.root = cache;
        this.sulOracle = sulOracle;
    }

    @Override
    public void processQueries(Collection<? extends Query<I, Word<O>>> queries) {
        for (Query<I, Word<O>> q : queries) {
            Word<I> fullInput = q.getPrefix().concat(q.getSuffix());
            Word<O> output = answerFromCache(fullInput);

            int inputLength = fullInput.length();
            int outputLength = (output == null) ? 0 : output.length();

//            System.out.println("Processing: " + fullInput + " with cache " +
//                    ((output != null) ? fullInput.prefix(outputLength) : ""));

            if (outputLength < inputLength) {
                // Query oracle only for unknown suffix
                int suffixLength = inputLength - outputLength;
                Word<O> sulOutput = sulOracle.answerQuery(fullInput.prefix(outputLength), fullInput.suffix(suffixLength));
                output = (outputLength == 0) ? sulOutput : output.concat(sulOutput);
                storeToCache(fullInput, output);
            }
            output = output.suffix(q.getSuffix().size());
            q.answer(output);
        }
    }

    private void storeToCache(Word<I> input, Word<O> output) {
        root.addObservation(input, output);
    }

    @Nullable private Word<O> answerFromCache(Word<I> input) {
//        if (terminatingOutputs.isEmpty())
//            return root.answerQuery(input);
//        else {
        Word<O> output = root.answerQuery(input, true);
        if (output.length() < input.length()) {
            if (output.isEmpty()) {
                return null;
            } else {
                if (output.lastSymbol().isTerminating()) {
                    Word<O> extendedOutput = output;
                    while (extendedOutput.length() < input.length()) {
                        extendedOutput = extendedOutput.append(output
                                .lastSymbol());
                    }
                    return extendedOutput;
                } else {
                    // We only return the known output
                    return output;
                }
            }
        } else {
            return output;
        }
    }

    public Word<O> answerQueryWithoutCache(Word<I> input) {
        return sulOracle.answerQuery(input);
    }
}
