package com.alarm.tool;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Stack;

import com.alarm.helper.Adj;
import com.alarm.helper.Path;
import com.alarm.tool.Adapter.Attack;
import com.alarm.tool.Adapter.Label;
import com.alarm.tool.MemoryModel.L;
import com.alarm.tool.MemoryModel.Loc;
import com.backblaze.erasure.RS;

import de.learnlib.algorithms.lstar.mealy.ExtensibleLStarMealyBuilder;
import de.learnlib.algorithms.ttt.mealy.TTTLearnerMealy;
import de.learnlib.algorithms.ttt.mealy.TTTLearnerMealyBuilder;
import de.learnlib.api.SUL;
import de.learnlib.api.algorithm.LearningAlgorithm.MealyLearner;
import de.learnlib.api.oracle.EquivalenceOracle.MealyEquivalenceOracle;
import de.learnlib.api.statistic.StatisticSUL;
import de.learnlib.drivers.reflect.MethodInput;
import de.learnlib.drivers.reflect.MethodOutput;
import de.learnlib.drivers.reflect.SimplePOJOTestDriver;
import de.learnlib.filter.cache.sul.SULCaches;
import de.learnlib.filter.statistic.sul.ResetCounterSUL;
import de.learnlib.oracle.equivalence.MealyRandomWpMethodEQOracle;
import de.learnlib.oracle.equivalence.MealyWpMethodEQOracle;
import de.learnlib.oracle.equivalence.mealy.RandomWalkEQOracle;
import de.learnlib.oracle.membership.SULOracle;
import de.learnlib.util.Experiment.MealyExperiment;
import de.learnlib.util.statistics.SimpleProfiler;
import net.automatalib.automata.transducers.MealyMachine;
import net.automatalib.serialization.dot.GraphDOT;
import net.automatalib.visualization.Visualization;
import net.automatalib.words.Word;

/* Copyright (C) 2013-2022 TU Dortmund
 * This file is part of LearnLib, http://www.learnlib.de/.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * This example shows how a model of a Java class can be learned using learnlib
 *
 * Derived from an example from @author falkhowar
 * 
 * @author ########
 */

// TODO:
//  - Read config from YAML file
//  - Decouple learning and analysis of model
//  - Use Logger for logging

public final class Learner {

	private static final int RANDOM_SEED = 46_346_293;
	private static final double RESET_PROBABILITY = 0.05;

	public static final int WORD_SIZE = RS.DATA_SHARDS;
	public static final int Parity_SIZE = RS.PARITY_SHARDS;
	public static final int CODE_SIZE = RS.TOTAL_SHARDS;

	private static final int TTT = 0;
	private static final int L_STAR = 1;

	private static final int RANDOM_WALK = 0;
	private static final int WP_METHOD = 1;
	private static final int RANDOM_WP_METHOD = 2;

	private static final int DEFAULT_LEARNING_ALGORITHM = TTT;
	private static final int DEFAULT_EQ_ORACLE = RANDOM_WALK;
	private static final int DEFAULT_MEMORY_SIZE = 3;
	private static final int DEFAULT_TRR_COUNTERS = 1;
	private static final int DEFAULT_MAXIMUM_BITS_TO_FLIP = 1 + 5;
	private static final int DEFAULT_NUMBER_OF_ACCESSES = 1300;
	private static final int DEFAULT_TRR_THRESHOLD = 2500;
	private static final int DEFAULT_RH_THRESHOLD = 3000;
	private static final int DEFAULT_REFRESH_INTERVAL = 6500;
	private static final int DEFAULT_BLAST_RADIUS = 1;
	private static final int DEFAULT_TRR_RADIUS = 1;
	private static final boolean DEFAULT_VISUALISE = false;
	private static final int DEFAULT_MAX_STEPS = 100;
	private static final double DEFAULT_RUN_TIME = 0;
	private static final boolean DEFAULT_ECC_STATUS = true;

