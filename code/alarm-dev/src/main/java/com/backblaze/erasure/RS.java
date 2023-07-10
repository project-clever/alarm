package com.backblaze.erasure;

import java.io.IOException;

public class RS {

	public static final int DATA_SHARDS = 6;
	public static final int PARITY_SHARDS = 4;
	public static final int TOTAL_SHARDS = DATA_SHARDS + PARITY_SHARDS;
	public static final int BYTES_IN_INT = 16;

	public static void main(String[] arguments) throws IOException {

		String input = "000000";

		int inputSize = input.length();
		final int storedSize = inputSize + BYTES_IN_INT;
		final int shardSize = (storedSize + DATA_SHARDS - 1) / DATA_SHARDS;

		byte[][] shards = encode(input);

//    	shards[0][0] = (byte) 1;
//    	shards[0][1] = (byte) 1;
//    	shards[1][0] = (byte) 1;
//    	shards[1][1] = (byte) 1;
//    	shards[2][0] = (byte) 1;
		shards[2][1] = (byte) 1;
//    	shards[3][0] = (byte) 1;
		shards[3][1] = (byte) 1;
//    	shards[4][0] = (byte) 1;
		shards[4][1] = (byte) 1;
//    	shards[5][0] = (byte) 1;
//    	shards[5][1] = (byte) 1;

		final boolean[] shardPresent = new boolean[TOTAL_SHARDS];
		for (int i = 0; i < TOTAL_SHARDS; i++) {
			shardPresent[i] = true;
		}

		shardPresent[2] = false;
		shardPresent[3] = false;
		shardPresent[4] = false;
		shardPresent[5] = false;
//        shardPresent[6] = false;

//        byte []tmpBuffer = new byte[PARITY_SHARDS];        
//        System.out.println(reedSolomon.isParityCorrect(shards, 0, PARITY_SHARDS, tmpBuffer));

		decode(shards, shardPresent, shardSize, inputSize);

		String out = array2Text(shards, shardSize, inputSize);
		System.out.println("recovered: " + out);

	}

	public static byte[][] encode(String input) {

		final int inputSize = input.length();
		final int storedSize = inputSize + BYTES_IN_INT;
		final int shardSize = (storedSize + DATA_SHARDS - 1) / DATA_SHARDS;
		final int bufferSize = shardSize * DATA_SHARDS;
		final byte[] allBytes = new byte[bufferSize];

		byte[] tmp = input.getBytes();
		for (int i = 0; i < inputSize; i++)
			allBytes[i + BYTES_IN_INT] = tmp[i];

		byte[][] shards = new byte[TOTAL_SHARDS][shardSize];

		for (int i = 0; i < DATA_SHARDS; i++) {
			System.arraycopy(allBytes, i * shardSize, shards[i], 0, shardSize);
		}

		ReedSolomon reedSolomon = ReedSolomon.create(DATA_SHARDS, PARITY_SHARDS);
		reedSolomon.encodeParity(shards, 0, shardSize);

		return shards;
	}

	public static void decode(byte[][] shards, boolean[] shardPresent, int shardSize, int inputSize) {

		ReedSolomon reedSolomon = ReedSolomon.create(DATA_SHARDS, PARITY_SHARDS);
		reedSolomon.decodeMissing(shards, shardPresent, 0, shardSize);

	}

	public static String array2Text(byte[][] shards, int shardSize, int inputSize) {
		byte[] allBytes = new byte[shardSize * DATA_SHARDS];
		for (int i = 0; i < DATA_SHARDS; i++) {
			System.arraycopy(shards[i], 0, allBytes, shardSize * i, shardSize);
		}

		String out = new String(allBytes, BYTES_IN_INT, inputSize);

		return out;
	}
}
