package com.alarm.tool;

// import de.learnlib.algorithms.adt.learner.ADTLearnerState;

import com.alarm.adapter.zcu104.ZCU104Adapter;
import com.alarm.alphabets.HammerAction;
import com.alarm.alphabets.HammerResult;
import com.alarm.config.AdapterConfig;
import com.alarm.config.Config;
import com.alarm.config.LearnerConfig;
import com.alarm.examples.SimpleDRAMAdaptor;
import com.alarm.oracles.*;
import de.learnlib.acex.analyzers.AcexAnalyzers;
import de.learnlib.algorithms.lstar.ce.ObservationTableCEXHandlers;
import de.learnlib.algorithms.lstar.closing.ClosingStrategies;
import de.learnlib.algorithms.lstar.mealy.ExtensibleLStarMealyBuilder;
import de.learnlib.algorithms.ttt.mealy.TTTLearnerMealyBuilder;
import de.learnlib.api.SUL;
import de.learnlib.api.algorithm.LearningAlgorithm;
import de.learnlib.api.logging.LearnLogger;
import de.learnlib.api.oracle.EquivalenceOracle.MealyEquivalenceOracle;
import de.learnlib.api.oracle.MembershipOracle.MealyMembershipOracle;
import de.learnlib.api.query.DefaultQuery;
import de.learnlib.filter.statistic.Counter;
import de.learnlib.filter.statistic.oracle.MealyCounterOracle;
import de.learnlib.oracle.equivalence.MealyRandomWpMethodEQOracle;
import de.learnlib.oracle.equivalence.MealyWpMethodEQOracle;
import de.learnlib.oracle.equivalence.mealy.RandomWalkEQOracle;
import de.learnlib.util.mealy.MealyUtil;
import net.automatalib.automata.transducers.MealyMachine;
import net.automatalib.automata.transducers.impl.compact.CompactMealy;
import net.automatalib.serialization.dot.GraphDOT;
import net.automatalib.util.automata.builders.AutomatonBuilders;
import net.automatalib.visualization.Visualization;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import net.automatalib.words.impl.Alphabets;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.*;

// TODO:
//  - take inspiration from https://github.com/PROGNOSISTool/learner/blob/main/src/learner/Main.java
//  - see other examples here https://github.com/LearnLib/cav2015-example/tree/master/src/main/java/de/learnlib/example/cav2015/coffee
//  - Structure:
//      - SimpleDRAMAdapter for experiments
//          - Note that, setting a probability of flips to X, the likelihood of most frequent answer will be X
//            which could be lower than the SamplingSULOracle threshold, causing it to diverge.
//          - In reality, flips are highly likely once a certain threshold (rowhammer threshold) has been reached;
//            However: what if we need to set a specific number of flips? Probability will be related to readcount
//      [X] DRAMAdapter (implementing SUL) to communicate to concrete DRAM
//          [X] Start implementing Adapter for SyntheticDRAM
//          - Adapter initialises memory depending on the number of intended bit flips
//            (e.g., pattern has three 1s if we want to flip 3 bits)
//              - This is hard: typically one uses modular patterns, or "striped" patterns (inverted every n rows)
//                Rowhammer tester heavily relies on those.
//          - Handle true/anti-cells: papers claim that each row is made of the same type of cells
//              - Modify script to show direction of bit flips (1->0 or 0->1)
//       [X] Caching: do not re-run tests for prefixes if answer if known
//  - How to implement SUL? Docker container or regular Java wrapper?
//      - Advantage of Docker: SUL can be implemented in any language, it can run daemon etc -- useful for rowhammer-tester?
//

public class Main {
    // Logger
    private static final LearnLogger logger = LearnLogger.getLogger("ALARM");

    //Objects for counting queries
    private static Counter queryCounter;
    private static Counter membershipCounter;
    private static int equivalenceCounter;

    // Configuration
    private static LearnerConfig learnerConfig;
    private static AdapterConfig adapterConfig;
    // private static Config config



    public static void main(String[] args) {
        if (args.length == 0) {
            printUsage();
            System.exit(-1);
        }

        try {
            loadConfig(args[0]);
            // System.out.println(alarmConfig.learnerConfig.randomSeed);
        }
        catch(IOException e) {
            System.err.println("Error reading configuration file.");
            printUsage();
            System.exit(-1);
        }

        try { runLearner(); }
        catch(Exception e) {
            System.err.println("Error whilst learning.");
            // logger.error(e.getMessage());
            e.printStackTrace();
            System.exit(-1);
        }
    }

