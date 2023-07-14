package com.alarm.tool;

import de.learnlib.algorithms.adt.learner.ADTLearnerState;
import de.learnlib.algorithms.lstar.mealy.ExtensibleLStarMealyBuilder;
import de.learnlib.algorithms.ttt.mealy.TTTLearnerMealyBuilder;
import de.learnlib.api.SUL;
import de.learnlib.api.algorithm.LearningAlgorithm;
import de.learnlib.api.logging.LearnLogger;
import de.learnlib.api.oracle.EquivalenceOracle;
import de.learnlib.api.oracle.MembershipOracle;
import de.learnlib.api.query.DefaultQuery;
import de.learnlib.drivers.reflect.MethodInput;
import de.learnlib.drivers.reflect.MethodOutput;
import de.learnlib.filter.cache.mealy.MealyCacheOracle;
import de.learnlib.filter.cache.mealy.MealyCaches;
import de.learnlib.filter.statistic.Counter;
import de.learnlib.filter.statistic.oracle.MealyCounterOracle;
import de.learnlib.filter.statistic.sul.ResetCounterSUL;
import de.learnlib.oracle.equivalence.MealyRandomWpMethodEQOracle;
import de.learnlib.oracle.equivalence.MealyWpMethodEQOracle;
import de.learnlib.oracle.equivalence.mealy.RandomWalkEQOracle;
import de.learnlib.oracle.membership.SULOracle;
import de.learnlib.oracle.membership.SimulatorOracle;
import de.learnlib.util.mealy.MealyUtil;
import net.automatalib.automata.transducers.MealyMachine;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import net.automatalib.words.impl.Alphabets;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.Collectors;

// TODO:
//  - take inspiration from https://github.com/PROGNOSISTool/learner/blob/main/src/learner/Main.java
//  - see other examples here https://github.com/LearnLib/cav2015-example/tree/master/src/main/java/de/learnlib/example/cav2015/coffee
//  - Structure:
//      - DRAMAdapter (implementing SUL) to communicate to concrete DRAM
//          - Start implementing Adapter for SyntheticDRAM
//      - MembershipQueryOracle taking DRAMAdapter
//      - Membership and Equivalence Oracle defined on top of query oracle
//  - How to implement SUL? Docker container or regular Java wrapper?
//      - Advantage of Docker: SUL can be implemented in any language, it can run daemon etc -- useful for rowhammer-tester?
public class Main {
    // Logger
    private static final LearnLogger logger = LearnLogger.getLogger("ALARM");

    //Objects for counting queries
    private static Counter queryCounter;
    private static Counter membershipCounter;
    private static int equivalenceCounter;

    // Configuration
    // private static Config config



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
        LearnerConfig learnerConfig = config.learnerConfig;
        Alphabet<HammerSymbol> inputAlphabet = buildInputAlphabet(learnerConfig);
        Alphabet<String> outputAlphabet = buildOutputAlphabet(learnerConfig);

        // TODO: Use real SUL
        SUL<HammerSymbol, String> dramSUL = null;

        logger.logEvent("Building query oracle...");
        MembershipOracle.MealyMembershipOracle<HammerSymbol, String> queryOracle =
                buildQueryOracle(inputAlphabet, dramSUL);

        logger.logEvent("Building membership oracle...");
        MembershipOracle.MealyMembershipOracle<HammerSymbol, String> memOracle =
                buildMembershipOracle(queryOracle);

        logger.logEvent("Building equivalence oracle...");
        EquivalenceOracle.MealyEquivalenceOracle<HammerSymbol, String> eqOracle =
                buildEquivalenceOracle(queryOracle, learnerConfig, dramSUL);

        logger.logEvent("Building Learner...");
        LearningAlgorithm.MealyLearner<HammerSymbol, String> learner =
                buildLearner(learnerConfig, inputAlphabet, memOracle);


        System.out.println(inputAlphabet);


//        learning = false;
//        TTTLearnerMealy<String, String> learner = new TTTLearnerMealyBuilder<String, String>()
//                .withAlphabet(alphabet)
//                .withOracle(memOracle)
//                .create();
//
        logger.logEvent("Starting learner...");
        MealyMachine<?, HammerSymbol, ?, String> model = learn(learner, eqOracle, inputAlphabet);
//
//        // final output to out.txt
        logger.logEvent("Done.");
        logger.logEvent("Successful run.");

        logger.logStatistic(queryCounter);
        logger.logStatistic(membershipCounter);
        logger.logEvent("Equivalence Queries: " + equivalenceCounter);
        logger.logModel(model);

