package com.alarm.helper;

import java.util.Stack;

public class Path {
	public int distance[];
	public int trace[];

	public Path(int size) {
		distance = new int[size];
		trace = new int[size];
		for (int i = 0; i < size; i++) {
			distance[i] = Integer.MAX_VALUE;
			trace[i] = -1;
		}
	}

	public Stack<Integer> path(int dest) {
		Stack<Integer> path = new Stack<Integer>();
		path.add(dest);
		while (trace[dest] != -1) {
			path.add(trace[dest]);
			dest = trace[dest];
		}

		return path;

	}
}
