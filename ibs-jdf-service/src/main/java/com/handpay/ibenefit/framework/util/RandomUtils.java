package com.handpay.ibenefit.framework.util;

import java.util.Random;

public final class RandomUtils {
	private static final int DECIMAL_BASE = 10;

	private RandomUtils() {
	}

	public static int getSpecificDigitRandom(int length) {
		int result = 0;
		Random random = new Random();
		for (int i = 0; i < length; i++) {
			result += DECIMAL_BASE * random.nextInt(DECIMAL_BASE);
		}
		return result;
	}

	public static int getSpecificDigitRandomWithMax(int length, int max) {
		int result = getSpecificDigitRandom(length);
		if (result <= max) {
			return result;
		} else {
			return getSpecificDigitRandomWithMax(length, max);
		}
	}
}
