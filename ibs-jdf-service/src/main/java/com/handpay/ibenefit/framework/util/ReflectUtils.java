package com.handpay.ibenefit.framework.util;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;

import javax.validation.constraints.NotNull;

import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.beanutils.converters.DateConverter;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.util.Assert;

public final class ReflectUtils {
	private static final Logger LOGGER = Logger.getLogger(ReflectUtils.class);

	private static final String PROPERTY_SEPERATOR = ".";

	private ReflectUtils(){
	}

	public static Method findGetterMethod(Object object, String propertyPath) {
		if (object == null) {
			LOGGER.debug("Object was null.");
			return null;
		}
		if (propertyPath == null) {
			LOGGER.debug("propertyPath was null.");
			return null;
		}
		String property = getPropertyFromPropertyPath(propertyPath);
		String getterMethodName = convertPropertyNameToGetMethodName(property);
		try {
			return object.getClass().getMethod(getterMethodName);
		} catch (NoSuchMethodException e) {
			LOGGER.error(e);
			return null;
		}
	}


	public static Boolean required(Object object, String propertyPath) {
		Method getMethod = findGetterMethod(object, propertyPath);
		if (getMethod == null) {
			return Boolean.FALSE;
		} else {
			return getMethod.isAnnotationPresent(NotNull.class);
		}
	}

	public static Boolean isDate(Object object, String propertyPath) {
		return java.util.Date.class.equals(getReturnType(object, propertyPath));
	}

	public static Class getReturnType(Object object, String propertyPath) {
		Method getMethod = findGetterMethod(object, propertyPath);
		if (getMethod == null) {
			return null;
		} else {
			return getMethod.getReturnType();
		}
	}

	public static Object getReferencedObject(Object object, String propertyPath) {
		Method getMethod = findGetterMethod(object, propertyPath);
		if (getMethod == null) {
			return null;
		}
		try {
			return getMethod.invoke(object);
		} catch (Exception e) {
			LOGGER.debug("Unable to get object via reflection.");
			return null;
		}
	}

	public static String convertPropertyNameToGetMethodName(String property) {
		if (property == null) {
			throw new IllegalArgumentException("Property name was null.");
		}
		StringBuilder builder = new StringBuilder(property.length() + 3);
		String firstLetter = property.substring(0, 1);
		String wordRemainder = property.substring(1);
		firstLetter = firstLetter.toUpperCase();
		builder.append("get").append(firstLetter).append(wordRemainder);
		return builder.toString();
	}

	public static String getObjectPath(String propertyPath) {
		// Path is "customer.contact.fax.number", need to return
		// "customer.contact.fax"
		LOGGER.debug("propertyPath = " + propertyPath);
		int lastSeperatorPosition = propertyPath.lastIndexOf(PROPERTY_SEPERATOR);
		String objectString = propertyPath.substring(0, lastSeperatorPosition);
		LOGGER.debug("objectString = " + objectString);
		return objectString;
	}

	public static String getPropertyFromPropertyPath(String propertyPath) {
		// Path is "customer.contact.fax.number", need to return "number"
		LOGGER.debug("propertyPath = " + propertyPath);
		int lastSeperatorPosition = propertyPath.lastIndexOf(PROPERTY_SEPERATOR);
		String propertyString = propertyPath.substring(lastSeperatorPosition + 1);
		LOGGER.debug("propertyString = " + propertyString);
		return propertyString;
	}

	public static String convertDatabaseNameToJavaName(String val) {
		if (val == null) {
			return null;
		}
		String value = val.toLowerCase();
		StringBuilder buf = null;
		StringTokenizer st = new StringTokenizer(value, "_");
		if (st.hasMoreTokens()) {
			buf = new StringBuilder(st.nextToken());
			while (st.hasMoreTokens()) {
				String fragment = st.nextToken();
				buf.append(initCap(fragment));
			}
		}
		if (buf == null) {
			return null;
		} else {
			return buf.toString();
		}
	}

	public static String convertGet(String value) {
		StringBuilder buf = new StringBuilder("get");
		buf.append(initCap(convertDatabaseNameToJavaName(value)));
		return buf.toString();
	}

	public static String convertSet(String value) {
		StringBuilder buf = new StringBuilder("set");
		buf.append(initCap(convertDatabaseNameToJavaName(value)));
		return buf.toString();
	}

	public static String initCap(String value) {
		if (value == null) {
			return null;
		}
		String initCap = null;
		String suffix = null;
		try {
			initCap = value.substring(0, 1).toUpperCase();
			suffix = value.substring(1, value.length());
		} catch (IndexOutOfBoundsException e) {
			LOGGER.error(e);
		}
		return initCap + suffix;
	}

	static {
		DateConverter dc = new DateConverter();
		dc.setUseLocaleFormat(true);
		dc.setPatterns(new String[] { "yyyy-MM-dd", "yyyy-MM-dd HH:mm:ss" });
		ConvertUtils.register(dc, Date.class);
	}

	public static Object invokeGetterMethod(Object target, String propertyName) {
		String getterMethodName = "get" + StringUtils.capitalize(propertyName);
		return invokeMethod(target, getterMethodName, new Class[] {}, new Object[] {});
	}

	public static void invokeSetterMethod(Object target, String propertyName, Object value) {
		invokeSetterMethod(target, propertyName, value, null);
	}

