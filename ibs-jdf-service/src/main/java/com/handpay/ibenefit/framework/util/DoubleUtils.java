package com.handpay.ibenefit.framework.util;

import java.math.BigDecimal;

public final class DoubleUtils {
	private DoubleUtils(){
	}
	
	public static Double toFixFraction(double in,int fractionDigit){
		BigDecimal  b = new BigDecimal(in);  
		return b.setScale(fractionDigit,BigDecimal.ROUND_HALF_UP).doubleValue();  
	}
	
	public static double parseDouble(String str) {  
        double res = 0.0;  
        try {  
            res = Double.parseDouble(str);  
        } catch (Exception e) {  
            // System.out.println("parseDouble error : =0.0");  
        }  
        return res;  
    }  
	
	public static double parseDouble(String str, double defaultValue) {  
        double res = 0.0;  
        try {  
            res = Double.parseDouble(str);  
        } catch (Exception e) {  
            res = defaultValue;  
        }  
        return res;  
    }  
	
}