	public static int LEARNING_ALGORITHM = DEFAULT_LEARNING_ALGORITHM;
	public static int EQ_ORACLE = DEFAULT_EQ_ORACLE;
	public static int MEMORY_SIZE = DEFAULT_MEMORY_SIZE;
	public static int TRR_COUNTERS = DEFAULT_TRR_COUNTERS;
	public static int MAXIMUM_BITS_TO_FLIP = DEFAULT_MAXIMUM_BITS_TO_FLIP;
	public static int NUMBER_OF_ACCESSES = DEFAULT_NUMBER_OF_ACCESSES;
	public static int TRR_THRESHOLD = DEFAULT_TRR_THRESHOLD;
	public static int RH_THRESHOLD = DEFAULT_RH_THRESHOLD;
	public static int REFRESH_INTERVAL = DEFAULT_REFRESH_INTERVAL;
	public static int BLAST_RADIUS = DEFAULT_BLAST_RADIUS;
	public static int TRR_RADIUS = DEFAULT_TRR_RADIUS;
	public static boolean VISUALISE = DEFAULT_VISUALISE;
	public static int MAX_STEPS = DEFAULT_MAX_STEPS;
	public static double RUN_TIME = DEFAULT_RUN_TIME;
	public static boolean ECC_STATUS = DEFAULT_ECC_STATUS;

	public static int trr_size = -1;
	public static int trr_threshold = Integer.MAX_VALUE;
	public static int bits_to_correct = -1;
	public static int bits_to_detect = -1;
	public static int rh_threshold = -1;

	private Learner() {
	}

