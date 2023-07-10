package com.alarm.driver;

import java.io.File;
import java.io.IOException;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

import com.alarm.tool.Learner;

/**
 * This is a plotter class to run the learning process by different arguments
 * and plot them.
 *
 * @author ########
 */

public class Plotter {
	public static String appName = "ALRAM";

	public static void run(boolean ecc_status, int test_factor) throws NoSuchMethodException, NoSuchFieldException,
			SecurityException, IllegalArgumentException, IllegalAccessException, IOException {

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
		String key = "RunTime";
		int number_of_factors = 8;
		int rounds = 5;

		String output[] = { "mem_size", "trr_counters", "max_bits", "num_accesses", "trr_threshold", "rh_threshold",
				"refresh_interval", "max_steps" };
		String title[] = { "Memory Size Measurement", "TRR Counters Measurement",
				"Maximum Number of Bits to Flip Measurement", "Number of Accesses Measurement",
				"TRR Threshold Measurement", "Rowhammer Threshold Measurement", "Refresh Interval Measurement",
				"Maximum Number of Steps Measurement" };
		String xTitle[] = { "Memory Size", "TRR Counters", "Max Bits to Flip", "Number of Accesses", "TRR Threshold",
				"Rowhammer Threshold", "Refresh Interval", "Maximum Number of Steps" };
		String yTitle = "Run_Time(s)";

		int data[][] = { { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 }, // mem_size
				{ 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 }, // trr_counters
				{ 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 }, // max_bits
				{ 100, 200, 300, 400, 500, 1000, 1500, 2000, 3000, 5000 }, // num_accesses
				{ 200, 300, 400, 500, 1000, 1500, 2000, 3000, 5000, 20000 }, // trr_threshold
				{ 200, 300, 400, 500, 1000, 1500, 2000, 3000, 5000, 20000 }, // rh_threshold
				{ 200, 300, 400, 500, 1000, 2000, 3000, 5000, 10000, 20000 }, // refresh_interval
				{ 50, 100, 200, 300, 500, 1000, 2000, 3000, 5000, 10000 } // max_steps
		};

		int TEST = -1;
		if (test_factor >= 0 && test_factor <= 7)
			TEST = test_factor;

		for (int i = 0; i < number_of_factors; i++) {
			if (TEST != -1 && i != TEST)
				continue;
			DefaultCategoryDataset line_chart_dataset = new DefaultCategoryDataset();

			for (int cell : data[i]) {
				double average = 0;
				for (int _i = 0; _i < rounds; _i++) {
					if (i == 0) { // mem_size
						Learner.run(learning_algorithm, eq_oracle, cell, trr_counters, max_bits_to_flip,
								number_of_accesses, trr_threshold, rh_threshold, refresh_interval, visualise, max_steps,
								ecc_status);
					} else if (i == 1) { // trr_counters
						Learner.run(learning_algorithm, eq_oracle, memory_size, cell, max_bits_to_flip,
								number_of_accesses, trr_threshold, rh_threshold, refresh_interval, visualise, max_steps,
								ecc_status);
					} else if (i == 2) { // max_bits
						Learner.run(learning_algorithm, eq_oracle, memory_size, trr_counters, cell, number_of_accesses,
								trr_threshold, rh_threshold, refresh_interval, visualise, max_steps, ecc_status);
					} else if (i == 3) { // num_accesses
						Learner.run(learning_algorithm, eq_oracle, memory_size, trr_counters, max_bits_to_flip, cell,
								trr_threshold, rh_threshold, refresh_interval, visualise, max_steps, ecc_status);
					} else if (i == 4) { // trr_threshold
						Learner.run(learning_algorithm, eq_oracle, memory_size, trr_counters, max_bits_to_flip,
								number_of_accesses, cell, rh_threshold, refresh_interval, visualise, max_steps,
								ecc_status);
					} else if (i == 5) { // rh_threshold
						Learner.run(learning_algorithm, eq_oracle, memory_size, trr_counters, max_bits_to_flip,
								number_of_accesses, trr_threshold, cell, refresh_interval, visualise, max_steps,
								ecc_status);
					} else if (i == 6) { // refresh_interval
						Learner.run(learning_algorithm, eq_oracle, memory_size, trr_counters, max_bits_to_flip,
								number_of_accesses, trr_threshold, rh_threshold, cell, visualise, max_steps,
								ecc_status);
					} else if (i == 7) { // max_steps
						Learner.run(learning_algorithm, eq_oracle, memory_size, trr_counters, max_bits_to_flip,
								number_of_accesses, trr_threshold, rh_threshold, refresh_interval, visualise, cell,
								ecc_status);
					}
					average += Learner.RUN_TIME;
				}
				average /= 1000;
				average /= rounds;
				line_chart_dataset.addValue(average, key, cell + "");
			}

			JFreeChart lineChartObject = ChartFactory.createLineChart(appName + " - " + title[i], xTitle[i], yTitle,
					line_chart_dataset, PlotOrientation.VERTICAL, true, true, false);

			int width = 640;
			int height = 480;
			String dir = "./plots" + (ecc_status ? "" : "_ecc-off") + "/";
			File directory = new File(dir);
			if (!directory.exists()) {
				directory.mkdir();
			}
			File lineChart = new File(dir + output[i] + ".png");
			ChartUtils.saveChartAsJPEG(lineChart, lineChartObject, width, height);
		}
	}

