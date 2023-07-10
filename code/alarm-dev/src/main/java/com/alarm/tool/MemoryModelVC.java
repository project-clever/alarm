package com.alarm.tool;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

import com.alarm.exceptions.ECCCorrectionException;
import com.alarm.exceptions.ECCDetectionException;
import com.alarm.exceptions.TRRException;
import com.backblaze.erasure.RS;
import com.backblaze.erasure.ReedSolomon;

/**
 * This is a simplified model for DRAM implemented in a form of a Java class.
 * This model contains Target Row Refresh(TRR) and Error Correction Code(ECC)
 * implementations.
 *
 * @author ########
 */

public class MemoryModelVC {

	public static final int PLUS = 1;
	public static final int MINUS = 2;
	public static final int MUL = 3;

	public static final int AND = 4;
	public static final int OR = 5;

	public static final int EQU = 6;
	public static final int LOE = 7;

	public static final int NEG = 8;

	public static int MEMORY_SIZE = Learner.MEMORY_SIZE;
	public static int TRR_THRESHOLD = Learner.TRR_THRESHOLD;
	public static int TRR_COUNTERS = Learner.TRR_COUNTERS;
	public static int RH_THRESHOLD = Learner.RH_THRESHOLD;
	public static int REFRESH_INTERVAL = Learner.REFRESH_INTERVAL;
	public static int BLAST_RADIUS = Learner.BLAST_RADIUS;
	public static int TRR_RADIUS = Learner.TRR_RADIUS;
	public static boolean ECC_STATUS = Learner.ECC_STATUS;

	public static final int FLIP_BITS[] = { 0, 1, 3, 7, 15, 31, 63, 127, 255, 511, 1023 };
	public static final double FLIP_PROBABILITY = 1.0;

	public static final int WORD_SIZE = Learner.WORD_SIZE;
	public static final int Parity_SIZE = Learner.Parity_SIZE;
	public static final int CODE_SIZE = Learner.CODE_SIZE;

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
				int l = Loc.getValue(loc);
				if (l % 2 == 0) {
					if (l + 1 < MEMORY_SIZE) {
						read(new L(l + 1), flip_v);
					}
				} else {
					read(new L(l - 1), flip_v);
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

	// Memory Definition
	public static class Mem {
		HashMap<Loc, Integer> map;

		public Mem() {
			this.map = new HashMap<Loc, Integer>();
		}

		public Mem(HashMap<Loc, Integer> map) {
			this.map = map;
		}

		public int read(Loc loc) {
			return this.map.get(loc);
		}

		public void write(Loc loc, int n) {
			this.map.put(loc, n);
		}

		public void flip(Loc loc, int v) {
			int tmp = this.map.get(loc);
			this.map.put(loc, tmp ^ FLIP_BITS[v]);
		}

		public HashSet<Loc> neighbours(Loc loc) {
			HashSet<Loc> out = new HashSet<>();
			for (Loc l : this.map.keySet()) {
				if (distance(loc, l) <= BLAST_RADIUS && distance(loc, l) > 0)
					out.add(l);
			}
			return out;
		}

		public int distance(Loc l1, Loc l2) {
			int l1_v = Loc.getValue(l1);
			int l2_v = Loc.getValue(l2);
			return Math.abs(l1_v - l2_v);
		}
	}
	// ENDO OF Memory Definition

	// Memory Location Definition
	public interface Loc extends Comparable<Loc> {
		public static String eval(Loc l) throws IllegalArgumentException {
			if (l instanceof L) {
				L s = (L) l;
				return "Loc" + s.loc;
			} else {
				throw new IllegalArgumentException("Invalid Loc!");
			}
		}

		public static int getValue(Loc l) throws IllegalArgumentException {
			if (l instanceof L) {
				L s = (L) l;
				return s.loc;
			} else {
				throw new IllegalArgumentException("Invalid Loc!");
			}
		}
	}

	public static class L implements Loc {
		int loc;

		public L(int loc) {
			this.loc = loc;
		}

		@Override
		public String toString() {
			return loc + "";
		}

		@Override
		public int compareTo(Loc l) {
			L s = (L) l;
			return loc - s.loc;
		}

		@Override
		public boolean equals(Object loc) {
			if (loc instanceof L) {
				L s = (L) loc;
				return this.loc == s.loc;
			}
			return false;
		}

		@Override
		public int hashCode() {
			return (41 * (41 + this.loc) + this.loc);
		}

	}
	// END OF Memory Location Definition

	// Environment Definition
	public static class Env {
		public int clocks;
		public CountRowVars counter;

		public Env() {
			this.clocks = 0;
			this.counter = new CountRowVars();
		}

		public Env(int clocks, CountRowVars counter) {
			this.clocks = clocks;
			this.counter = counter;
		}

		public void checkClocks() {
			this.clocks = this.clocks + 1;

			if (this.clocks >= REFRESH_INTERVAL) {
				this.clocks = 1;
				this.resetCounters();
			}
		}

		public void tickCounter(Loc loc, int v) {
			if (this.counter.map.containsKey(loc)) {
				int tmp = this.counter.map.get(loc);
				this.counter.map.put(loc, tmp + v);
			} else {
				this.counter.map.put(loc, v);
			}
		}

		public void resetCounter(Loc loc, int v) {
			this.counter.map.put(loc, v);
		}

		public void resetCounters() {
			for (Loc loc : this.counter.map.keySet()) {
				resetCounter(loc, 0);
			}
		}
	}
	// END OF Environment Definition

	// Row Counter Definition
	public static class CountRowVars {
		public HashMap<Loc, Integer> map;

		public CountRowVars() {
			this.map = new HashMap<Loc, Integer>();
		}

		public CountRowVars(HashMap<Loc, Integer> map) {
			this.map = map;
		}
	}
	// END OF Row Counter Definition

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
			if (!this.map.containsKey(loc)) {
				return true;
			}
			return this.map.get(loc) < TRR_THRESHOLD;
		}