	public static void run(int input_learning_algorithm, int input_eq_oracle, int input_memory_size,
			int input_trr_counters, int input_max_bits_to_flip, int input_number_of_accesses, int input_trr_threshold,
			int input_rh_threshold, int input_refresh_interval, boolean input_visualise, int input_max_steps,
			boolean input_ecc_status) throws NoSuchMethodException, IOException, NoSuchFieldException,
			SecurityException, IllegalArgumentException, IllegalAccessException {

		LEARNING_ALGORITHM = DEFAULT_LEARNING_ALGORITHM;
		EQ_ORACLE = DEFAULT_EQ_ORACLE;
		MEMORY_SIZE = DEFAULT_MEMORY_SIZE;
		TRR_COUNTERS = DEFAULT_TRR_COUNTERS;
		MAXIMUM_BITS_TO_FLIP = DEFAULT_MAXIMUM_BITS_TO_FLIP;
		NUMBER_OF_ACCESSES = DEFAULT_NUMBER_OF_ACCESSES;
		TRR_THRESHOLD = DEFAULT_TRR_THRESHOLD;
		RH_THRESHOLD = DEFAULT_RH_THRESHOLD;
		REFRESH_INTERVAL = DEFAULT_REFRESH_INTERVAL;
		BLAST_RADIUS = DEFAULT_BLAST_RADIUS;
		TRR_RADIUS = DEFAULT_TRR_RADIUS;
		VISUALISE = DEFAULT_VISUALISE;
		MAX_STEPS = DEFAULT_MAX_STEPS;
		RUN_TIME = DEFAULT_RUN_TIME;
		ECC_STATUS = DEFAULT_ECC_STATUS;

		trr_size = -1;
		trr_threshold = Integer.MAX_VALUE;
		bits_to_correct = -1;
		bits_to_detect = -1;
		rh_threshold = -1;

		LEARNING_ALGORITHM = (input_learning_algorithm == L_STAR) ? input_learning_algorithm
				: DEFAULT_LEARNING_ALGORITHM;
		EQ_ORACLE = (input_eq_oracle == WP_METHOD || input_eq_oracle == RANDOM_WP_METHOD) ? input_eq_oracle
				: DEFAULT_EQ_ORACLE;
		MEMORY_SIZE = (input_memory_size != -1) ? input_memory_size : DEFAULT_MEMORY_SIZE;
		TRR_COUNTERS = (input_trr_counters != -1) ? input_trr_counters : DEFAULT_TRR_COUNTERS;
		MAXIMUM_BITS_TO_FLIP = (input_max_bits_to_flip != -1) ? input_max_bits_to_flip : DEFAULT_MAXIMUM_BITS_TO_FLIP;
		NUMBER_OF_ACCESSES = (input_number_of_accesses != -1) ? input_number_of_accesses : DEFAULT_NUMBER_OF_ACCESSES;
		TRR_THRESHOLD = (input_trr_threshold != -1) ? input_trr_threshold : DEFAULT_TRR_THRESHOLD;
		RH_THRESHOLD = (input_rh_threshold != -1) ? input_rh_threshold : DEFAULT_RH_THRESHOLD;
		REFRESH_INTERVAL = (input_refresh_interval != -1) ? input_refresh_interval : DEFAULT_REFRESH_INTERVAL;
		VISUALISE = input_visualise;
		MAX_STEPS = (input_max_steps != -1) ? input_max_steps : DEFAULT_MAX_STEPS;
		ECC_STATUS = input_ecc_status;

		SimplePOJOTestDriver driver = new SimplePOJOTestDriver(Adapter.Memory.class.getConstructor());

		int mem_size = MEMORY_SIZE;
		Loc locs[] = new Loc[mem_size];
		for (int i = 0; i < mem_size; i++) {
			locs[i] = new L(i);
		}

		int bits = MAXIMUM_BITS_TO_FLIP;
		int bits_to_flip[] = new int[bits];
		for (int i = 0; i < bits; i++) {
			bits_to_flip[i] = i;
		}

		Method m_access = Adapter.Memory.class.getMethod("access", Label.class, Loc.class, int.class);

		Label label_attack = new Attack(NUMBER_OF_ACCESSES);
		MethodInput access = driver.addInput(
				"access(" + NUMBER_OF_ACCESSES + ")(" + locs[0] + ")(" + bits_to_flip[1] + ")", m_access, label_attack,
				locs[0], bits_to_flip[0]);
		for (int i = 1; i < locs.length; i++) {
			for (int j = 1; j < bits; j++) {
				if (i == 0 && j == 0)
					continue;
//				if (j == 1 || j == 2 || j == 3)
//					continue;
				access = driver.addInput("access(" + NUMBER_OF_ACCESSES + ")(" + locs[i] + ")(" + bits_to_flip[j] + ")",
						m_access, label_attack, locs[i], bits_to_flip[j]);
			}
		}

		long start = System.nanoTime();

		StatisticSUL<MethodInput, MethodOutput> statistic_sul = new ResetCounterSUL<>("membership queries", driver);

		SUL<MethodInput, MethodOutput> effective_sul = statistic_sul;
		effective_sul = SULCaches.createCache(driver.getInputs(), effective_sul);

		SULOracle<MethodInput, MethodOutput> mq_oracle = new SULOracle<>(effective_sul);

		List<Word<MethodInput>> suffixes = new ArrayList<>();
		suffixes.add(Word.fromSymbols(access));

		MealyLearner<MethodInput, MethodOutput> lstar = new ExtensibleLStarMealyBuilder<MethodInput, MethodOutput>()
				.withAlphabet(driver.getInputs()).withOracle(mq_oracle).withInitialSuffixes(suffixes).create();

		TTTLearnerMealy<MethodInput, MethodOutput> ttt = new TTTLearnerMealyBuilder<MethodInput, MethodOutput>()
				.withAlphabet(driver.getInputs()).withOracle(mq_oracle).create();

		MealyEquivalenceOracle<MethodInput, MethodOutput> random_walks = new RandomWalkEQOracle<>(driver,
				RESET_PROBABILITY, MAX_STEPS, false, new Random(RANDOM_SEED));

		MealyWpMethodEQOracle<MethodInput, MethodOutput> wp = new MealyWpMethodEQOracle<MethodInput, MethodOutput>(
				mq_oracle, MAX_STEPS);

		MealyRandomWpMethodEQOracle<MethodInput, MethodOutput> rwp = new MealyRandomWpMethodEQOracle<MethodInput, MethodOutput>(
				mq_oracle, 1, MAX_STEPS);

		MealyExperiment<MethodInput, MethodOutput> experiment = null;

		if (LEARNING_ALGORITHM == TTT) {
			if (EQ_ORACLE == RANDOM_WALK)
				experiment = new MealyExperiment<>(ttt, random_walks, driver.getInputs());
			else if (EQ_ORACLE == WP_METHOD)
				experiment = new MealyExperiment<>(ttt, wp, driver.getInputs());
			else if (EQ_ORACLE == RANDOM_WP_METHOD)
				experiment = new MealyExperiment<>(ttt, rwp, driver.getInputs());
		} else if (LEARNING_ALGORITHM == L_STAR) {
			if (EQ_ORACLE == RANDOM_WALK)
				experiment = new MealyExperiment<>(lstar, random_walks, driver.getInputs());
			else if (EQ_ORACLE == WP_METHOD)
				experiment = new MealyExperiment<>(lstar, wp, driver.getInputs());
			else if (EQ_ORACLE == RANDOM_WP_METHOD)
				experiment = new MealyExperiment<>(lstar, rwp, driver.getInputs());
		}

		experiment.setProfile(true);
		experiment.setLogModels(true);
		experiment.run();

		MealyMachine<?, MethodInput, ?, MethodOutput> result = experiment.getFinalHypothesis();

		long stop = System.nanoTime();

		System.out.println("-------------------------------------------------------");
		System.out.println("Profiling:");
		System.out.println(SimpleProfiler.getResults());

		System.out.println("-------------------------------------------------------");
		System.out.println("Learning phase statistics:");
		System.out.println(experiment.getRounds().getSummary());
		System.out.println(statistic_sul.getStatisticalData().getSummary());

		System.out.println("-------------------------------------------------------");
		System.out.println("Model statistics:");
		System.out.println("States: " + result.size());
		System.out.println("Sigma: " + driver.getInputs().size());

		System.out.println();
		System.out.println("-------------------------------------------------------");
		System.out.println("Model: ");

		if (VISUALISE) {
			GraphDOT.write(result, driver.getInputs(), System.out);
			Visualization.visualize(result, driver.getInputs());
		}

		int size = result.getStates().size();
		int head = size + 2;
		Adj adj = new Adj(size);
		String nodes[] = new String[size];
		int t = 0;
		for (Object node : result.getStates()) {
			nodes[t] = node.toString() + "";
			t++;
		}

		int source = 0;// (int)result.transitionGraphView(driver.getInputs()).getNodes().toArray()[0];
		int dest = -1;
		ArrayDeque<Integer> trr_dests = new ArrayDeque<Integer>();
		StringBuilder sb = new StringBuilder();
		GraphDOT.write(result, driver.getInputs(), sb);
		String parse = sb.toString();
		String lines[] = parse.split("\n");

		for (int i = head; i < lines.length - 5; i++) {
			String chunks[] = lines[i].split(" ");
			int s = Integer.valueOf(chunks[0].trim().substring(1));
			int d = Integer.valueOf(chunks[2].trim().substring(1));
			String tmp_l = chunks[3].split("\\(")[2];
			int l = Integer.parseInt(tmp_l.substring(0, tmp_l.length() - 1));
			insertEdge(adj, s, d);
			if (dest == -1 && chunks[5].contains("FlipException"))
				dest = d;
			else if (chunks[5].contains("Correction") && !chunks[5].contains("TRR")) {
				String tmp_s = chunks[3].split("\\(")[3];
				int tmp = Integer.parseInt(tmp_s.substring(0, tmp_s.length() - 3));
				if (tmp > bits_to_correct)
					bits_to_correct = tmp;
			}
			if (chunks[5].contains("TRR"))
				trr_dests.add(s);

			adj.label[s][d] += chunks[3].substring(8, chunks[3].length() - 2) + "/";
			adj.locs[s][d].add(l);
		}

		ArrayDeque<Integer> locs_visited[] = null;
		Path path = null;
		if (dest != -1)
			path = shortestPath(adj, source, dest, size);

		if (path == null) {
			System.out.println("No bit flips were found (There's no path between the source and destination)");
		} else {
			rh_threshold = path.distance[dest];
			System.out.println("Rowhammer Threshold: [" + (rh_threshold - 1) * NUMBER_OF_ACCESSES + ", "
					+ rh_threshold * NUMBER_OF_ACCESSES * BLAST_RADIUS + "]");
			System.out.println();
			System.out.println("Minimum trace of states to get a bit flip: ");
			Stack<Integer> out = path.path(dest);
			String seq = "";
			int tmp1 = -1;
			int tmp2 = -1;
			locs_visited = new ArrayDeque[rh_threshold];
			int k = 0;
			while (!out.isEmpty()) {
				tmp1 = tmp2;
				tmp2 = out.pop();
				if (tmp1 != -1) {
					seq += adj.label[tmp1][tmp2] + ",";
					locs_visited[k++] = new ArrayDeque<Integer>(adj.locs[tmp1][tmp2]);
				}
				System.out.print(tmp2 + ", ");
			}
			System.out.println("\n");
			System.out.println("Minimum trace of read/write to get a bit flip: ");
			System.out.println(seq);
		}

		while (!trr_dests.isEmpty()) {
			Path trr_path = null;
			int trr_dest = trr_dests.poll();
			trr_path = shortestPath(adj, source, trr_dest, size);
			if (trr_path != null) {
				int tmp = trr_path.distance[trr_dest] + 1;
				if (trr_threshold > tmp)
					trr_threshold = tmp;
			}
		}

		if (bits_to_correct > 0)
			System.out.println("ECC Maximum Number of Bits to Correct: " + bits_to_correct);
		else
			System.out.println("No ECC was found!");

		if (trr_threshold == Integer.MAX_VALUE) {
			System.out.println("There's no active TRR in place");
		} else {
			System.out.println("TRR Threshold: [" + (trr_threshold - 1) * NUMBER_OF_ACCESSES + ", "
					+ trr_threshold * NUMBER_OF_ACCESSES * BLAST_RADIUS + "]");
			trr_size = TRRSizeCalculator(locs_visited, rh_threshold);
			if (trr_size > 0)
				System.out.println("TRR Size: " + trr_size);
			else
				System.out.println("TRR Size: " + (MEMORY_SIZE - 1) + " (Ideal - No bit flips were found)");
		}

		System.out.println("-------------------------------------------------------");

		double total = (stop - start) / (double) 1000000;
		RUN_TIME = total;
		System.out.printf("Time to run: %1.6f ms.\n", total);

		System.out.println("-------------------------------------------------------");

		SimpleProfiler.reset();
	}