    private static void runLearner() throws Exception {
        Alphabet<HammerAction> inputAlphabet = buildInputAlphabet(learnerConfig);


        ZCU104Adapter adaptor = new ZCU104Adapter();
                //new SimpleDRAMAdaptor();
        TestRunnerSUL<HammerAction, HammerResult> dramSUL = new TestRunnerSUL<>(adaptor);

        logger.logEvent("Building query oracle...");
        MealyMembershipOracle<HammerAction, HammerResult> queryOracle = buildQueryOracle(inputAlphabet, dramSUL);

        logger.logEvent("Building membership oracle...");
        MealyMembershipOracle<HammerAction, HammerResult> memOracle = buildMembershipOracle(queryOracle);

        logger.logEvent("Building equivalence oracle...");
        MealyEquivalenceOracle<HammerAction, HammerResult> eqOracle =  buildEquivalenceOracle(queryOracle, dramSUL);

        logger.logEvent("Building Learner...");
        LearningAlgorithm.MealyLearner<HammerAction, HammerResult> learner =
                buildLearner(learnerConfig, inputAlphabet, memOracle);

        logger.logEvent("Starting learner...");
        MealyMachine<?, HammerAction, ?, HammerResult> model = learn(learner, eqOracle, inputAlphabet);

        logger.logEvent("Done.");
        logger.logEvent("Successful run.");

        logger.logStatistic(queryCounter);
        logger.logStatistic(membershipCounter);
        logger.logEvent("Equivalence Queries: " + equivalenceCounter);
        logger.logModel(model);

        logger.logEvent("Learner Finished!");
    }

    private static LearningAlgorithm.MealyLearner<HammerAction, HammerResult>
        buildLearner(LearnerConfig learnerConfig, Alphabet<HammerAction> inputAlphabet,
                     MealyMembershipOracle<HammerAction, HammerResult> memOracle) {

        return switch(learnerConfig.algorithm) {
                    case TTT -> new TTTLearnerMealyBuilder<HammerAction, HammerResult>()
                            .withAlphabet(inputAlphabet)
                            .withOracle(memOracle)
                            .withAnalyzer(AcexAnalyzers.BINARY_SEARCH_FWD)
                            .create();
                    case LSTAR -> new ExtensibleLStarMealyBuilder<HammerAction, HammerResult>()
                            .withAlphabet(inputAlphabet)
                            .withOracle(memOracle)
                            .withClosingStrategy(ClosingStrategies.CLOSE_SHORTEST)
                            .withCexHandler(ObservationTableCEXHandlers.CLASSIC_LSTAR)
                            .create();
                };
    }



    private static CompactMealy<HammerAction, HammerResult> createTestMachine(Alphabet<HammerAction> inputAlphabet) {
        return AutomatonBuilders.forMealy(new CompactMealy<HammerAction, HammerResult>(inputAlphabet))
        .from("q0")
        .on(new HammerAction(1,1,1))
        .withOutput(HammerResult.OK)
        .to("q0")
        .withInitial("q0")
        .create();
    }

    private static Alphabet<HammerAction> buildInputAlphabet(LearnerConfig config) {
        List<HammerAction> hammerSymbols = new LinkedList<HammerAction>();

        // Read parameters from config and build input alphabet accordingly
        for (int readCount: config.readCounts)
            for (int row = config.minRow; row <= config.maxRow; row++)
                for (int bitFlip: config.bitFlips)
                    hammerSymbols.add(new HammerAction(row, readCount, bitFlip));

        return Alphabets.fromList(hammerSymbols);
    }

    private static MealyMembershipOracle<HammerAction, HammerResult>
        buildQueryOracle(Alphabet<HammerAction> inputAlphabet,
                         TestRunnerSUL<HammerAction, HammerResult> dramSUL) {

        TestRunnerSULOracle<HammerAction, HammerResult> dramSULOracle = new TestRunnerSULOracle<>(dramSUL);

        // Add sampling oracle
        SamplingSULOracle<HammerAction, HammerResult> samplingSULOracle =
                    new SamplingSULOracle<>(learnerConfig.runsPerQuery,
                            learnerConfig.samplingThreshold, dramSULOracle, new PrintWriter(System.out));
        // MealyMembershipOracle<HammerAction, HammerResult> samplingSULOracle = dramSULOracle;

        // Introduce counter for SUL queries
        MealyCounterOracle<HammerAction, HammerResult> counterOracle =
                new MealyCounterOracle<>(samplingSULOracle , "Queries to SUL");
        queryCounter = counterOracle.getCounter();

        // Add ObservationTree-based cache such that HammerResult.FLIP always leads to a sink
        return new CachingSULOracle<>(counterOracle, new ObservationTree<HammerAction, HammerResult>(), HammerResult.FLIP);
    }

