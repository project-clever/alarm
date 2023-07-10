package com.alarm.helper;

public class HammingCode {

	// print elements of array
	static void print(int ar[]) {
		for (int i = 0; i < ar.length; i++) {
			System.out.print(ar[i]);
		}
		System.out.println();
	}

	// calculating value of redundant bits
	static int[] calculation(int[] ar, int r) {
		for (int i = 0; i < r; i++) {
			int x = (int) Math.pow(2, i);
			for (int j = 1; j < ar.length; j++) {
				if (((j >> i) & 1) == 1) {
					if (x != j)
						ar[x] = ar[x] ^ ar[j];
				}
			}
//            System.out.println("r" + x + " = " + ar[x]);
		}

		return ar;
	}

	static int[] generateCode(String str, int M, int r) {
		int[] ar = new int[r + M + 1];
		int j = 0;
		for (int i = 1; i < ar.length; i++) {
			if ((Math.ceil(Math.log(i) / Math.log(2)) - Math.floor(Math.log(i) / Math.log(2))) == 0) {

				// if i == 2^n for n in (0, 1, 2, .....)
				// then ar[i]=0
				// codeword[i] = 0 ----
				// redundant bits are initialized
				// with value 0
				ar[i] = 0;
			} else {

				// codeword[i] = dataword[j]
				ar[i] = (int) (str.charAt(j) - '0');
				j++;
			}
		}
		return ar;
	}

	public static int[] getCode(String m) {
		int M = m.length();
		int r = 1;

		while (Math.pow(2, r) < (M + r + 1)) {
			r++;
		}
		int[] ar = generateCode(m, M, r);
		ar = calculation(ar, r);

		for (int i = 1; i < ar.length; i++) {
			ar[0] = (ar[0] + ar[i]) % 2;
		}
		return ar;
	}

	public static int getInt(int[] code) {
		int out = 0;
		int size = code.length;
		for (int i = 0; i < size; i++) {
			out += code[size - i - 1] * Math.pow(2, i);
		}
		return out;
	}

	public static int[] getArr(int code, int size) {

		int out[] = new int[size];
		for (int i = size - 1; i >= 0; i--) {
			out[i] = code % 2;
			code = code >> 1;
		}
		return out;
	}

	public static String getStr(int code, int size) {
		String out = "";
		out = Integer.toBinaryString(code);
		while (out.length() < size)
			out = "0" + out;
		return out;
	}

	public static String getMessage(int[] code, int size) {
		String out = "";

		for (int i = size - 1; i > 0; i--) {
			if ((Math.ceil(Math.log(i) / Math.log(2)) - Math.floor(Math.log(i) / Math.log(2))) != 0) {
				out = code[i] + out;
			}
		}

		return out;
	}

	public static String getMessage(String s_code, int size) {
		int code[] = new int[s_code.length()];
		for (int i = 0; i < code.length; i++) {
			code[i] = Integer.parseInt(s_code.charAt(i) + "");
		}
		return getMessage(code, size);
	}

	public static int getParity(String s_code) {
		return s_code.charAt(0) - '0';
	}

	public static int getParity(int[] code) {
		return code[0];
	}

	public static int calParity(String s_code) {
		int out = 0;
		for (int i = 1; i < s_code.length(); i++) {
			out = (out + Integer.parseInt(s_code.charAt(i) + "")) % 2;
		}
		return out;
	}

	public static int calParity(int[] code) {
		int out = 0;
		for (int i = 1; i < code.length; i++) {
			out = (out + code[i]) % 2;
		}
		return out;
	}

	public static String getCheckSum(int[] code) {
		int c2 = code[4] ^ code[5] ^ code[6] ^ code[7];
		int c1 = code[2] ^ code[3] ^ code[6] ^ code[7];
		int c0 = code[1] ^ code[3] ^ code[5] ^ code[7];
		String out = "" + c2 + c1 + c0;
		return out;
	}

	public static String getCheckSum(String s_code) {
		int c2 = Integer.parseInt("" + s_code.charAt(4)) ^ Integer.parseInt("" + s_code.charAt(5))
				^ Integer.parseInt("" + s_code.charAt(6)) ^ Integer.parseInt("" + s_code.charAt(7));
		int c1 = Integer.parseInt("" + s_code.charAt(2)) ^ Integer.parseInt("" + s_code.charAt(3))
				^ Integer.parseInt("" + s_code.charAt(6)) ^ Integer.parseInt("" + s_code.charAt(7));
		int c0 = Integer.parseInt("" + s_code.charAt(1)) ^ Integer.parseInt("" + s_code.charAt(3))
				^ Integer.parseInt("" + s_code.charAt(5)) ^ Integer.parseInt("" + s_code.charAt(7));
		String out = "" + c2 + c1 + c0;
		return out;
	}

	// Driver code
	public static void main(String[] args) {

		// input message
		String str = "1111";
		int M = str.length();
		int r = 1;

		while (Math.pow(2, r) < (M + r + 1)) {
			// r is number of redundant bits
			r++;
		}
		int[] ar = generateCode(str, M, r);

		System.out.println("Generated hamming code ");
		ar = calculation(ar, r);
		for (int i = 1; i < ar.length; i++) {
			ar[0] = (ar[0] + ar[i]) % 2;
		}

		print(ar);
		System.out.println(getMessage(ar, ar.length));

		System.out.println("Generated Second hamming code ");
		ar = getCode(str);
		print(ar);

		System.out.println(getInt(ar));
		print(getArr(getInt(ar), ar.length));

		System.out.println(getMessage(ar, ar.length));
	}
}