	public static void insertEdge(Adj adj, int source, int dest) {
		adj.connection[source][dest] = true;
	}

	public static Path shortestPath(Adj adj, int source, int dest, int size) {
		ArrayDeque<Integer> queue = new ArrayDeque<Integer>();

		boolean visited[] = new boolean[size];
		Path path = new Path(size);

		visited[source] = true;
		queue.add(source);
		path.distance[source] = 0;

		while (!queue.isEmpty()) {
			int s = queue.remove();

			for (int i = 0; i < size; i++) {
				if (adj.connection[s][i] && !visited[i]) {
					visited[i] = true;
					queue.add(i);
					path.distance[i] = path.distance[s] + 1;
					path.trace[i] = s;

					if (i == dest)
						return path;
				}
			}
		}
		return null;
	}

	public static int TRRSizeCalculator(ArrayDeque<Integer> locs_visited[], int levels) {
		HashSet<HashSet<Integer>> result = new HashSet<HashSet<Integer>>();
		result.add(new HashSet<Integer>());
		for (int i = 0; i < levels; i++) {
			HashSet<HashSet<Integer>> tmp_array = new HashSet<HashSet<Integer>>();
			for (HashSet<Integer> hs : result) {
				for (int l : locs_visited[i]) {
					HashSet<Integer> tmp_out = (HashSet<Integer>) hs.clone();
					tmp_out.add(l);
					tmp_array.add(tmp_out);
				}
			}
			result = (HashSet<HashSet<Integer>>) tmp_array.clone();
		}

		int min = Integer.MAX_VALUE;
		for (HashSet<Integer> hs : result) {
			if (hs.size() < min) {
				min = hs.size();
			}
		}

		return min;
	}
}