	public static void runMinimal(boolean ecc_status, int test_factor) throws NoSuchMethodException,
			NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException, IOException {

		int learning_algorithm = -1;
		int eq_oracle = -1;
		int memory_size = -1;
		int trr_counters = -1;
		int max_bits_to_flip = -1;
		int number_of_accesses = 3000;
		int trr_threshold = -1;
		int rh_threshold = -1;
		int refresh_interval = -1;
		boolean visualise = false;
		int max_steps = 100;
		String key = "RunTime";
		int number_of_factors = 8;
		int rounds = 1;

		String output[] = { "mem_size", "trr_counters", "max_bits", "num_accesses", "trr_threshold", "rh_threshold",
				"refresh_interval", "max_steps" };
		String title[] = { "Memory Size Measurement", "TRR Counters Measurement",
				"Maximum Number of Bits to Flip Measurement", "Number of Accesses Measurement",
				"TRR Threshold Measurement", "Rowhammer Threshold Measurement", "Refresh Interval Measurement",
				"Maximum Number of Steps Measurement" };
		String xTitle[] = { "Memory Size", "TRR Counters", "Max Bits to Flip", "Number of Accesses", "TRR Threshold",
				"Rowhammer Threshold", "Refresh Interval", "Maximum Number of Steps" };
		String yTitle = "Run_Time(s)";

		int data[][] = { { 1, 2, 3, 4, 5, 6 }, // mem_size
				{ 0, 1, 2, 3, 4, 5 }, // trr_size
				{ 1, 2, 3, 4, 5, 6 }, // max_bits
				{ 200, 500, 1000, 2000, 3000, 5000 }, // num_accesses
				{ 1000, 1500, 2000, 3000, 5000, 20000 }, // trr_threshold
				{ 200, 500, 1000, 2000, 3000, 5000 }, // rh_threshold
				{ 200, 500, 1000, 5000, 10000, 20000 }, // refresh_interval
				{ 50, 100, 500, 1000, 5000, 10000 } // max_steps
		};

		int TEST = -1;
		if (test_factor >= 0 && test_factor <= 7)
			TEST = test_factor;

		for (int i = 0; i < number_of_factors; i++) {
			if (TEST != -1 && i != TEST)
				continue;
			DefaultCategoryDataset line_chart_dataset = new DefaultCategoryDataset();

			for (int cell : data[i]) {
				double average = 0;
				for (int _i = 0; _i < rounds; _i++) {
					if (i == 0) { // mem_size
						Learner.run(learning_algorithm, eq_oracle, cell, trr_counters, max_bits_to_flip,
								number_of_accesses, trr_threshold, rh_threshold, refresh_interval, visualise, max_steps,
								ecc_status);
					} else if (i == 1) { // trr_counters
						Learner.run(learning_algorithm, eq_oracle, memory_size, cell, max_bits_to_flip,
								number_of_accesses, trr_threshold, rh_threshold, refresh_interval, visualise, max_steps,
								ecc_status);
					} else if (i == 2) { // max_bits
						Learner.run(learning_algorithm, eq_oracle, memory_size, trr_counters, cell, number_of_accesses,
								trr_threshold, rh_threshold, refresh_interval, visualise, max_steps, ecc_status);
					} else if (i == 3) { // num_accesses
						Learner.run(learning_algorithm, eq_oracle, memory_size, trr_counters, max_bits_to_flip, cell,
								trr_threshold, rh_threshold, refresh_interval, visualise, max_steps, ecc_status);
					} else if (i == 4) { // trr_threshold
						Learner.run(learning_algorithm, eq_oracle, memory_size, trr_counters, max_bits_to_flip,
								number_of_accesses, cell, rh_threshold, refresh_interval, visualise, max_steps,
								ecc_status);
					} else if (i == 5) { // rh_threshold
						Learner.run(learning_algorithm, eq_oracle, memory_size, trr_counters, max_bits_to_flip,
								number_of_accesses, trr_threshold, cell, refresh_interval, visualise, max_steps,
								ecc_status);
					} else if (i == 6) { // refresh_interval
						Learner.run(learning_algorithm, eq_oracle, memory_size, trr_counters, max_bits_to_flip,
								number_of_accesses, trr_threshold, rh_threshold, cell, visualise, max_steps,
								ecc_status);
					} else if (i == 7) { // max_steps
						Learner.run(learning_algorithm, eq_oracle, memory_size, trr_counters, max_bits_to_flip,
								number_of_accesses, trr_threshold, rh_threshold, refresh_interval, visualise, cell,
								ecc_status);
					}
					average += Learner.RUN_TIME;
				}
				average /= 1000;
				average /= rounds;
				line_chart_dataset.addValue(average, key, cell + "");
			}

			JFreeChart lineChartObject = ChartFactory.createLineChart(appName + " - " + title[i], xTitle[i], yTitle,
					line_chart_dataset, PlotOrientation.VERTICAL, true, true, false);

			int width = 640;
			int height = 480;

			String dir = "./plots_minimal" + (ecc_status ? "" : "_ecc-off") + "/";
			File directory = new File(dir);
			if (!directory.exists()) {
				directory.mkdir();
			}
			File lineChart = new File(dir + output[i] + ".png");
			ChartUtils.saveChartAsJPEG(lineChart, lineChartObject, width, height);
		}
	}
}
