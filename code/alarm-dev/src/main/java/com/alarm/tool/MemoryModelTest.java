package com.alarm.tool;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.alarm.exceptions.TRRException;
import com.alarm.tool.MemoryModel.Aexp;
import com.alarm.tool.MemoryModel.And;
import com.alarm.tool.MemoryModel.Assign;
import com.alarm.tool.MemoryModel.Bexp;
import com.alarm.tool.MemoryModel.BexpA;
import com.alarm.tool.MemoryModel.BexpN;
import com.alarm.tool.MemoryModel.BoolE;
import com.alarm.tool.MemoryModel.Com;
import com.alarm.tool.MemoryModel.If;
import com.alarm.tool.MemoryModel.L;
import com.alarm.tool.MemoryModel.LoE;
import com.alarm.tool.MemoryModel.Loc;
import com.alarm.tool.MemoryModel.MemSystem;
import com.alarm.tool.MemoryModel.Neg;
import com.alarm.tool.MemoryModel.Num;
import com.alarm.tool.MemoryModel.Or;
import com.alarm.tool.MemoryModel.Plus;
import com.alarm.tool.MemoryModel.Seq;
import com.alarm.tool.MemoryModel.Skip;
import com.alarm.tool.MemoryModel.Truth;
import com.alarm.tool.MemoryModel.Var;
import com.alarm.tool.MemoryModel.While;

class MemoryModelTest {

	@Test
	void HalfDoubleTest() throws IllegalArgumentException, TRRException {
		// For this instance to run set you need to set TRR threshold, RH threshold and
		// blast radius properly.
		// You can for example set them as TRR threshold=4 and RH threshold=8,
		// blast_raius=2.
		// Also make sure that you comment out line 116 in Memory model to avoid getting
		// an exception.

		int flips = 5;
		MemSystem mem = new MemSystem();
		int locVal = 0;
		Loc loc[] = new Loc[5];
		for (int i = 0; i < loc.length; i++) {
			loc[i] = new L(i);
			mem.mem.write(loc[i], locVal);
			mem.ecc.add(loc[i], locVal);
		}

		Com assign0 = new Assign(loc[0], new Num(0));
		Com assign4 = new Assign(loc[4], new Aexp(new Plus(), new Var(loc[4]), new Num(1)));

		Com seq = new Seq(assign0, assign4);

		BoolE b = new BexpA(new LoE(), new Var(loc[4]), new Num(30));

		Com whileCom = new While(b, seq);

		Com.eval(whileCom, mem, flips);
		
		assertEquals(0, mem.mem.read(loc[2]));
	}

	@Test
	void SkipTest() throws IllegalArgumentException, TRRException {
		int flips = 0;
		MemSystem mem = new MemSystem();
		Loc loc1 = new L(1);
		Loc loc2 = new L(2);
		int loc1Val = 10;
		int loc2Val = 20;
		mem.mem.write(loc1, loc1Val);
		mem.mem.write(loc2, loc2Val);

		Com skip = new Skip();

		Com.eval(skip, mem, flips);

		assertEquals(loc1Val, mem.mem.read(loc1));
		assertEquals(loc2Val, mem.mem.read(loc2));

		assertEquals(null, mem.env.counter.map.get(loc1));
		assertEquals(null, mem.env.counter.map.get(loc2));

		assertEquals(0, mem.env.clocks);

	}

	@Test
	void AssignTest() throws IllegalArgumentException, TRRException {
		int flips = 0;
		MemSystem mem = new MemSystem();
		Loc loc1 = new L(1);
		Loc loc2 = new L(2);
		int loc1Val = 10;
		int loc2Val = 20;
		mem.mem.write(loc1, loc1Val);
		mem.mem.write(loc2, loc2Val);

		Com assign1 = new Assign(loc1, new Num(30));

		Com.eval(assign1, mem, flips);

		assertEquals(30, mem.mem.read(loc1));
		assertEquals(loc2Val, mem.mem.read(loc2));

		assertEquals(0, (int) mem.env.counter.map.get(loc1));
		assertEquals(1, (int) mem.env.counter.map.get(loc2));

		assertEquals(1, mem.env.clocks);

		Com assign2 = new Assign(loc2, new Num(40));

		Com.eval(assign2, mem, flips);

		assertEquals(30, mem.mem.read(loc1));
		assertEquals(40, mem.mem.read(loc2));

		assertEquals(1, (int) mem.env.counter.map.get(loc1));
		assertEquals(0, (int) mem.env.counter.map.get(loc2));

		assertEquals(2, mem.env.clocks);

		Com assign3 = new Assign(loc1, new Aexp(new Plus(), new Var(loc1), new Num(1)));

		Com.eval(assign3, mem, flips);

		assertEquals(31, mem.mem.read(loc1));
		assertEquals(40, mem.mem.read(loc2));

		assertEquals(0, (int) mem.env.counter.map.get(loc1));
		assertEquals(2, (int) mem.env.counter.map.get(loc2));

		assertEquals(4, mem.env.clocks);
	}

