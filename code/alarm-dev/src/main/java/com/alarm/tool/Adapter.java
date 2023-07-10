package com.alarm.tool;

import java.util.HashMap;

import com.alarm.exceptions.ECCCorrectionException;
import com.alarm.exceptions.ECCDetectionException;
import com.alarm.exceptions.FlipException;
import com.alarm.exceptions.TRRException;
import com.alarm.tool.MemoryModel.L;
import com.alarm.tool.MemoryModel.Loc;
import com.alarm.tool.MemoryModel.MemSystem;
import com.backblaze.erasure.RS;

/**
 * This class is defined to establish a connection between the learner and the
 * memory class (the Java class to be learnt) by acting as a translator in
 * between.
 *
 * @author ########
 */

public class Adapter {

	public static int MEMORY_SIZE = Learner.MEMORY_SIZE;
	public static int CODE_SIZE = Learner.CODE_SIZE;
	public static int WORD_SIZE = Learner.WORD_SIZE;
	public static final double FLIP_PROBABILITY = 1.0;

	// Memory Definition
	public static class Memory {
		HashMap<Loc, Character> learner_map;
		HashMap<Loc, Integer> memory_map;
		HashMap<Character, Integer> variable_map;
		MemSystem mem;
		boolean flip;
		boolean ecc;

		public Memory() {
			MEMORY_SIZE = Learner.MEMORY_SIZE;
			CODE_SIZE = Learner.CODE_SIZE;
			WORD_SIZE = Learner.WORD_SIZE;

			this.learner_map = new HashMap<Loc, Character>();
			int size = MEMORY_SIZE;
			Loc[] locs = new Loc[size];
			char c = 'x';
			for (int i = 0; i < size; i++) {
				locs[i] = new L(i);
				learner_map.put(locs[i], c);
			}
			this.variable_map = new HashMap<Character, Integer>();
			variable_map.put(c, 0);
			this.memory_map = new HashMap<Loc, Integer>();
			this.mem = new MemSystem();
			for (Loc loc : learner_map.keySet()) {
				memory_map.put(loc, variable_map.get(learner_map.get(loc)));
				mem.mem.write(loc, variable_map.get(learner_map.get(loc)));
				mem.ecc.add(loc, variable_map.get(learner_map.get(loc)));
			}
			flip = false;
			ecc = true;
		}

		public char read(Loc loc, int flip_v)
				throws FlipException, ECCCorrectionException, ECCDetectionException, TRRException {
			int r = this.mem.read(loc, flip_v);
			this.ecc = this.mem.ecc.validateAll(this.mem.mem);
			if (r != 0) {
				flip = true;
			}
			char c = 'x';
			if (!flip) {
				for (Loc l : memory_map.keySet()) {
					if (!memory_map.get(l).equals(mem.mem.read(l))) {
						flip = true;
						break;
					}
				}
			}
			if (flip) {
				throw new FlipException("A flip has occured");
			}
			return c;
		}

		public void write(Loc loc, char c, int flip_v)
				throws FlipException, ECCCorrectionException, ECCDetectionException, TRRException {
			this.mem.write(loc, this.variable_map.get(c), flip_v);
			this.ecc = this.mem.ecc.validateAll(this.mem.mem);
			if (!ecc) {
				throw new ECCCorrectionException("ECC has detected an error");
			}
			if (!flip) {
				for (Loc l : memory_map.keySet()) {
					if (!memory_map.get(l).equals(mem.mem.read(l))) {
						flip = true;
						break;
					}
				}
			}
			if (flip) {
				throw new FlipException("A flip has occured");
			}
		}

		public String access(Label label, Loc loc, int flip_v) throws Exception {
			return Label.eval(this, label, loc, flip_v);
		}

	}
	// END OF Memory Definition

	// Label Definition
	public interface Label {
		public static String eval(Memory mem, Label label, Loc loc, int flip_v)
				throws FlipException, ECCCorrectionException, ECCDetectionException, TRRException {
			String out = "";
			boolean ec = false, trr = false;
			int size = CODE_SIZE;

			boolean[] shard_present = new boolean[size];
			for (int i = 0; i < size; i++) {
				shard_present[i] = true;
				if (i < flip_v)
					shard_present[i] = false;
			}

			if (label instanceof Attack) {
				Attack s = (Attack) label;
				for (int i = 0; i < s.num; i++) {
					try {
						out = "" + (mem.read(loc, flip_v) == 'x' ? "NoFlip" : "Flip");
					} catch (ECCCorrectionException e) {
						for (Loc l : mem.mem.mem.neighbours(loc)) {
							try {
								byte[][] tmp = mem.mem.getECC().get(l);
								int inputSize = WORD_SIZE;
								int storedSize = inputSize + RS.BYTES_IN_INT;
								int shardSize = (storedSize + inputSize - 1) / inputSize;
								RS.decode(tmp, shard_present, shardSize, inputSize);
								;
								String tmp2 = RS.array2Text(tmp, shardSize, inputSize);
								int val_tmp2 = Integer.parseInt(tmp2);
								if (val_tmp2 != mem.mem.read(l, flip_v)) {
									mem.mem.write(l, val_tmp2, flip_v);
									Attack tmp_s = new Attack(s.num - i);
									out = Label.eval(mem, tmp_s, loc, flip_v);
									ec = true;
								}
							} catch (IllegalArgumentException ee) {
								throw new FlipException("A flip has occured");
							}
						}
					} catch (TRRException e) {
						Attack tmp_s = new Attack(s.num - i);
						out = Label.eval(mem, tmp_s, loc, flip_v);
						trr = true;
					}
				}
			} else {
				throw new IllegalArgumentException("Invalid Label!");
			}

			return out + (ec && (!out.contains("ECC_Correction")) ? ",ECC_Correction" : "")
					+ (trr && (!out.contains("TRR_Correction")) ? ",TRR_Correction" : "");
		}
	}

	public static class Attack implements Label {

		int num;

		public Attack(int num) {
			this.num = num;
		}
	}
	// END OF Label Definition

}
