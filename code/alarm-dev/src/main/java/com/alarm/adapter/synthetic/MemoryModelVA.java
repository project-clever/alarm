package com.alarm.adapter.synthetic;

import java.util.HashMap;
import java.util.Random;

import com.alarm.adapter.synthetic.components.*;
import com.alarm.exceptions.ECCCorrectionException;
import com.alarm.exceptions.ECCDetectionException;
import com.alarm.exceptions.TRRException;
import com.alarm.tool.Learner;
import com.backblaze.erasure.RS;
import com.backblaze.erasure.ReedSolomon;

//test


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
				int attenuation_factor = BLAST_RADIUS - loc.distance(l) + 1;
				this.env.tickCounter(l, attenuation_factor);
			}
			this.trr.tickCounter((L) loc, 1);
			if (!this.trr.checkSingleCounter((L) loc)) {
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


	// ECC Definition
	public static class ECC extends ECCBase<L> {

		public ECC() {
			super();
		}

		@Override
		public boolean validate(L loc, Mem mem) throws ECCCorrectionException, ECCDetectionException {
			if (!ECC_STATUS)
				return true;
			String code = Integer.toBinaryString(mem.read(loc));
			while (code.length() < WORD_SIZE)
				code = "0" + code;

			byte[] tmpBuffer = new byte[Parity_SIZE];
			ReedSolomon reedSolomon = ReedSolomon.create(WORD_SIZE, Parity_SIZE);
			boolean isValid = reedSolomon.isParityCorrect(this.map.get(loc), 0, Parity_SIZE, tmpBuffer);
			if (!isValid)
				throw new ECCCorrectionException();

			return true;
		}

		@Override
		public boolean validateAll(Mem mem) throws ECCCorrectionException, ECCDetectionException {
			for (Loc<Integer> l : mem.map.keySet()) {
				validate((L) l, mem);
			}
			return true;
		}

		@Override
		public void tweak(L loc, int position, byte val) {
			byte[][] tmp = this.map.get(loc);
			tmp[position][0] = val;
			this.map.put(loc, tmp);
		}

		@Override
		public void add(L loc, int val) {
			String in = Integer.toBinaryString(val);
			while (in.length() < WORD_SIZE)
				in = "0" + in;
			this.map.put(loc, RS.encode(in));
		}

		@Override
		public byte[][] get(L loc) {
			return this.map.get(loc);
		}
	}
	// END OF ECC Definition

	// TRR Definition
	public static class TRR extends TRRBase<L>{

		public TRR() {
			super();
		}

		public TRR(int counters, HashMap<L, Integer> map, int radius) {
			super(counters, map, radius);
		}

		@Override
		public boolean checkSingleCounter(L loc) {
			return this.map.get(loc) < TRR_THRESHOLD;
		}

		@Override
		public void tickCounter(L loc, int v) {
			if (this.map.containsKey(loc)) {
				int tmp = this.map.get(loc);
				this.map.put(loc, tmp + v);
			} else if (this.map.size() < this.counters) {
				this.map.put(loc, v);
			} else {
				int min = Integer.MAX_VALUE;
				L minKey = null;
				for (L l : this.map.keySet()) {
					if (this.map.get(l) < min) {
						min = this.map.get(l);
						minKey = l;
					}
				}
				this.map.remove(minKey);
				this.map.put(loc, v);
			}
		}
	}
	// END OF TRR Definition
}