	@Test
	void SeqTest() throws IllegalArgumentException, TRRException {
		int flips = 0;
		MemSystem mem = new MemSystem();
		Loc loc1 = new L(1);
		Loc loc2 = new L(2);
		int loc1Val = 10;
		int loc2Val = 20;
		mem.mem.write(loc1, loc1Val);
		mem.mem.write(loc2, loc2Val);

		Com skip = new Skip();

		Com assign1 = new Assign(loc1, new Num(30));
		Com assign2 = new Assign(loc2, new Num(40));
		Com assign3 = new Assign(loc1, new Aexp(new Plus(), new Var(loc1), new Num(1)));

		Com seq1 = new Seq(assign1, assign2);
		Com seq2 = new Seq(assign1, skip);
		Com seq3 = new Seq(skip, assign2);
		Com seq4 = new Seq(assign3, assign3);

		Com.eval(seq1, mem, flips);

		assertEquals(30, mem.mem.read(loc1));
		assertEquals(40, mem.mem.read(loc2));

		assertEquals(1, (int) mem.env.counter.map.get(loc1));
		assertEquals(0, (int) mem.env.counter.map.get(loc2));

		assertEquals(2, mem.env.clocks);

		mem.mem.write(loc1, loc1Val);
		mem.mem.write(loc2, loc2Val);

		Com.eval(seq2, mem, flips);

		assertEquals(30, mem.mem.read(loc1));
		assertEquals(20, mem.mem.read(loc2));

		assertEquals(0, (int) mem.env.counter.map.get(loc1));
		assertEquals(1, (int) mem.env.counter.map.get(loc2));

		assertEquals(3, mem.env.clocks);

		mem.mem.write(loc1, loc1Val);
		mem.mem.write(loc2, loc2Val);

		Com.eval(seq3, mem, flips);

		assertEquals(10, mem.mem.read(loc1));
		assertEquals(40, mem.mem.read(loc2));

		assertEquals(1, (int) mem.env.counter.map.get(loc1));
		assertEquals(0, (int) mem.env.counter.map.get(loc2));

		assertEquals(4, mem.env.clocks);

		Com.eval(seq4, mem, flips);

		assertEquals(12, mem.mem.read(loc1));
		assertEquals(40, mem.mem.read(loc2));

		assertEquals(0, (int) mem.env.counter.map.get(loc1));
		assertEquals(4, (int) mem.env.counter.map.get(loc2));

		assertEquals(8, mem.env.clocks);
	}

	@Test
	void IfTest() throws IllegalArgumentException, TRRException {
		int flips = 0;
		MemSystem mem = new MemSystem();
		Loc loc1 = new L(1);
		Loc loc2 = new L(2);
		int loc1Val = 10;
		int loc2Val = 20;
		mem.mem.write(loc1, loc1Val);
		mem.mem.write(loc2, loc2Val);

		Com skip = new Skip();

		Com assign1 = new Assign(loc1, new Num(30));
		Com assign2 = new Assign(loc2, new Num(40));
		Com assign3 = new Assign(loc1, new Aexp(new Plus(), new Var(loc1), new Num(1)));

		Com seq1 = new Seq(assign3, assign3);

		BoolE b1 = new Bexp(new Or(), new Truth(true), new Truth(false));
		BoolE b2 = new Bexp(new And(), new Truth(true), new Truth(false));
		BoolE b3 = new BexpA(new LoE(), new Var(loc1), new Num(25));
		BoolE b4 = new BexpN(new Neg(), new BexpA(new LoE(), new Var(loc1), new Num(25)));

		Com if1 = new If(b1, assign1, assign2);
		Com if2 = new If(b2, assign1, assign2);
		Com if3 = new If(b3, assign3, skip);
		Com if4 = new If(b4, skip, seq1);

		Com.eval(if1, mem, flips);

		assertEquals(30, mem.mem.read(loc1));
		assertEquals(loc2Val, mem.mem.read(loc2));

		assertEquals(0, (int) mem.env.counter.map.get(loc1));
		assertEquals(1, (int) mem.env.counter.map.get(loc2));

		assertEquals(1, mem.env.clocks);

		mem.mem.write(loc1, loc1Val);
		mem.mem.write(loc2, loc2Val);

		Com.eval(if2, mem, flips);

		assertEquals(loc1Val, mem.mem.read(loc1));
		assertEquals(40, mem.mem.read(loc2));

		assertEquals(1, (int) mem.env.counter.map.get(loc1));
		assertEquals(0, (int) mem.env.counter.map.get(loc2));

		assertEquals(2, mem.env.clocks);

		mem.mem.write(loc1, loc1Val);
		mem.mem.write(loc2, loc2Val);

		Com.eval(if3, mem, flips);

		assertEquals(loc1Val + 1, mem.mem.read(loc1));
		assertEquals(loc2Val, mem.mem.read(loc2));

		assertEquals(0, (int) mem.env.counter.map.get(loc1));
		assertEquals(3, (int) mem.env.counter.map.get(loc2));

		assertEquals(5, mem.env.clocks);

		mem.mem.write(loc1, loc1Val);
		mem.mem.write(loc2, loc2Val);

		Com.eval(if4, mem, flips);

		assertEquals(loc1Val + 2, mem.mem.read(loc1));
		assertEquals(loc2Val, mem.mem.read(loc2));

		assertEquals(0, (int) mem.env.counter.map.get(loc1));
		assertEquals(8, (int) mem.env.counter.map.get(loc2));

		assertEquals(10, mem.env.clocks);
	}

