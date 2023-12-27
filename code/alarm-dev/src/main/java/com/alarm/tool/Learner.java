package com.alarm.tool;

// import de.learnlib.algorithms.adt.learner.ADTLearnerState;

import com.alarm.alphabets.HammerAction;
import com.alarm.alphabets.HammerOutput;
import com.alarm.config.LearnerConfig;
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
import net.automatalib.serialization.dot.GraphDOT;
import net.automatalib.visualization.Visualization;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import net.automatalib.words.impl.Alphabets;

import java.io.IOException;
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

/**
 * Represents a Learner running an active automata learning algorithm on a given SUL.
 *
 * @param <O> the output alphabet type
 */
public class Learner<O extends HammerOutput> {
    // Logger
    private static final LearnLogger logger = LearnLogger.getLogger("ALARM");

    //Objects for counting queries
    private Counter queryCounter;
    private Counter membershipCounter;
    private int equivalenceCounter;

    // Configuration
    private final LearnerConfig learnerConfig;
    private final TestRunner<HammerAction, O> adapter;
    // private static AdapterConfig adapterConfig;
    // private static Config config

    /**
     * Initialises Learner with given configuration and target adapter.
     *
     * @param config  the configuration object containing the learner parameters
     * @param adapter the target adapter that will be queried by the Learner
     */
    public Learner(LearnerConfig config, TestRunner<HammerAction, O> adapter) {
        this.learnerConfig = config;
        this.adapter = adapter;
    }

    /**
     * Runs the selected learning algorithm on the given adepter (see {@link #Learner}).
     *
     * @throws Exception if anything wrong happens during learning
     */
    public void runLearner() throws Exception {
        // Builds input alphabet based on config file
        Alphabet<HammerAction> inputAlphabet = buildInputAlphabet();


        // ZCU104Adapter adaptor = new ZCU104Adapter();
                //new SimpleDRAMAdaptor();
        // Initialises SUL using given adapter
        TestRunnerSUL<HammerAction, O> dramSUL = new TestRunnerSUL<>(adapter);

        // Builds basic query oracle that will be used by both membership and equivalence oracle
        logger.logEvent("Building query oracle...");
        MealyMembershipOracle<HammerAction, O> queryOracle = buildQueryOracle(inputAlphabet, dramSUL);

        // Builds membership oracle
        logger.logEvent("Building membership oracle...");
        MealyMembershipOracle<HammerAction, O> memOracle = buildMembershipOracle(queryOracle);

        // Builds equivalence oracle
        logger.logEvent("Building equivalence oracle...");
        MealyEquivalenceOracle<HammerAction, O> eqOracle =  buildEquivalenceOracle(queryOracle, dramSUL);

        // Builds learner targeting Mealy machines
        logger.logEvent("Building Learner...");
        LearningAlgorithm.MealyLearner<HammerAction, O> learner =
                buildLearner(learnerConfig, inputAlphabet, memOracle);

        // Starts the learning process
        logger.logEvent("Starting learner...");
        MealyMachine<?, HammerAction, ?, O> model = learn(learner, eqOracle, inputAlphabet);

        logger.logEvent("Done.");
        logger.logEvent("Successful run.");

        // Prints out stats
        logger.logStatistic(queryCounter);
        logger.logStatistic(membershipCounter);
        logger.logEvent("Equivalence Queries: " + equivalenceCounter);
        logger.logModel(model);

        logger.logEvent("Learner Finished!");
    }

    /**
     * Builds learner based on the given parameters.
     *
     * @param learnerConfig configuration paramters
     * @param inputAlphabet input alphabet for building queries
     * @param memOracle membership oracle
     * @return a MealyLearner object for learning
     */
    private LearningAlgorithm.MealyLearner<HammerAction, O>
        buildLearner(LearnerConfig learnerConfig, Alphabet<HammerAction> inputAlphabet,
                     MealyMembershipOracle<HammerAction, O> memOracle) {

        return switch(learnerConfig.algorithm) {
                    case TTT -> new TTTLearnerMealyBuilder<HammerAction, O>()
                            .withAlphabet(inputAlphabet)
                            .withOracle(memOracle)
                            .withAnalyzer(AcexAnalyzers.BINARY_SEARCH_FWD)
                            .create();
                    case LSTAR -> new ExtensibleLStarMealyBuilder<HammerAction, O>()
                            .withAlphabet(inputAlphabet)
                            .withOracle(memOracle)
                            .withClosingStrategy(ClosingStrategies.CLOSE_SHORTEST)
                            .withCexHandler(ObservationTableCEXHandlers.CLASSIC_LSTAR)
                            .create();
                };
    }



//    private CompactMealy<HammerAction, O> createTestMachine(Alphabet<HammerAction> inputAlphabet) {
//        return AutomatonBuilders.forMealy(new CompactMealy<HammerAction, O>(inputAlphabet))
//        .from("q0")
//        .on(new HammerAction(1,1,1))
//        .withOutput(new HammerOutput(HammerResult.OK))
//        .to("q0")
//        .withInitial("q0")
//        .create();
//    }

