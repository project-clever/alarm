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

public class MemoryModelVB extends MemoryModel{

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
			L[] locs = new L[size];
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

		public int read(L loc, int flip_v) throws TRRException {
			clock(loc, flip_v);
			this.env.resetCounter(loc, 0);
			return this.mem.read(loc);
		}

		public void write(L loc, int v, int flip_v) throws TRRException {
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

		public void clock(L loc, int flip_v) throws TRRException {
			this.trr.checkClocks(this.env.clocks);
			this.env.checkClocks();
			for (Loc<Integer> l : this.mem.neighbours(loc)) {
				int attenuation_factor = BLAST_RADIUS - l.distance(loc) + 1;
				this.env.tickCounter(l, attenuation_factor);
			}
			this.trr.tickCounter(loc, 1);
			if (!this.trr.checkSingleCounter(loc)) {
				this.env.resetCounter(loc, 0);
				this.trr.resetCounter(loc);
				read(loc, flip_v);
				for (Loc<Integer> l : this.mem.neighbours(loc)) {
					read((L) l, flip_v);
				}
				throw new TRRException();
			}
			for (Loc<Integer> l : this.mem.neighbours(loc)) {
				Random r = new Random();
				double pr = r.nextDouble();
				int tmp = this.env.counter.map.get(l);
				if (tmp >= RH_THRESHOLD && pr <= FLIP_PROBABILITY) {
					this.mem.flip(l, flip_v);
					for (int i = 0; i < flip_v; i++)
						this.ecc.tweak((L) l, i, (byte) 1);
				}
			}
		}
	}
	// END OF Memory System Definition



	// END OF Row Counter Definition

	// ECC Definition
	public static class ECC {
		public HashMap<L, byte[][]> map;

		public ECC() {
			map = new HashMap<>();
		}

		public boolean validate(L loc, Mem mem) throws ECCCorrectionException, ECCDetectionException {
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
			for (Loc<Integer> l : mem.map.keySet()) {
				validate((L) l, mem);
			}
			return true;
		}

		public void tweak(L loc, int position, byte val) {
			byte[][] tmp = this.map.get(loc);
			tmp[position][0] = val;
			this.map.put(loc, tmp);
		}

		public void add(L loc, int val) {
			String in = Integer.toBinaryString(val);
			while (in.length() < WORD_SIZE)
				in = "0" + in;
			this.map.put(loc, RS.encode(in));
		}

		public byte[][] get(L loc) {
			return this.map.get(loc);
		}
	}
	// END OF ECC Definition

	// TRR Definition
	public static class TRR {
		int counters;
		int radius;
		public HashMap<L, Integer> map;

		public TRR() {
			counters = TRR_COUNTERS;
			radius = TRR_RADIUS;
			map = new HashMap<>();
		}

		public TRR(int counters, HashMap<L, Integer> map, int radius) {
			this.counters = counters;
			this.radius = radius;
			this.map = map;
		}

		public boolean checkSingleCounter(L loc) {
			if (!this.map.containsKey(loc)) {
				return true;
			}
			return this.map.get(loc) < TRR_THRESHOLD;
		}

		public void tickCounter(L loc, int v) {
			if (this.map.containsKey(loc)) {
				int tmp = this.map.get(loc);
				this.map.put(loc, tmp + v);
			} else if (this.map.size() < this.counters) {
				this.map.put(loc, v);
			} else {
				int code = calLocCode(loc, 0);
				L outKey = null;
				boolean swap = false;
				for (Loc l : this.map.keySet()) {
					if (calLocCode((L) l, this.map.get(l)) > code) {
						code = calLocCode((L) l, this.map.get(l));
						outKey = (L) l;
						swap = true;
					}
				}
				if (swap) {
					this.map.remove(outKey);
					this.map.put(loc, v);
				}
			}
		}

		private int calLocCode(L loc, int n) {
			int v = loc.getValue();
			int m = 16;
			return (v % m) ^ (n % m);
		}

		public void resetCounter(L loc) {
			if (this.map.containsKey(loc)) {
				this.map.put(loc, 0);
			}
		}

		public void resetCounters() {
			for (L loc : this.map.keySet()) {
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
