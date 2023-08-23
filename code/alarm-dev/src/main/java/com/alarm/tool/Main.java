package com.alarm.tool;

import com.alarm.adapter.zcu104.ZCU104Adapter;
import com.alarm.alphabets.HammerRowsOutput;
import com.alarm.config.AdapterConfig;
import com.alarm.config.Config;
import com.alarm.config.LearnerConfig;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class Main {
    private static LearnerConfig learnerConfig;
    private static AdapterConfig adapterConfig;

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

        ZCU104Adapter adapter = null;
        try {
            adapter = new ZCU104Adapter();
        }
        catch(IOException ex) {
            ex.printStackTrace();
            System.exit(-1);
        }
        Learner<HammerRowsOutput> learner = new Learner<>(learnerConfig, adapter);

        try { learner.runLearner(); }
        catch(Exception e) {
            System.err.println("Error whilst learning.");
            // logger.error(e.getMessage());
            e.printStackTrace();
            System.exit(-1);
        }
    }

    private static void printUsage() {
        System.out.println("Usage: alarm <config_file>");
    }

    // Load config parameters from provided yaml file
    private static void loadConfig(String fileName) throws IOException {
        InputStream is = new FileInputStream(fileName);
        Yaml yaml = new Yaml(new Constructor(Config.class, new LoaderOptions()));
        Config config = yaml.load(is);
        learnerConfig = config.learnerConfig;
        adapterConfig = config.adapterConfig;
    }
}