	public static void invokeSetterMethod(Object target, String propertyName, Object value, Class<?> propertyType) {
		Class<?> type = propertyType != null ? propertyType : value.getClass();
		String setterMethodName = "set" + StringUtils.capitalize(propertyName);
		invokeMethod(target, setterMethodName, new Class[] { type }, new Object[] { value });
	}

	public static Object getFieldValue(final Object object, final String fieldName) {
		Field field = getDeclaredField(object, fieldName);
		if (field == null) {
			throw new IllegalArgumentException("Could not find field [" + fieldName + "] on target [" + object + "]");
		}
		makeAccessible(field);
		Object result = null;
		try {
			result = field.get(object);
		} catch (IllegalAccessException e) {
			LOGGER.error("getFieldValue", e);
		}
		return result;
	}

	public static void setFieldValue(final Object object, final String fieldName, final Object value) {
		Field field = getDeclaredField(object, fieldName);
		if (field == null) {
			throw new IllegalArgumentException("Could not find field [" + fieldName + "] on target [" + object + "]");
		}
		makeAccessible(field);
		try {
			field.set(object, value);
		} catch (IllegalAccessException e) {
			LOGGER.error("setFieldValue", e);
		}
	}

	public static Object invokeMethod(final Object object, final String methodName, final Class<?>[] parameterTypes,
	        final Object[] parameters) {
		Method method = getDeclaredMethod(object, methodName, parameterTypes);
		if (method == null) {
			throw new IllegalArgumentException("Could not find method [" + methodName + "] on target [" + object + "]");
		}
		method.setAccessible(true);
		try {
			return method.invoke(object, parameters);
		} catch (Exception e) {
			throw convertReflectionExceptionToUnchecked(e);
		}
	}

	protected static Field getDeclaredField(final Object object, final String fieldName) {
		Assert.notNull(object);
		Assert.hasText(fieldName);
		for (Class<?> superClass = object.getClass(); superClass != Object.class; superClass = superClass
		        .getSuperclass()) {
			try {
				return superClass.getDeclaredField(fieldName);
			} catch (NoSuchFieldException e) {
				// nothing to do
			}
		}
		return null;
	}

	protected static void makeAccessible(final Field field) {
		if (!Modifier.isPublic(field.getModifiers()) || !Modifier.isPublic(field.getDeclaringClass().getModifiers())) {
			field.setAccessible(true);
		}
	}

	protected static Method getDeclaredMethod(Object object, String methodName, Class<?>[] parameterTypes) {
		Assert.notNull(object);
		for (Class<?> superClass = object.getClass(); superClass != Object.class; superClass = superClass
		        .getSuperclass()) {
			try {
				return superClass.getDeclaredMethod(methodName, parameterTypes);
			} catch (NoSuchMethodException e) {
				// nothing to do
			}
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public static <T> Class<T> getSuperClassGenricType(final Class clazz) {
		return getSuperClassGenricType(clazz, 0);
	}

	@SuppressWarnings("unchecked")
	public static Class getSuperClassGenricType(final Class clazz, final int index) {
		Type genType = clazz.getGenericSuperclass();
		if (!(genType instanceof ParameterizedType)) {
			LOGGER.warn(clazz.getSimpleName() + "'s superclass not ParameterizedType");
			return Object.class;
		}
		Type[] params = ((ParameterizedType) genType).getActualTypeArguments();
		if (index >= params.length || index < 0) {
			LOGGER.warn("Index: " + index + ", Size of " + clazz.getSimpleName() + "'s Parameterized Type: "
			        + params.length);
			return Object.class;
		}
		if (!(params[index] instanceof Class)) {
			LOGGER.warn(clazz.getSimpleName() + " not set the actual class on superclass generic parameter");
			return Object.class;
		}
		return (Class) params[index];
	}

	@SuppressWarnings("unchecked")
	public static List convertElementPropertyToList(final Collection collection, final String propertyName) {
		List list = new ArrayList();
		try {
			for (Object obj : collection) {
				list.add(PropertyUtils.getProperty(obj, propertyName));
			}
		} catch (Exception e) {
			throw convertReflectionExceptionToUnchecked(e);
		}
		return list;
	}

	@SuppressWarnings("unchecked")
	public static String convertElementPropertyToString(final Collection collection, final String propertyName,
	        final String separator) {
		List list = convertElementPropertyToList(collection, propertyName);
		return StringUtils.join(list, separator);
	}

	public static Object convertStringToObject(String value, Class<?> toType) {
		try {
			return ConvertUtils.convert(value, toType);
		} catch (Exception e) {
			throw convertReflectionExceptionToUnchecked(e);
		}
	}

	public static RuntimeException convertReflectionExceptionToUnchecked(Exception e) {
		if (e instanceof IllegalAccessException || e instanceof IllegalArgumentException
		        || e instanceof NoSuchMethodException) {
			return new IllegalArgumentException("Reflection Exception.", e);
		} else if (e instanceof InvocationTargetException) {
			return new RuntimeException("Reflection Exception.", ((InvocationTargetException) e).getTargetException());
		} else if (e instanceof RuntimeException) {
			return (RuntimeException) e;
		}
		return new RuntimeException("Unexpected Checked Exception.", e);
	}

}
