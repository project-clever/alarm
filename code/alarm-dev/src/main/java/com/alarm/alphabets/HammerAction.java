package com.alarm.alphabets;

// Class representing an input alphabet symbol
public class HammerAction {

    private final int readCount;
    private final int row;
    private final int bitFlips;

    public HammerAction(int row, int readCount, int bitFlips) {
        this.row = row;
        this.readCount = readCount;
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

        HammerAction that = (HammerAction) o;

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
        //return "HAMMER(read: " + readCount + ", row: " + row + ", bitflips: " + bitFlips + ")";
        return "HAMMER(" + row + "," + readCount + "," + bitFlips + ")";
    }
}