    private static MealyEquivalenceOracle<HammerAction, HammerResult>
        buildEquivalenceOracle(MealyMembershipOracle<HammerAction, HammerResult>  queryOracle,
                               SUL<HammerAction, HammerResult> sul) {

        MealyEquivalenceOracle<HammerAction, HammerResult> eqOracle =
                switch(learnerConfig.eqOracle) {
                     case RANDOM_WALK -> new RandomWalkEQOracle<>(sul, learnerConfig.resetProbability, learnerConfig.eqOracleMaxSteps,
                    false, new Random(learnerConfig.randomSeed));
                     case WP_METHOD -> new MealyWpMethodEQOracle<>(queryOracle, learnerConfig.eqOracleMaxSteps);
                     case RANDOM_WP_METHOD -> new MealyRandomWpMethodEQOracle<>(queryOracle, 1,
                             learnerConfig.eqOracleMaxSteps);
        };

        equivalenceCounter = 0;
        return eqOracle;
    }

    private static MealyMembershipOracle<HammerAction, HammerResult>
        buildMembershipOracle(MealyMembershipOracle<HammerAction, HammerResult> queryOracle) {
        MealyCounterOracle<HammerAction, HammerResult> memOracle = new MealyCounterOracle<>(queryOracle, "Membership Queries");
        membershipCounter = memOracle.getCounter();

        return memOracle;
    }



    // Load config parameters from provided yaml file
    private static void loadConfig(String fileName) throws IOException {
        InputStream is = new FileInputStream(fileName);
        Yaml yaml = new Yaml(new Constructor(Config.class, new LoaderOptions()));
        Config config = yaml.load(is);
        learnerConfig = config.learnerConfig;
        adapterConfig = config.adapterConfig;
    }

    private static void printUsage() {
        System.out.println("Usage: alarm <config_file>");
    }

    private static MealyMachine<?, HammerAction, ?, HammerResult>
        learn(LearningAlgorithm.MealyLearner<HammerAction, HammerResult> learner,
              MealyEquivalenceOracle<HammerAction, HammerResult> eqOracle,
              Alphabet<HammerAction> inputAlphabet) throws IOException {

        int hypCounter = 1;
        boolean done = false;

        learner.startLearning();

        MealyMachine<?, HammerAction, ?, HammerResult> hyp;
        while (!done) {
            // stable hypothesis after membership queries
            hyp = learner.getHypothesisModel();

            if (learnerConfig.visualiseAllHypotheses) {
                Visualization.visualize(hyp.transitionGraphView(inputAlphabet), true);
                System.out.println("Hyp");
            }

            logger.logEvent("starting equivalence query");

            // search for counterexample
            DefaultQuery<HammerAction, Word<HammerResult>> o = eqOracle.findCounterExample(hyp, inputAlphabet);
            logger.logEvent("completed equivalence query");
            equivalenceCounter++;

            // no counter example -> learning is done
            if (o == null) {
                done = true;
                continue;
            }
            o = MealyUtil.shortenCounterExample(hyp, o);
            assert o != null;

            // return counter example to the learner, so that it can use
            // it to generate new membership queries
            learner.refineHypothesis(o);
        }
        hyp = learner.getHypothesisModel();

        if (learnerConfig.visualiseLearntModel) {
            Visualization.visualize(hyp.transitionGraphView(inputAlphabet), true);
            writeDOT(hyp, inputAlphabet);
        }

        return hyp;
    }

    private static void writeDOT(MealyMachine<?,HammerAction,?,HammerResult> automaton,
                                 Alphabet<HammerAction> inputAlphabet) {
       try(PrintWriter dotWriter = new PrintWriter(learnerConfig.dotOutputPath + "A" + new Date().getTime() + ".dot")) {
           GraphDOT. write(automaton.transitionGraphView(inputAlphabet), dotWriter);
       }
       catch(IOException e){}
    }
}
