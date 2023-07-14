package com.alarm.tool;

// Class representing an input alphabet symbol
public class HammerSymbol {

    private final int readCount;
    private final int row;
    private final int bitFlips;

    public HammerSymbol(int readCount, int row, int bitFlips) {
        this.readCount = readCount;
        this.row = row;
        this.bitFlips = bitFlips;
    }

    public int getReadCount() {
        return readCount;
    }

    public int getRow() {
        return row;
    }

    public int getBitFlips() {
        return bitFlips;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        HammerSymbol that = (HammerSymbol) o;

        if (readCount != that.readCount) return false;
        if (row != that.row) return false;
        return bitFlips == that.bitFlips;
    }

    @Override
    public int hashCode() {
        int result = readCount;
        result = 31 * result + row;
        result = 31 * result + bitFlips;
        return result;
    }

    public String toString() {
        return "HAMMER(read: " + readCount + ", row: " + row + ", bitflips: " + bitFlips + ")";
    }
}
