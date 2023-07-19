package com.alarm.adapter.synthetic;

import java.util.HashMap;
import java.util.Random;

import com.alarm.exceptions.ECCCorrectionException;
import com.alarm.exceptions.ECCDetectionException;
import com.alarm.exceptions.TRRException;
import com.alarm.tool.Learner;
import com.backblaze.erasure.RS;
import com.backblaze.erasure.ReedSolomon;

//test
import com.alarm.adapter.synthetic.components.L;
import com.alarm.adapter.synthetic.components.Loc;
import com.alarm.adapter.synthetic.components.Mem;
import com.alarm.adapter.synthetic.components.Env;

/**
 * This is a simplified model for DRAM implemented in a form of a Java class.
 * This model contains Target Row Refresh(TRR) and Error Correction Code(ECC)
 * implementations.
 *
 * Currently, it extends MemoryModel.
 * @author ########
 */

public class MemoryModelVA extends MemoryModel{

	// Memory System Definition
	public static class MemSystem {
		public Mem mem;
		public Env env;
		public ECC ecc;
		public TRR trr;

		public MemSystem() {
			MEMORY_SIZE = Learner.MEMORY_SIZE;
			TRR_THRESHOLD = Learner.TRR_THRESHOLD;
			TRR_COUNTERS = Learner.TRR_COUNTERS;
			RH_THRESHOLD = Learner.RH_THRESHOLD;
			REFRESH_INTERVAL = Learner.REFRESH_INTERVAL;
			BLAST_RADIUS = Learner.BLAST_RADIUS;
			TRR_RADIUS = Learner.TRR_RADIUS;
			ECC_STATUS = Learner.ECC_STATUS;

			Mem mem = new Mem();
			int size = MEMORY_SIZE;
			Loc[] locs = new Loc[size];
			for (int i = 0; i < size; i++) {
				locs[i] = new L(i);
				mem.write(locs[i], 0);
			}
			this.mem = mem;
			this.env = new Env();
			this.ecc = new ECC();
			this.trr = new TRR();
		}

		public MemSystem(Mem mem, Env env, ECC ecc, TRR trr) {
			this.mem = mem;
			this.env = env;
			this.ecc = ecc;
			this.trr = trr;
		}

		public int read(Loc loc, int flip_v) throws TRRException {
			clock(loc, flip_v);
			this.env.resetCounter(loc, 0);
			return this.mem.read(loc);
		}

		public void write(Loc loc, int v, int flip_v) throws TRRException {
			clock(loc, flip_v);
			this.env.resetCounter(loc, 0);
			this.mem.write(loc, v);
			this.ecc.add(loc, v);
		}

		public Mem getMem() {
			return this.mem;
		}

		public Env getEnv() {
			return this.env;
		}

		public ECC getECC() {
			return this.ecc;
		}

		public void clock(Loc loc, int flip_v) throws TRRException {
			this.trr.checkClocks(this.env.clocks);
			this.env.checkClocks();
			for (Loc l : this.mem.neighbours(loc)) {
				int attenuation_factor = BLAST_RADIUS - this.mem.distance(loc, l) + 1;
				this.env.tickCounter(l, attenuation_factor);
			}
			this.trr.tickCounter(loc, 1);
			if (!this.trr.checkSingleCounter(loc)) {
				this.env.resetCounter(loc, 0);
				this.trr.resetCounter(loc);
				read(loc, flip_v);
				for (Loc l : this.mem.neighbours(loc)) {
					read(l, flip_v);
				}
				throw new TRRException();
			}
			for (Loc l : this.mem.neighbours(loc)) {
				Random r = new Random();
				double pr = r.nextDouble();
				int tmp = this.env.counter.map.get(l);
				if (tmp >= RH_THRESHOLD && pr <= FLIP_PROBABILITY) {
					this.mem.flip(l, flip_v);
					for (int i = 0; i < flip_v; i++)
						this.ecc.tweak(l, i, (byte) 1);
				}
			}
		}
	}
	// END OF Memory System Definition


	// ECC Definition
	public static class ECC {
		public HashMap<Loc, byte[][]> map;

		public ECC() {
			map = new HashMap<Loc, byte[][]>();
		}

		public boolean validate(Loc loc, Mem mem) throws ECCCorrectionException, ECCDetectionException {
			if (!ECC_STATUS)
				return true;
			String code = "" + Integer.toBinaryString(mem.read(loc));
			while (code.length() < WORD_SIZE)
				code = "0" + code;

			byte[] tmpBuffer = new byte[Parity_SIZE];
			ReedSolomon reedSolomon = ReedSolomon.create(WORD_SIZE, Parity_SIZE);
			boolean isValid = reedSolomon.isParityCorrect(this.map.get(loc), 0, Parity_SIZE, tmpBuffer);
			if (!isValid)
				throw new ECCCorrectionException();

			return true;
		}

		public boolean validateAll(Mem mem) throws ECCCorrectionException, ECCDetectionException {
			for (Loc l : mem.map.keySet()) {
				validate(l, mem);
			}
			return true;
		}

		public void tweak(Loc loc, int position, byte val) {
			byte tmp[][] = this.map.get(loc);
			tmp[position][0] = val;
			this.map.put(loc, tmp);
		}

		public void add(Loc loc, int val) {
			String in = Integer.toBinaryString(val);
			while (in.length() < WORD_SIZE)
				in = "0" + in;
			this.map.put(loc, RS.encode(in));
		}

		public byte[][] get(Loc loc) {
			return this.map.get(loc);
		}
	}
	// END OF ECC Definition

	// TRR Definition
	public static class TRR {
		int counters;
		int radius;
		public HashMap<Loc, Integer> map;

		public TRR() {
			counters = TRR_COUNTERS;
			radius = TRR_RADIUS;
			map = new HashMap<Loc, Integer>();
		}

		public TRR(int counters, HashMap<Loc, Integer> map, int radius) {
			this.counters = counters;
			this.radius = radius;
			this.map = map;
		}

		public boolean checkSingleCounter(Loc loc) {
			return this.map.get(loc) < TRR_THRESHOLD;
		}

		public void tickCounter(Loc loc, int v) {
			if (this.map.containsKey(loc)) {
				int tmp = this.map.get(loc);
				this.map.put(loc, tmp + v);
			} else if (this.map.size() < this.counters) {
				this.map.put(loc, v);
			} else {
				int min = Integer.MAX_VALUE;
				Loc minKey = null;
				for (Loc l : this.map.keySet()) {
					if (this.map.get(l) < min) {
						min = this.map.get(l);
						minKey = l;
					}
				}
				this.map.remove(minKey);
				this.map.put(loc, v);
			}
		}

		public void resetCounter(Loc loc) {
			if (this.map.containsKey(loc)) {
				this.map.put(loc, 0);
			}
		}

		public void resetCounters() {
			for (Loc loc : this.map.keySet()) {
				resetCounter(loc);
			}
		}

		public void checkClocks(int clock) {
			if (clock >= REFRESH_INTERVAL) {
				resetCounters();
			}
		}
	}
	// END OF TRR Definition


}