		public void tickCounter(Loc loc, int v) {
			if (this.map.containsKey(loc)) {
				int tmp = this.map.get(loc);
				this.map.put(loc, tmp + v);
			} else if (this.map.size() < this.counters) {
				this.map.put(loc, v);
			} else {
				int code = calLocCode(loc, 0);
				Loc outKey = null;
				boolean swap = false;
				for (Loc l : this.map.keySet()) {
					if (calLocCode(l, this.map.get(l)) > code) {
						code = calLocCode(l, this.map.get(l));
						outKey = l;
						swap = true;
					}
				}
				if (swap) {
					this.map.remove(outKey);
					this.map.put(loc, v);
				}
			}
		}

		private int calLocCode(Loc loc, int n) {
			int v = Loc.getValue(loc);
			int m = 16;
			return (v % m) ^ (n % m);
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

	// Below is the definition of a simple while language only for the matter
	// testing the memory
	// Commands Definition
	public interface Com {
		public static MemSystem eval(Com c, MemSystem mem, int flip_v) throws IllegalArgumentException, TRRException {
			if (c instanceof Skip) {
				return mem;
			} else if (c instanceof Assign) {
				Assign s = (Assign) c;
				mem.write(s.loc, ArithE.eval(s.a, mem, flip_v), flip_v);
				return mem;
			} else if (c instanceof Seq) {
				Seq s = (Seq) c;
				return Com.eval(s.c2, (Com.eval(s.c1, mem, flip_v)), flip_v);
			} else if (c instanceof If) {
				If s = (If) c;
				if (BoolE.eval(s.b, mem, flip_v))
					return Com.eval(s.c1, mem, flip_v);
				else
					return Com.eval(s.c2, mem, flip_v);
			} else if (c instanceof While) {
				While s = (While) c;
				if (BoolE.eval(s.b, mem, flip_v))
					return Com.eval(new Seq(s.c, c), mem, flip_v);
				else
					return Com.eval(new Skip(), mem, flip_v);
			} else {
				throw new IllegalArgumentException("Invalid Com!");
			}
		}
	}

	public static class Skip implements Com {
		public Skip() {
		}
	}

	public static class Assign implements Com {

		Loc loc;
		ArithE a;

		public Assign(Loc loc, ArithE a) {
			this.loc = loc;
			this.a = a;
		}

	}

	public static class Seq implements Com {

		Com c1;
		Com c2;

		public Seq(Com c1, Com c2) {
			this.c1 = c1;
			this.c2 = c2;
		}

	}

	public static class If implements Com {

		BoolE b;
		Com c1;
		Com c2;

		public If(BoolE b, Com c1, Com c2) {
			this.b = b;
			this.c1 = c1;
			this.c2 = c2;
		}

	}

	public static class While implements Com {

		BoolE b;
		Com c;

		public While(BoolE b, Com c) {
			this.b = b;
			this.c = c;
		}

	}
	// END OF Commands Definition

	// Arithmetic Expressions Definition
	public interface ArithE {
		public static int eval(ArithE a, MemSystem mem, int flip_v) throws IllegalArgumentException, TRRException {
			if (a instanceof Num) {
				Num s = (Num) a;
				return s.n;
			} else if (a instanceof Var) {
				Var s = (Var) a;
				return mem.read(s.loc, flip_v);
			} else if (a instanceof Aexp) {
				Aexp s = (Aexp) a;
				int op = Aop.eval(s.op);
				if (op == PLUS)
					return eval(s.a1, mem, flip_v) + eval(s.a2, mem, flip_v);
				else if (op == MINUS)
					return eval(s.a1, mem, flip_v) - eval(s.a2, mem, flip_v);
				else if (op == MUL)
					return eval(s.a1, mem, flip_v) * eval(s.a2, mem, flip_v);
				else
					throw new IllegalArgumentException("Invalid Operator!");
			} else {
				throw new IllegalArgumentException("Invalid ArithE!");
			}
		}
	}

	public static class Num implements ArithE {

		int n;

		public Num(int n) {
			this.n = n;
		}
	}

	public static class Var implements ArithE {

		Loc loc;

		public Var(Loc loc) {
			this.loc = loc;
		}
	}

	public static class Aexp implements ArithE {

		Aop op;
		ArithE a1;
		ArithE a2;

		public Aexp(Aop op, ArithE a1, ArithE a2) {
			this.op = op;
			this.a1 = a1;
			this.a2 = a2;
		}
	}
	// END OF Arithmetic Expressions Definition

	// Boolean Expressions Definition
	public interface BoolE {
		public static boolean eval(BoolE b, MemSystem mem, int flip_v) throws IllegalArgumentException, TRRException {
			if (b instanceof Truth) {
				Truth s = (Truth) b;
				return s.t;
			} else if (b instanceof Bexp) {
				Bexp s = (Bexp) b;
				int op = Bop.eval(s.op);
				if (op == AND)
					return eval(s.b1, mem, flip_v) && eval(s.b2, mem, flip_v);
				else if (op == OR)
					return eval(s.b1, mem, flip_v) || eval(s.b2, mem, flip_v);
				else
					throw new IllegalArgumentException("Invalid Operator!");
			} else if (b instanceof BexpA) {
				BexpA s = (BexpA) b;
				int op = BopA.eval(s.op);
				if (op == EQU)
					return ArithE.eval(s.a1, mem, flip_v) == ArithE.eval(s.a2, mem, flip_v);
				else if (op == LOE)
					return ArithE.eval(s.a1, mem, flip_v) <= ArithE.eval(s.a2, mem, flip_v);
				else
					throw new IllegalArgumentException("Invalid Operator!");
			} else if (b instanceof BexpN) {
				BexpN s = (BexpN) b;
				int op = BopN.eval(s.op);
				if (op == NEG)
					return !eval(s.b, mem, flip_v);
				else
					throw new IllegalArgumentException("Invalid Operator!");
			} else {
				throw new IllegalArgumentException("Invalid BoolE!");
			}
		}
	}

	public static class Truth implements BoolE {

		boolean t;

		public Truth(boolean t) {
			this.t = t;
		}
	}

	public static class Bexp implements BoolE {

		Bop op;
		BoolE b1;
		BoolE b2;

		public Bexp(Bop op, BoolE b1, BoolE b2) {
			this.op = op;
			this.b1 = b1;
			this.b2 = b2;
		}
	}

	public static class BexpA implements BoolE {

		BopA op;
		ArithE a1;
		ArithE a2;

		public BexpA(BopA op, ArithE a1, ArithE a2) {
			this.op = op;
			this.a1 = a1;
			this.a2 = a2;
		}
	}

	public static class BexpN implements BoolE {

		BopN op;
		BoolE b;

		public BexpN(BopN op, BoolE b) {
			this.op = op;
			this.b = b;
		}
	}
	// END OF Boolean Expressions Definition

	// Arithmetic Operations Definition
	public interface Aop {
		public static int eval(Aop op) throws IllegalArgumentException {
			if (op instanceof Plus) {
				return PLUS;
			} else if (op instanceof Minus) {
				return MINUS;
			} else if (op instanceof Mul) {
				return MUL;
			} else {
				throw new IllegalArgumentException("Invalid Operator!");
			}
		}
	}

	public static class Plus implements Aop {
	}

	public static class Minus implements Aop {
	}

	public static class Mul implements Aop {
	}
	// END OF Arithmetic Operations Definition

	// Boolean Operations Definition
	public interface Bop {
		public static int eval(Bop op) throws IllegalArgumentException {
			if (op instanceof And) {
				return AND;
			} else if (op instanceof Or) {
				return OR;
			} else {
				throw new IllegalArgumentException("Invalid Operator!");
			}
		}
	}

	public static class And implements Bop {
	}

	public static class Or implements Bop {
	}

	public interface BopA {
		public static int eval(BopA op) throws IllegalArgumentException {
			if (op instanceof Equ) {
				return EQU;
			} else if (op instanceof LoE) {
				return LOE;
			} else {
				throw new IllegalArgumentException("Invalid Operator!");
			}
		}
	}

	public static class Equ implements BopA {
	}

	public static class LoE implements BopA {
	}

	public interface BopN {
		public static int eval(BopN op) throws IllegalArgumentException {
			if (op instanceof Neg) {
				return NEG;
			} else {
				throw new IllegalArgumentException("Invalid Operator!");
			}
		}
	}

	public static class Neg implements BopN {
	}
	// END OF Boolean Operations Definition

}
