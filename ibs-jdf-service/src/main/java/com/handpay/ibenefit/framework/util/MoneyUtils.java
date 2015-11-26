package com.handpay.ibenefit.framework.util;

public final class MoneyUtils {
	private MoneyUtils() {
	}

	public static void main(String args[]) {
		System.out.println(MoneyUtils.change("100001"));
	}

	private final static String[] CN_DIGITS = { "零", "壹", "貳", "叁", "肆", "伍", "陆", "柒", "捌", "玖", };

	public static String change(String moneyValue) {
		String value = moneyValue.replaceFirst("^0+", "");
		value = value.replaceAll(",", "");
		int dot_pos = value.indexOf('.');
		String int_value;
		if (dot_pos == -1) {
			int_value = value;
		} else {
			int_value = value.substring(0, dot_pos);
		}

		int len = int_value.length();
		if (len > 16)
			return "Too big";
		StringBuilder cn_currency = new StringBuilder();
		String[] CN_Carry = new String[] { "", "万", "亿", "万" };
		int cnt = len / 4 + (len % 4 == 0 ? 0 : 1);
		int partLen = len - (cnt - 1) * 4;
		String partValue = null;
		boolean bZero = false;
		String curCN = null;
		for (int i = 0; i < cnt; i++) {
			partValue = int_value.substring(0, partLen);
			int_value = int_value.substring(partLen);
			curCN = part2Cn(partValue, i != 0 && !"零".equals(curCN));
			if (bZero && !"零".equals(curCN)) {
				cn_currency.append("零");
				bZero = false;
			}
			if ("零".equals(curCN))
				bZero = true;
			if (!"零".equals(curCN)) {
				cn_currency.append(curCN);
				cn_currency.append(CN_Carry[cnt - 1 - i]);
			}
			partLen = 4;
			partValue = null;
		}
//		cn_currency.append("元");
//		int fv1 = Integer.parseInt(fraction_value.substring(0, 1));
//		int fv2 = Integer.parseInt(fraction_value.substring(1, 2));
//		if (fv1 + fv2 == 0) {
//			cn_currency.append("整");
//		} else {
//			cn_currency.append(CN_Digits[fv1]).append("角");
//			cn_currency.append(CN_Digits[fv2]).append("分");
//		}
		return cn_currency.toString();

	}

	private static String part2Cn(String partValue, boolean bInsertZero) {
		partValue = partValue.replaceFirst("^0+", "");
		int len = partValue.length();
		if (len == 0)
			return "零";
		StringBuilder sbResult = new StringBuilder();
		int digit;
		String[] CN_Carry = new String[] { "", "拾", "佰", "仟" };
		for (int i = 0; i < len; i++) {
			digit = Integer.parseInt(partValue.substring(i, i + 1));
			if (digit != 0) {
				sbResult.append(CN_DIGITS[digit]);
				sbResult.append(CN_Carry[len - 1 - i]);
			} else {
				if (i != len - 1 && Integer.parseInt(partValue.substring(i + 1, i + 2)) != 0)
					sbResult.append("零");
			}
		}
		if (bInsertZero && len != 4){
			sbResult.insert(0, "零");
		}
		return sbResult.toString();
	}
}
