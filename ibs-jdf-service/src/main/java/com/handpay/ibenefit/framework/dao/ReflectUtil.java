package com.handpay.ibenefit.framework.dao;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

public class ReflectUtil {

	public static Object invokeGet(String fieldName, Object t) throws RuntimeException {
		Class cl = t.getClass();
		String methodName = "get" + captureName(fieldName);
		Object result= null;
		try{
			Method method = getMethod(methodName,cl);
			if(method != null){
			    result = method.invoke(t, null);
			}
		}catch(Exception e){

		}
		return result;
	}

	private static Method getMethod(String methodName, Class clazz) {
		if (null == clazz || Object.class == clazz) {
			return null;
		}
		Method method = null;
			try {
				method = clazz.getDeclaredMethod(methodName, null);
			} catch (Exception e) {
				return getMethod(methodName,clazz.getSuperclass());
			}

		return method;
	}

	public static void invokeSet(String fieldName, Object t, Object value) throws RuntimeException {
		if(value!=null){
			Class cl = t.getClass();
			String methodName = "set" + captureName(fieldName);
			try{
				Method method = getMethod(methodName,cl);
				if(method != null){
				    method.invoke(t, value);
				}
			}catch(Exception e){
				throw new RuntimeException(e);
			}
		}
	}

	public static String captureName(String name) {
		char[] cs = name.toCharArray();
		cs[0] -= 32;
		return String.valueOf(cs);
	}

	public static void getFiledsByAnnotation(Class clazz,Class annotationClass,List<String> fields){
		Class cSuper = clazz.getSuperclass();
		if (null != cSuper && Object.class != cSuper) {
			Field[] fieldArr = clazz.getDeclaredFields();
			for(Field field : fieldArr){
				Annotation a = field.getAnnotation(annotationClass);
				if(a!=null){
					fields.add(field.getName());
				}
			}
			getFiledsByAnnotation(cSuper,annotationClass,fields);
		}
	}

}
