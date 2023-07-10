package com.alarm.tool;

import com.alarm.utils.Counter;
import de.learnlib.algorithms.ttt.mealy.TTTLearnerMealy;
import de.learnlib.api.logging.LearnLogger;
import de.learnlib.api.oracle.MembershipOracle;
import net.automatalib.automata.transducers.MealyMachine;
import net.automatalib.words.Word;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.*;
import java.nio.file.Files;

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
    public static void main(String[] args) {
        if (args.length == 0) {
            printUsage();
            System.exit(-1);
        }

        try {
            Config alarmConfig = loadConfig(args[0]);
            runLearner(alarmConfig);
            // System.out.println(alarmConfig.learnerConfig.randomSeed);
        }
        catch(IOException e) {
            System.err.println("Error reading configuration file.");
            printUsage();
            System.exit(-1);
        }
    }

    private static void runLearner(Config alarmConfig) {
        logger.logEvent("Building query oracle...");
        MembershipOracle<String, Word<String>> queryOracle = buildQueryOracle(config);

        logger.logEvent("Building membership oracle...");
        MembershipOracle<String, Word<String>> memOracle = buildMembershipOracle(queryOracle);

        logger.logEvent("Building equivalence oracle...");
        EquivalenceOracle<MealyMachine<?, String, ?, String>, String, Word<String>> eqOracle = buildEquivalenceOracle(queryOracle);

        logger.logEvent("Building Learner...");
        learning = false;
        TTTLearnerMealy<String, String> learner = new TTTLearnerMealyBuilder<String, String>()
                .withAlphabet(alphabet)
                .withOracle(memOracle)
                .create();

        logger.logEvent("Starting learner...");
        MealyMachine<?, String, ?, String> model = learn(learner, eqOracle);

        // final output to out.txt
        logger.logEvent("Done.");
        logger.logEvent("Successful run.");

        logger.logStatistic(queryCounter);
        logger.logStatistic(membershipCounter);
        logger.logStatistic(equivalenceCounter);
        logger.logModel(model);

        logger.logEvent("Learner Finished!");
    }


    private static Config loadConfig(String fileName) throws IOException {
        InputStream is = new FileInputStream(fileName);
        Yaml yaml = new Yaml(new Constructor(Config.class, new LoaderOptions()));
        return yaml.load(is);
    }

    private static void printUsage() {
        System.out.println("Usage: alarm <config_file>");
    }
}
