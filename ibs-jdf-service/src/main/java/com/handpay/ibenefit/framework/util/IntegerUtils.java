package com.handpay.ibenefit.framework.util;

public final class IntegerUtils {
	private IntegerUtils() {
	}

	public static int parseInt(String str) {
		int res = 0;
		try {
			res = Integer.parseInt(str);
		} catch (Exception e) {
		}
		return res;
	}

	public static int parseInt(String str, int defaultValue) {
		int res = 0;
		try {
			res = Integer.parseInt(str);
		} catch (Exception e) {
			res = defaultValue;
		}
		return res;
	}
}
