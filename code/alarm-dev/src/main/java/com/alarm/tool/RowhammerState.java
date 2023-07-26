package com.alarm.tool;

import java.util.HashMap;
import java.util.Set;

public class RowhammerState {
    private HashMap<Integer,Integer> rowsToFlips;

    public RowhammerState() {
        rowsToFlips = new HashMap<>();
    }

    public Set<Integer> getFlippedRows() {
        return rowsToFlips.keySet();
    }

    public int getFlipsForRow(int row) {
        Integer flips = rowsToFlips.get(row);

        if (flips == null) return 0;

        return flips;
    }

    public void addFlippedRow(int row, int flips) {
        rowsToFlips.put(row, flips);
    }

    public int getTotalFlipsAtDistance(int row, int rowDistance) {
        int totalFlips = 0;
        for (int d = 1; d <= rowDistance; d++) {
            totalFlips += getFlipsForRow(row + d) + getFlipsForRow(row - d);
        }

        return totalFlips;
    }
}
