package com.alarm.helper;

import java.util.HashSet;

public class Adj {
	public boolean connection[][];
	public String label[][];
	public HashSet<Integer> locs[][];

	public Adj(int size) {
		connection = new boolean[size][size];
		label = new String[size][size];
		locs = new HashSet[size][size];
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < connection.length; j++) {
				connection[i][j] = false;
				label[i][j] = "";
				locs[i][j] = new HashSet<Integer>();
			}
		}
	}
}