package com.alarm.tool;

import de.learnlib.algorithms.lstar.mealy.ExtensibleLStarMealyBuilder;
import de.learnlib.algorithms.ttt.mealy.TTTLearnerMealy;
import de.learnlib.algorithms.ttt.mealy.TTTLearnerMealyBuilder;
import de.learnlib.api.algorithm.LearningAlgorithm;
import de.learnlib.api.logging.LearnLogger;
import de.learnlib.filter.statistic.Counter;
import de.learnlib.api.oracle.EquivalenceOracle;
import de.learnlib.api.oracle.MembershipOracle;
import de.learnlib.api.query.DefaultQuery;
import de.learnlib.util.mealy.MealyUtil;
import net.automatalib.automata.transducers.MealyMachine;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import net.automatalib.words.impl.Alphabets;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.*;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

// TODO:
//  - take inspiration from https://github.com/PROGNOSISTool/learner/blob/main/src/learner/Main.java
//  - see other examples here https://github.com/LearnLib/cav2015-example/tree/master/src/main/java/de/learnlib/example/cav2015/coffee
public class Main {
    // Logger
    private static final LearnLogger logger = LearnLogger.getLogger("ALARM");

    //Objects for counting queries
    private static Counter queryCounter;
    private static Counter membershipCounter;
    private static Counter equivalenceCounter;

    // Configuration
    // private static Config config;




    public static void main(String[] args) {
        if (args.length == 0) {
            printUsage();
            System.exit(-1);
        }

        try {
            Config config = loadConfig(args[0]);
            runLearner(config);
            // System.out.println(alarmConfig.learnerConfig.randomSeed);
        }
        catch(IOException e) {
            System.err.println("Error reading configuration file.");
            printUsage();
            System.exit(-1);
        }
        catch(Exception e) {
            System.err.println("Error whilst learning.");
            e.printStackTrace();
            System.exit(-1);
        }
    }

    private static void runLearner(Config config) throws Exception {
        logger.logEvent("Building query oracle...");
        MembershipOracle<String, Word<String>> queryOracle = buildQueryOracle(config);

        logger.logEvent("Building membership oracle...");
        MembershipOracle<String, Word<String>> memOracle = buildMembershipOracle(queryOracle);

        logger.logEvent("Building equivalence oracle...");
        EquivalenceOracle<MealyMachine<?, String, ?, String>, String, Word<String>> eqOracle = buildEquivalenceOracle(queryOracle);

        logger.logEvent("Building Learner...");

        LearnerConfig learnerConfig = config.learnerConfig;
        Alphabet<String> inputAlphabet = buildInputAlphabet(learnerConfig);
        Alphabet<String> outputAlphabet = buildOutputAlphabet(learnerConfig);

//        System.out.println(inputAlphabet);
        LearningAlgorithm<MealyMachine<?,String,?,String>, String, Word<String>> learner =
                switch(learnerConfig.algorithm) {
                    case TTT -> new TTTLearnerMealyBuilder<String, String>()
                            .withAlphabet(inputAlphabet)
                            .withOracle(memOracle)
                            .create();
                    case LSTAR -> new ExtensibleLStarMealyBuilder<String, String>()
                            .withAlphabet(inputAlphabet)
                            .withOracle(memOracle)
                            .create();
                };

//        learning = false;
//        TTTLearnerMealy<String, String> learner = new TTTLearnerMealyBuilder<String, String>()
//                .withAlphabet(alphabet)
//                .withOracle(memOracle)
//                .create();
//
        logger.logEvent("Starting learner...");
        MealyMachine<?, String, ?, String> model = learn(learner, eqOracle, inputAlphabet);
//
//        // final output to out.txt
        logger.logEvent("Done.");
        logger.logEvent("Successful run.");

        logger.logStatistic(queryCounter);
        logger.logStatistic(membershipCounter);
        logger.logStatistic(equivalenceCounter);
        logger.logModel(model);

        logger.logEvent("Learner Finished!");
    }

    private static Alphabet<String> buildOutputAlphabet(LearnerConfig config) {
        return Alphabets.fromList(config.outputAlphabet);
    }

    private static Alphabet<String> buildInputAlphabet(LearnerConfig config) {
        List<Integer> rowInterval = new LinkedList<Integer>();
        for (int r = config.minRow; r <= config.maxRow; r++)
            rowInterval.add(r);

        List<String> inAlphabet = cartesianProductToString(
                cartesianProductToString(rowInterval, config.readCounts),
                config.bitFlips);
        inAlphabet = inAlphabet.stream().map(t -> "HAMMER(" + t + ")").collect(Collectors.toList());

        return Alphabets.fromList(inAlphabet);
    }

    private static <S,T> List<String> cartesianProductToString(List<S> list1, List<T> list2) {
        return list1
                .stream()
                .map(s1 -> list2.stream().map(s2 -> s1.toString() + "," + s2.toString()))
                .flatMap(Function.identity())
                .collect(Collectors.toList());
    }

    private static EquivalenceOracle<MealyMachine<?, String, ?, String>, String, Word<String>> buildEquivalenceOracle(MembershipOracle<String, Word<String>> queryOracle) {
        return null;
    }

    private static MembershipOracle<String, Word<String>> buildMembershipOracle(MembershipOracle<String, Word<String>> queryOracle) {
        return null;
    }

    private static MembershipOracle<String, Word<String>> buildQueryOracle(Config config) {
        return null;
    }


    private static Config loadConfig(String fileName) throws IOException {
        InputStream is = new FileInputStream(fileName);
        Yaml yaml = new Yaml(new Constructor(Config.class, new LoaderOptions()));
        return yaml.load(is);
    }

    private static void printUsage() {
        System.out.println("Usage: alarm <config_file>");
    }

    private static MealyMachine<?, String, ?, String> learn(LearningAlgorithm<MealyMachine<?,String,?,String>, String, Word<String>> learner,
                                                            EquivalenceOracle<MealyMachine<?, String, ?, String>, String, Word<String>> eqOracle,
                                                            Alphabet<String> inputAlphabet)
            throws IOException {
        int hypCounter = 1;
        boolean done = false;

        learner.startLearning();

        while (!done) {
            // stable hypothesis after membership queries
            MealyMachine<?, String, ?, String> hyp = learner.getHypothesisModel();

            // DotWriter.writeDotFile(hyp, alphabet, hypFileName);

            logger.logEvent("starting equivalence query");

            // search for counterexample
            DefaultQuery<String, Word<String>> o = eqOracle.findCounterExample(hyp, inputAlphabet);
            logger.logEvent("completed equivalence query");

            // no counter example -> learning is done
            if (o == null) {
                done = true;
                continue;
            }
            o = MealyUtil.shortenCounterExample(hyp, o);
            assert o != null;

            hypCounter ++;
            logger.logEvent("Sending counterexample to LearnLib.");
            logger.logCounterexample(o.toString());
            // return counter example to the learner, so that it can use
            // it to generate new membership queries
            learner.refineHypothesis(o);
        }
        return learner.getHypothesisModel();
    }
}