	@Test
	void WhileTest() throws IllegalArgumentException, TRRException {
		int flips = 0;
		MemSystem mem = new MemSystem();
		Loc loc1 = new L(1);
		Loc loc2 = new L(2);
		int loc1Val = 10;
		int loc2Val = 20;
		mem.mem.write(loc1, loc1Val);
		mem.mem.write(loc2, loc2Val);

		Com skip = new Skip();

		Com assign1 = new Assign(loc1, new Num(30));
		Com assign2 = new Assign(loc2, new Num(40));
		Com assign3 = new Assign(loc1, new Aexp(new Plus(), new Var(loc1), new Num(1)));

		Com seq1 = new Seq(assign3, assign3);
		Com seq2 = new Seq(assign1, assign2);

		BoolE b1 = new Bexp(new Or(), new Truth(true), new Truth(false));
		BoolE b2 = new Bexp(new And(), new Truth(true), new Truth(false));
		BoolE b3 = new BexpA(new LoE(), new Var(loc1), new Num(24));
		BoolE b4 = new BexpN(new Neg(), new BexpA(new LoE(), new Var(loc1), new Num(25)));
		BoolE b5 = new BexpN(new Neg(), new BexpN(new Neg(), b2));

		Com if1 = new If(b1, assign1, assign2);

		Com while1 = new While(b3, assign3);
		Com while2 = new While(b4, assign3);
		Com while3 = new While(b3, seq1);
		Com while4 = new While(b3, seq2);
		Com while5 = new While(b5, skip);
		Com while6 = new While(b3, if1);

		Com.eval(while1, mem, flips);

		assertEquals(25, mem.mem.read(loc1));
		assertEquals(loc2Val, mem.mem.read(loc2));

		assertEquals(0, (int) mem.env.counter.map.get(loc1));
		assertEquals(46, (int) mem.env.counter.map.get(loc2));

		assertEquals(46, mem.env.clocks);

		mem.mem.write(loc1, loc1Val);
		mem.mem.write(loc2, loc2Val);

		Com.eval(while2, mem, flips);

		assertEquals(loc1Val, mem.mem.read(loc1));
		assertEquals(loc2Val, mem.mem.read(loc2));

		assertEquals(0, (int) mem.env.counter.map.get(loc1));
		assertEquals(47, (int) mem.env.counter.map.get(loc2));

		assertEquals(47, mem.env.clocks);

		mem.mem.write(loc1, loc1Val);
		mem.mem.write(loc2, loc2Val);

		Com.eval(while3, mem, flips);

		assertEquals(26, mem.mem.read(loc1));
		assertEquals(loc2Val, mem.mem.read(loc2));

		assertEquals(0, (int) mem.env.counter.map.get(loc1));
		assertEquals(88, (int) mem.env.counter.map.get(loc2));

		assertEquals(88, mem.env.clocks);

		mem.mem.write(loc1, loc1Val);
		mem.mem.write(loc2, loc2Val);

		Com.eval(while4, mem, flips);

		assertEquals(30, mem.mem.read(loc1));
		assertEquals(40, mem.mem.read(loc2));

		assertEquals(0, (int) mem.env.counter.map.get(loc1));
		assertEquals(1, (int) mem.env.counter.map.get(loc2));

		assertEquals(92, mem.env.clocks);

		mem.mem.write(loc1, loc1Val);
		mem.mem.write(loc2, loc2Val);

		Com.eval(while5, mem, flips);

		assertEquals(loc1Val, mem.mem.read(loc1));
		assertEquals(loc2Val, mem.mem.read(loc2));

		assertEquals(0, (int) mem.env.counter.map.get(loc1));
		assertEquals(1, (int) mem.env.counter.map.get(loc2));

		assertEquals(92, mem.env.clocks);

		mem.mem.write(loc1, loc1Val);
		mem.mem.write(loc2, loc2Val);

		Com.eval(while6, mem, flips);

		assertEquals(30, mem.mem.read(loc1));
		assertEquals(loc2Val, mem.mem.read(loc2));

		assertEquals(0, (int) mem.env.counter.map.get(loc1));
		assertEquals(4, (int) mem.env.counter.map.get(loc2));

		assertEquals(95, mem.env.clocks);
	}

}
