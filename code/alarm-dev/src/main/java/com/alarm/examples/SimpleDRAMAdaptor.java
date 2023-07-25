package com.alarm.examples;

import com.alarm.tool.HammerAction;
import com.alarm.tool.HammerResult;
import com.alarm.tool.TestRunner;
import net.automatalib.words.Word;

import java.util.*;

public class SimpleDRAMAdaptor implements TestRunner<HammerAction, HammerResult> {
    private HashSet<Word<HammerAction>> tests = new HashSet<>();
    private final int rowhammerThreshold = 2;
    private final int blastRadius = 1;
    private final double probabilityIncrement = 0.1;
    private final int numRows = 5;
    private  Map<Integer, Double> flipProbabilities;
    private Map<Integer, Integer> readCounts;
    private Random rnd = new Random();

    public SimpleDRAMAdaptor() {
        flipProbabilities = new HashMap<>();
        readCounts = new HashMap<>();
        setup();
    }

    @Override
    public void setup() {
        for (int row = 0; row < numRows; row++) {
            flipProbabilities.put(row, 0.0);
            readCounts.put(row, 0);
        }
    }

    @Override
    public void cleanup() {

    }

    private void computeProbabilities() {
        for (int row = 0; row < numRows; row++) {
            // Compute total number of attacks from neighbours
            int totAttacks = 0;

            for (int neighbour: getNeighbours(row))
                totAttacks += readCounts.get(neighbour);

            // Compute probability depending on number of attacks
            flipProbabilities.put(row, computeFlipProbability(totAttacks));
        }
    }

    private double computeFlipProbability(int attacks) {
        // Using sigmoid function with bias
        // return 1 / ( 1 + Math.exp(-attacks + rowhammerThreshold));

        // piece-wise function
        //  if (attacks >= rowhammerThreshold) return 1.0;
        //  return 0.0;

        // Steep exponential
        return Math.min(Math.exp(10 * attacks - 30), 1);
    }

    private HammerResult generateOutcome(int row) {
        int flipSlots = (int) Math.floor(flipProbabilities.get(row) / probabilityIncrement);
        int totSlots = (int) ( 1 / probabilityIncrement );
        HammerResult[] resSlots = new HammerResult[totSlots];

        Arrays.fill(resSlots, 0, flipSlots, HammerResult.FLIP);
        Arrays.fill(resSlots, flipSlots, totSlots, HammerResult.OK);
        // System.out.println(Arrays.toString(resSlots));

        int rndSlot = rnd.nextInt(totSlots);
        return resSlots[rndSlot];
    }

    @Override
    public HammerResult runTest(Word<HammerAction> test) {
        // System.out.println("Running test: " + test);

        for (HammerAction act: test) {
            int curReadcount = readCounts.get(act.getRow());
            readCounts.put(act.getRow(), curReadcount + act.getReadCount());
        }

        computeProbabilities();
        int lastRow = test.lastSymbol().getRow();

        boolean flipped = false;
        for (int row : getNeighbours(lastRow)) {
            flipped |= (generateOutcome(row) == HammerResult.FLIP);
        }

        tests.add(test);

        if (flipped) return HammerResult.FLIP;
        return HammerResult.OK;
    }

    private List<Integer> getNeighbours(int row) {
        int rowAbove, rowBelow;
        LinkedList<Integer> result = new LinkedList<>();

        for (int d = 1; d <= blastRadius; d++) {
            if ( (rowAbove = row + d) < numRows) result.add(rowAbove);
            if ( (rowBelow = row - d) >= 0) result.add(rowBelow);
        }

        return result;
    }

    public static Word<HammerAction> doubleSidedAttack(int row, int readCount) {
        int rowAbove = row + 1;
        int rowBelow = row - 1;

        Word<HammerAction> attack = Word.epsilon();

        for (int i = 0; i < 3; i++) {
            attack = attack.append(new HammerAction(readCount, rowAbove, 3));
            attack = attack.append(new HammerAction(readCount, rowBelow, 3));
        }

        return attack;
    }

    public static void main(String[] args) {
        SimpleDRAMAdaptor a = new SimpleDRAMAdaptor();
        Word<HammerAction> test = Word.fromSymbols(
                new HammerAction(1, 0, 1),
                new HammerAction(1,2,1),
                new HammerAction(1, 4, 1),
                new HammerAction(1, 0, 1),
                new HammerAction(1, 3, 1),
                new HammerAction(1, 4, 1)
        );

        a.runTest(test);
        System.out.println(a.readCounts);
        System.out.println(a.flipProbabilities);

        int numFlips = 0;

        for (int i = 0; i < 100; i++) {
            a.setup();
            HammerResult output = a.runTest(test);
            System.out.println(output);
            if(output == HammerResult.FLIP) numFlips++;
            a.cleanup();
        }
        System.out.println("Flip frequency: " + (double) numFlips / 100.0);
        // «'HAMMER(read: 1, row: 0, bitflips: 1)'␣'
        // HAMMER(read: 1, row: 0, bitflips: 1)'␣
        // 'HAMMER(read: 1, row: 1, bitflips: 1)'␣
        // 'HAMMER(read: 1, row: 1, bitflips: 1)'␣
        // 'HAMMER(read: 1, row: 0, bitflips: 1)'␣
        // 'HAMMER(read: 1, row: 1, bitflips: 1)'␣
        // 'HAMMER(read: 1, row: 1, bitflips: 1)'»
        //System.out.println(a.computeFlipProbability(5));
    }
}