    /**
     * Builds input alphabet based on supplied configuration.
     *
     * @return input alphabet
     */
    private Alphabet<HammerAction> buildInputAlphabet() {
        List<HammerAction> hammerSymbols = new LinkedList<>();

        // Read parameters from config and build input alphabet accordingly
        for (int readCount: learnerConfig.readCounts)
            for (int row: learnerConfig.aggressorRows)
                for (int bitFlip: learnerConfig.bitFlips)
                    hammerSymbols.add(new HammerAction(row, readCount, bitFlip));

        return Alphabets.fromList(hammerSymbols);
    }

    /**
     * Builds query oracle for target SUL. This is the basic query oracle that will be used
     * by both the membership and the query oracle.
     *
     * @param inputAlphabet the input alphabet for building queries
     * @param dramSUL the target SUL
     * @return a MealyMembershipOracle object for querying the target SUL
     */
    private MealyMembershipOracle<HammerAction, O>
        buildQueryOracle(Alphabet<HammerAction> inputAlphabet,
                         TestRunnerSUL<HammerAction, O> dramSUL) {

        // Creates basic SUL oracle for issuing single tests to dramSUL
        TestRunnerSULOracle<HammerAction, O> dramSULOracle = new TestRunnerSULOracle<>(dramSUL);

        // Adds sampling oracle which issues several tests per query and computes output using sampling
        SamplingSULOracle<HammerAction, O> samplingSULOracle =
                    new SamplingSULOracle<>(learnerConfig.runsPerQuery,
                            learnerConfig.samplingThreshold, dramSULOracle, new PrintWriter(System.out));

        // IGNORE
        // MealyMembershipOracle<HammerAction, HammerResult> samplingSULOracle = dramSULOracle;

        // Introduces counter for SUL queries
        MealyCounterOracle<HammerAction, O> counterOracle =
                new MealyCounterOracle<>(samplingSULOracle , "Queries to SUL");
        queryCounter = counterOracle.getCounter();

        // Add ObservationTree-based cache such that HammerResult.FLIP always leads to a sink
        // HashSet<O> terminalSymbols = getTerminalSymbols();

        return new CachingSULOracle<>(counterOracle, new ObservationTree<>());
    }


    /**
     * Builds equivalence oracle for target SUL.
     *
     * @param queryOracle the query oracle that will be used to implement queries
     * @param sul the target SUL
     * @return a MealyEquivalenceOracle object for issuing equivalence queries
     */
    private MealyEquivalenceOracle<HammerAction, O>
        buildEquivalenceOracle(MealyMembershipOracle<HammerAction, O>  queryOracle,
                               SUL<HammerAction, O> sul) {

        MealyEquivalenceOracle<HammerAction, O> eqOracle =
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

    /**
     * Builds membership oracle for target SUL.
     *
     * @param queryOracle the query oracle that will be used to implement queries
     * @return a MealyMembershipOracle object for issuing membership queries
     */
    private MealyMembershipOracle<HammerAction, O>
        buildMembershipOracle(MealyMembershipOracle<HammerAction, O> queryOracle) {
        MealyCounterOracle<HammerAction, O> memOracle = new MealyCounterOracle<>(queryOracle, "Membership Queries");
        membershipCounter = memOracle.getCounter();

        return memOracle;
    }







    private MealyMachine<?, HammerAction, ?, O>
        learn(LearningAlgorithm.MealyLearner<HammerAction, O> learner,
              MealyEquivalenceOracle<HammerAction, O> eqOracle,
              Alphabet<HammerAction> inputAlphabet) throws IOException {

        int hypCounter = 1;
        boolean done = false;

        learner.startLearning();

        MealyMachine<?, HammerAction, ?, O> hyp;
        while (!done) {
            // stable hypothesis after membership queries
            hyp = learner.getHypothesisModel();

            if (learnerConfig.visualiseAllHypotheses) {
                Visualization.visualize(hyp.transitionGraphView(inputAlphabet), true);
                System.out.println("Hyp");
            }

            logger.logEvent("starting equivalence query");

            // search for counterexample
            DefaultQuery<HammerAction, Word<O>> o = eqOracle.findCounterExample(hyp, inputAlphabet);
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

    private void writeDOT(MealyMachine<?,HammerAction,?, O> automaton,
                                 Alphabet<HammerAction> inputAlphabet) {
       try(PrintWriter dotWriter = new PrintWriter(learnerConfig.dotOutputPath + "A" + new Date().getTime() + ".dot")) {
           GraphDOT. write(automaton.transitionGraphView(inputAlphabet), dotWriter);
       }
       catch(IOException ignored){}
    }
}
