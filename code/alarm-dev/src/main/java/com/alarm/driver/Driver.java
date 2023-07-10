package com.alarm.driver;

import java.io.IOException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import com.alarm.tool.Learner;

/**
 * This is a driver class to run the learning process by getting arguments from
 * the user.
 *
 * @author ########
 */

public class Driver {
	public static String appName = "RH Tool";

	public static void main(String args[]) throws NoSuchMethodException, NoSuchFieldException, SecurityException,
			IllegalArgumentException, IllegalAccessException, IOException {

		Options options = new Options();

		int learning_algorithm = -1;
		int eq_oracle = -1;
		int memory_size = -1;
		int trr_counters = -1;
		int max_bits_to_flip = -1;
		int number_of_accesses = -1;
		int trr_threshold = -1;
		int rh_threshold = -1;
		int refresh_interval = -1;
		boolean visualise = false;
		int max_steps = -1;
		boolean ecc_status = true;
		int test_factor = -1;

		Option input_help = new Option("h", "help", false, "Help");
		Option input_learning_algorithm = new Option("l", "learning", true, "Learning Algorthm (options: ttt, lstar)");
		Option input_eq_oracle = new Option("o", "oracle", true, "Equivalence Oracle (options: rw, wp, rwp)");
		Option input_memory_size = new Option("m", "memory-size", true,
				"Number of rows inside the memory (options: a non-negative number)");
		Option input_trr_counters = new Option("t", "trr-counters", true,
				"Number of counters in TRR (options: a non-negative number)");
		Option input_max_bits_to_flip = new Option("b", "bits", true,
				"Maximum number of bits to get flipped in a memory rows (options: a non-negative number)");
		Option input_number_of_accesses = new Option("n", "number", true,
				"Number of accesses to a row in each step (options: a non-negative number)");
		Option input_trr_threshold = new Option("s", "trr-threshold", true,
				"Minimum number of accesses rquired to a row in an interval to trigger TRR (options: a non-negative number)");
		Option input_rh_threshold = new Option("r", "rh-threshold", true,
				"Minimum number of accesses rquired to adjacent rows in an interval to trigger a possible bit flip in a row (options: a non-negative number)");
		Option input_refresh_interval = new Option("i", "interval", true,
				"Minimum number of read/write rquired to issue a refresh to all the memory rows (options: a non-negative number)");
		Option input_visualise = new Option("v", "visualise", false, "Visualise and show the output automata");
		Option input_max_steps = new Option("x", "steps", true,
				"Maximum number of steps for the Random Walk Eq Oracle (options: a positive number)");
		Option input_plotter = new Option("p", "plotter", false, "Run the plotter and plot the figures");
		Option input_plotter_minimal = new Option("a", "plotter-minimal", false,
				"Run the plotter in a minimal way without averaging and plot the figures");
		Option input_ecc_status = new Option("e", "ecc-off", false, "Turn off ECC in the memory");
		Option input_test_factor = new Option("f", "factor", true, "Select a specific parameter to run the plotter against it (options: a positive number in [0,7])");

		input_help.setRequired(false);
		input_learning_algorithm.setRequired(false);
		input_eq_oracle.setRequired(false);
		input_memory_size.setRequired(false);
		input_trr_counters.setRequired(false);
		input_max_bits_to_flip.setRequired(false);
		input_number_of_accesses.setRequired(false);
		input_trr_threshold.setRequired(false);
		input_rh_threshold.setRequired(false);
		input_refresh_interval.setRequired(false);
		input_visualise.setRequired(false);
		input_max_steps.setRequired(false);
		input_plotter.setRequired(false);
		input_plotter_minimal.setRequired(false);
		input_ecc_status.setRequired(false);
		input_test_factor.setRequired(false);

		options.addOption(input_help);
		options.addOption(input_learning_algorithm);
		options.addOption(input_eq_oracle);
		options.addOption(input_memory_size);
		options.addOption(input_trr_counters);
		options.addOption(input_max_bits_to_flip);
		options.addOption(input_number_of_accesses);
		options.addOption(input_trr_threshold);
		options.addOption(input_rh_threshold);
		options.addOption(input_refresh_interval);
		options.addOption(input_visualise);
		options.addOption(input_max_steps);
		options.addOption(input_plotter);
		options.addOption(input_plotter_minimal);
		options.addOption(input_ecc_status);
		options.addOption(input_test_factor);

		CommandLineParser clp = new DefaultParser();
		HelpFormatter hf = new HelpFormatter();
		CommandLine cmd = null;

		try {
			cmd = clp.parse(options, args);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			hf.printHelp(appName, options);
			System.exit(0);
		}

		ecc_status = !cmd.hasOption("ecc-off");

		if (cmd.hasOption("help")) {
			hf.printHelp(appName, options);
			System.exit(0);
		}

		test_factor = (cmd.getOptionValue("factor") != null) ? Integer.valueOf(cmd.getOptionValue("factor"))
				: test_factor;

		if (cmd.hasOption("plotter")) {
			Plotter.run(ecc_status, test_factor);
			System.exit(0);
		}
		if (cmd.hasOption("plotter-minimal")) {
			Plotter.runMinimal(ecc_status, test_factor);
			System.exit(0);
		}

		learning_algorithm = (cmd.getOptionValue("learning") != null) ? Integer.valueOf(cmd.getOptionValue("learning"))
				: learning_algorithm;
		eq_oracle = (cmd.getOptionValue("oracle") != null) ? Integer.valueOf(cmd.getOptionValue("oracle")) : eq_oracle;
		memory_size = (cmd.getOptionValue("momory-size") != null) ? Integer.valueOf(cmd.getOptionValue("momory-size"))
				: memory_size;
		trr_counters = (cmd.getOptionValue("trr-counters") != null)
				? Integer.valueOf(cmd.getOptionValue("trr-counters"))
				: trr_counters;
		max_bits_to_flip = (cmd.getOptionValue("bits") != null) ? Integer.valueOf(cmd.getOptionValue("bits"))
				: max_bits_to_flip;
		number_of_accesses = (cmd.getOptionValue("number") != null) ? Integer.valueOf(cmd.getOptionValue("number"))
				: number_of_accesses;
		trr_threshold = (cmd.getOptionValue("trr-threshold") != null)
				? Integer.valueOf(cmd.getOptionValue("trr-threshold"))
				: trr_threshold;
		rh_threshold = (cmd.getOptionValue("rh-threshold") != null)
				? Integer.valueOf(cmd.getOptionValue("rh-threshold"))
				: rh_threshold;
		refresh_interval = (cmd.getOptionValue("interval") != null) ? Integer.valueOf(cmd.getOptionValue("interval"))
				: refresh_interval;
		max_steps = (cmd.getOptionValue("steps") != null) ? Integer.valueOf(cmd.getOptionValue("steps")) : max_steps;

		visualise = cmd.hasOption("visualise");

		Learner.run(learning_algorithm, eq_oracle, memory_size, trr_counters, max_bits_to_flip, number_of_accesses,
				trr_threshold, rh_threshold, refresh_interval, visualise, max_steps, ecc_status);

	}

}