        logger.logEvent("Learner Finished!");
    }

    private static LearningAlgorithm.MealyLearner<HammerSymbol, String>
        buildLearner(LearnerConfig learnerConfig, Alphabet<HammerSymbol> inputAlphabet,
                     MembershipOracle.MealyMembershipOracle<HammerSymbol,
                     String> memOracle) {

        return switch(learnerConfig.algorithm) {
                    case TTT -> new TTTLearnerMealyBuilder<HammerSymbol, String>()
                            .withAlphabet(inputAlphabet)
                            .withOracle(memOracle)
                            .create();
                    case LSTAR -> new ExtensibleLStarMealyBuilder<HammerSymbol, String>()
                            .withAlphabet(inputAlphabet)
                            .withOracle(memOracle)
                            .create();
                };
    }

    private static Alphabet<String> buildOutputAlphabet(LearnerConfig config) {
        return Alphabets.fromList(config.outputAlphabet);
    }

    private static Alphabet<HammerSymbol> buildInputAlphabet(LearnerConfig config) {
        List<HammerSymbol> hammerSymbols = new LinkedList<HammerSymbol>();

        for (int readCount: config.readCounts)
            for (int row = config.minRow; row <= config.maxRow; row++)
                for (int bitFlip: config.bitFlips)
                    hammerSymbols.add(new HammerSymbol(readCount, row, bitFlip));

        return Alphabets.fromList(hammerSymbols);
    }

    private static MembershipOracle.MealyMembershipOracle<HammerSymbol, String>
        buildQueryOracle(Alphabet<HammerSymbol> inputAlphabet, SUL<HammerSymbol,String> sul) {

        SULOracle<HammerSymbol, String> sulOracle = new SULOracle<>(sul);

        // Introduce counter for SUL queries
        MealyCounterOracle<HammerSymbol, String> counterOracle =
                new MealyCounterOracle<>(sulOracle, "Queries to SUL");
        queryCounter = counterOracle.getCounter();

        // TODO: Handle non-determinism

        // Add cache
        return MealyCaches.createCache(inputAlphabet, counterOracle);
    }

    private static EquivalenceOracle.MealyEquivalenceOracle<HammerSymbol, String>
        buildEquivalenceOracle(MembershipOracle.MealyMembershipOracle<HammerSymbol, String>  queryOracle,
                               LearnerConfig config,
                               SUL<HammerSymbol, String> sul) {

        EquivalenceOracle.MealyEquivalenceOracle<HammerSymbol, String> eqOracle =
                switch(config.eqOracle) {
                     case RANDOM_WALK -> new RandomWalkEQOracle<>(sul, config.resetProbability, config.eqOracleMaxSteps,
                    false, new Random(config.randomSeed));
                     case WP_METHOD -> new MealyWpMethodEQOracle<>(queryOracle, config.eqOracleMaxSteps);
                     case RANDOM_WP_METHOD -> new MealyRandomWpMethodEQOracle<>(queryOracle, 1,
                             config.eqOracleMaxSteps);
        };

        equivalenceCounter = 0;
        return eqOracle;
    }

    private static MembershipOracle.MealyMembershipOracle<HammerSymbol, String>
        buildMembershipOracle(MembershipOracle.MealyMembershipOracle<HammerSymbol, String> queryOracle) {

        return new MealyCounterOracle<HammerSymbol, String>(queryOracle, "Membership Queries");
    }




    private static Config loadConfig(String fileName) throws IOException {
        InputStream is = new FileInputStream(fileName);
        Yaml yaml = new Yaml(new Constructor(Config.class, new LoaderOptions()));
        return yaml.load(is);
    }

    private static void printUsage() {
        System.out.println("Usage: alarm <config_file>");
    }

    private static MealyMachine<?, HammerSymbol, ?, String>
        learn(LearningAlgorithm.MealyLearner<HammerSymbol, String> learner,
              EquivalenceOracle.MealyEquivalenceOracle<HammerSymbol, String> eqOracle,
              Alphabet<HammerSymbol> inputAlphabet) throws IOException {

        int hypCounter = 1;
        boolean done = false;

        learner.startLearning();

        while (!done) {
            // stable hypothesis after membership queries
            MealyMachine<?, HammerSymbol, ?, String> hyp = learner.getHypothesisModel();

            // DotWriter.writeDotFile(hyp, alphabet, hypFileName);

            logger.logEvent("starting equivalence query");

            // search for counterexample
            DefaultQuery<HammerSymbol, Word<String>> o = eqOracle.findCounterExample(hyp, inputAlphabet);
            logger.logEvent("completed equivalence query");

            // no counter example -> learning is done
            if (o == null) {
                done = true;
                continue;
            }
            o = MealyUtil.shortenCounterExample(hyp, o);
            assert o != null;
            equivalenceCounter++;

//            hypCounter ++;
//            logger.logEvent("Sending counterexample to LearnLib.");
//            logger.logCounterexample(o.toString());
            // return counter example to the learner, so that it can use
            // it to generate new membership queries
            learner.refineHypothesis(o);
        }
        return learner.getHypothesisModel();
    }
}
