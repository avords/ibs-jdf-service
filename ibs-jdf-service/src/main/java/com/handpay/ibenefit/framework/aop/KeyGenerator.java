package com.handpay.ibenefit.framework.aop;

import java.util.Date;

public final class KeyGenerator {

	private static final int DEFAULT_KEY_SIZE = 30;

	private KeyGenerator() {
	}

	public static String getCacheKey(String targetName, String methodName, Object[] arguments) {
        StringBuilder sb = new StringBuilder(DEFAULT_KEY_SIZE);
        sb.append(targetName).append(".").append(methodName);
        if ((arguments != null) && (arguments.length != 0)) {
            for (int i = 0; i < arguments.length; i++) {
            	if(arguments[i]!=null){
            		//Parameter is Array
            		if(arguments[i] instanceof Object []){
            			for(Object arg : (Object[])arguments[i]){
            				sb.append(".").append(getObjectValue(arg));
            			}
                	}else{
                		sb.append(".").append(getObjectValue(arguments[i]));
                	}
            	//No parameter set as dot
            	}else {
            		sb.append(".");
            	}
            }
        }
        return sb.toString();
    }

	//尽量取得基础数据类型的值，使产生的KEY有意义
	private static String getObjectValue(Object arg){
		String value = "";
		if(arg instanceof Date){
			value =String.valueOf(((Date)arg).getTime());
		}else{
    		value = String.valueOf(arg);
    	}
		return value;

	}

}
