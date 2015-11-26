package com.handpay.ibenefit.framework.dao;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;

import org.apache.log4j.Logger;

import com.handpay.ibenefit.framework.validate.FieldProperty;
import com.handpay.ibenefit.framework.validate.ValidateContainer;

public final class BaseDaoUtils {
	
	private final static Map<String, Class<?>> FIELD_TYPE_MAP = new HashMap<String, Class<?>>();

	public static final Integer DEAFULT_PAGE_SIZE = 30; // 每页默认记录数

	public static final Integer DEAFULT_PAGE_NAVIGATEPAGES = 5; // 每页默认记录数
	
	public static final String SPACE_TABLE_NAME="SPACE_TABLE_NAME";

	// ORACLE 序列前缀
	public static final String IBS_TABLE_SEQ_PREFIX = "HP_";

	// ORACLE序列后缀
	public static final String IBS_TABLE_SEQ_SUFFIX = "_SQ";

	public static final String IBS_TABLE_SEQ_NEXTVAL = "Nextval";
	
	private static final Logger LOGGER = Logger.getLogger(BaseDaoUtils.class);

	public enum MYBATIS_SPECIAL_STRING {
		ORDER_BY, LIMIT, COLUMNS, TABLES, WHERE, LIKE, START_ROW, END_ROW;
		public static List<String> list() {
			List<String> result = new ArrayList<String>();
			for (MYBATIS_SPECIAL_STRING entry : MYBATIS_SPECIAL_STRING.values()) {
				result.add(entry.name());
			}
			return result;
		}
	}


	/**
	 * 用于存放POJO的列信息
	 */
	private static final Map<String, Map<String, String>> COLUMN_MAP = new HashMap<String, Map<String, String>>();
	
	static{
		FIELD_TYPE_MAP.put("Double", Double.class);
		FIELD_TYPE_MAP.put("Short", Short.class);
		FIELD_TYPE_MAP.put("Long", Long.class);
		FIELD_TYPE_MAP.put("Float", Float.class);
		FIELD_TYPE_MAP.put("Integer", Integer.class);
		FIELD_TYPE_MAP.put("Byte", Byte.class);
		FIELD_TYPE_MAP.put("String", String.class);
		FIELD_TYPE_MAP.put("Character", Character.class);
		FIELD_TYPE_MAP.put("Date", Date.class);
		FIELD_TYPE_MAP.put("Boolean", Boolean.class);
		FIELD_TYPE_MAP.put("Date", java.util.Date.class);
		
		Map<String,List<FieldProperty>> map =  ValidateContainer.getAllFiledProperty();
		for (Map.Entry<String,List<FieldProperty>> entry : map.entrySet()) {
			String className = entry.getKey();
			Map<String, String> fieldMap = new HashMap<String, String>();
			for (FieldProperty fieldProperty : entry.getValue()) {
				Field field = fieldProperty.getField();
				boolean notColumn = field.isAnnotationPresent(Transient.class);
				boolean isStatic = Modifier.isStatic(field.getModifiers());
				boolean isTransient = Modifier.isTransient(field.getModifiers());
				boolean isFinal = Modifier.isFinal(field.getModifiers());
				boolean isPrimitive = field.getType().isPrimitive() || FIELD_TYPE_MAP.containsValue(field.getType());
				if (notColumn || isStatic || isFinal || isTransient || !isPrimitive) {
					continue;
				}
				fieldMap.put(fieldProperty.getFieldName(), fieldProperty.getColumnName());
			}
			COLUMN_MAP.put(className, fieldMap);
		}
	}
	
	/**
	 * 获取POJO对应的表名 需要POJO中的属性定义@Table(name)
	 * 
	 * @return
	 */
	
	public static String tableName(Object obj) {
		return tableName(obj.getClass());
	}
	
	public static String tableName(Class clazz) {
		Entity entity = (Entity)clazz.getAnnotation(Entity.class);
		return entity.name();
	}
	
	/**
	 * 获取POJO中的主键字段名 需要定义@Id
	 * 
	 * @return
	 */
	public static String id(Object obj) {
		for (FieldProperty fieldProperty : ValidateContainer.getAllFieldsOfTheDomain(obj.getClass().getName())) {
			Field field = fieldProperty.getField();
			if (null != field.getAnnotation(Id.class))
				return field.getName();
		}
		if (obj.getClass().equals(Long.class) || obj.getClass().equals(long.class) || obj.getClass().equals(int.class)
				|| obj.getClass().equals(Integer.class)) {
			return "id";
		}
		throw new RuntimeException("undefine " + obj.getClass().getName() + " @Id");
	}

	private static boolean isNull(Serializable obj, String fieldname) {
		Method  getMethod = getGetMehtodByField(obj.getClass(), fieldname);
		if(getMethod!=null){
			try {  
	            Object value = getMethod.invoke(obj, new Object[] {});  
	            if (value == null) {  
	                return true;  
	            }else{
	            	return false;
	            }
	        } catch (Exception e) {  
	        	LOGGER.error("isNull",e);
	        }  
		}
		return true;  
	}
	
	private static Method getGetMehtodByField(Class clazz, String field) {  
        Method method = null;  
        String methodName = "get" + field.substring(0, 1).toUpperCase()+ field.substring(1);  
        try {  
            method = clazz.getMethod(methodName, new Class[] {});  
        } catch (Exception e) {  
        	LOGGER.error("getGetMehtodByField",e);
        }  
        return method;  
    }  
	
	/**
	 * Where条件信息
	 * 
	 * @author HUYAO
	 * 
	 */
	public class WhereColumn {
		public String name;
		public boolean isString;
		public WhereColumn(String name, boolean isString) {
			this.name = name;
			this.isString = isString;
		}
	}

	/**
	 * 用于获取Insert的字段累加
	 * 
	 * @return
	 */
	public static String returnInsertColumnsName(Serializable obj) {
		boolean isPrimitive = false;
		StringBuilder sb = new StringBuilder();
		Map<String, String> fieldMap = COLUMN_MAP.get(obj.getClass().getName());
		Iterator<String> iterator = fieldMap.keySet().iterator();
		int i = 0;
		while (iterator.hasNext()) {
			String fieldname = iterator.next();
			try {
				Field field = ValidateContainer.getFieldProperty(obj.getClass().getName(), fieldname).getField();
				isPrimitive = field.getAnnotation(Id.class) != null;
			} catch (Exception e) {
				LOGGER.error("returnInsertColumnsName",e);
			}

			if (isNull(obj, fieldname) && !fieldname.equals("createTime") & !isPrimitive)
				continue;
			if (i++ != 0) {
				sb.append(',');
			}
			sb.append(fieldMap.get(fieldname));
		}
		return sb.toString();
	}

	/**
	 * 用于获取Insert的字段映射累加
	 * @return
	 */
	public static String returnInsertColumnsDefine(Serializable obj) {
		StringBuilder sb = new StringBuilder();
		boolean isPrimitive = false;
		Map<String, String> fieldMap = COLUMN_MAP.get(obj.getClass().getName());
		Iterator<String> iterator = fieldMap.keySet().iterator();
		int i = 0;
		while (iterator.hasNext()) {
			String fieldname = iterator.next();
			try {
				Field field = ValidateContainer.getFieldProperty(obj.getClass().getName(), fieldname).getField();
				isPrimitive = field.getAnnotation(Id.class) != null;
			} catch (Exception e) {
				LOGGER.error("returnInsertColumnsDefine",e);
			}
			boolean isTime = fieldname.equals("createTime") ;
			if ((!isTime) && isNull(obj, fieldname) && !isPrimitive)
				continue;

			if (i++ != 0) {
				sb.append(',');
			}

			if (isTime) {
				sb.append("sysdate");
			} else {
				sb.append("#{").append(fieldname).append('}');
			}
		}
		return sb.toString();
	}
	/**
	 * 用于获取Update Set的字段累加
	 * 
	 * @return
	 */
	public static String returnUpdateSetFull(Serializable obj) {
		StringBuilder sb = new StringBuilder();
		Map<String, String> fieldMap = COLUMN_MAP.get(obj.getClass().getName());
		int i = 0;
		for (Map.Entry<String, String> column : fieldMap.entrySet()) {
			if (i++ != 0){
				sb.append(',');
			}
			sb.append(column.getValue()).append("=#{").append(column.getKey()).append('}');
		}
		return sb.toString();
	}
	
	/**
	 * 用于获取Update Set的字段累加,排除新增字段
	 * @return
	 */
	public static String returnUpdateSetExcludeCreateInfo(Serializable obj) {
		StringBuilder sb = new StringBuilder();
		Map<String, String> fieldMap = COLUMN_MAP.get(obj.getClass().getName());
		int i = 0;
		for (Map.Entry<String, String> column : fieldMap.entrySet()) {
			boolean isCreateBy = column.getKey().equalsIgnoreCase("createdBy");
			boolean isCreateOn = column.getKey().equalsIgnoreCase("createdOn");
			if(isCreateBy || isCreateOn){
				continue;
			}
			if (i++ != 0){
				sb.append(',');
			}
			sb.append(column.getValue()).append("=#{").append(column.getKey()).append('}');
		}
		return sb.toString();
	}

	/**
	 * 用于获取Update Set的字段累加
	 * 
	 * @return
	 */
	public static String returnUpdateSet(Serializable obj) {
		StringBuilder sb = new StringBuilder();

		Map<String, String> fieldMap = COLUMN_MAP.get(obj.getClass().getName());
		int i = 0;
		for (Map.Entry<String, String> column : fieldMap.entrySet()) {
			String key = column.getKey();
			boolean isUpdateTime = key.equalsIgnoreCase("updateTime");
			if (isNull(obj, key) && !isUpdateTime)
				continue;

			if (i++ != 0)
				sb.append(',');
			if (isUpdateTime) {
				sb.append("update_time=sysdate");
			} else {
				sb.append(column.getValue()).append("=#{").append(column.getKey()).append('}');
			}
		}
		return sb.toString();
	}

	/**
	 * 用于获取select、delete的条件组装
	 * 
	 * @return
	 */
	public static String whereColumnNotNull(Serializable obj) {
		StringBuilder sb = new StringBuilder();
		Map<String, String> fieldMap = COLUMN_MAP.get(obj.getClass().getName());
		int i = 0;
		for (Map.Entry<String, String> column : fieldMap.entrySet()) {
			if (isNull(obj, column.getKey()))
				continue;
			if (i++ != 0)
				sb.append(" AND ");
			sb.append(column.getValue()).append("=#{").append(column.getKey() + "}");
		}
		//附止条件为空，误删除记录
		if(sb.length() == 0){
			return "1!=1";
		}
		return sb.toString();
	}

	/**
	 * 用于获取select、delete的条件组装
	 * 
	 * @return
	 */
	public static String whereColumn(Map<String, Object> param) {
		StringBuilder sb = new StringBuilder();
		int i = 0;
		for (Map.Entry<String, Object> column : param.entrySet()) {
			if (i++ != 0)
				sb.append(" AND ");
			if (!MYBATIS_SPECIAL_STRING.list().contains(column.getKey().toUpperCase())) {
				sb.append(column.getKey().replaceAll("([A-Z])", "_$1").toLowerCase()).append("=#{")
						.append(column.getKey() + "}");
			} else if (MYBATIS_SPECIAL_STRING.LIKE.name().equalsIgnoreCase(column.getKey())) {
				sb.append(column.getValue());
			} else if (MYBATIS_SPECIAL_STRING.COLUMNS.name().equalsIgnoreCase(column.getKey())) {
				sb.append(column.getValue());
			}
		}
		return sb.toString();
	}
	
	/**
	 * 用于获取select、delete的条件组装
	 * 
	 * @return
	 */
	public static String whereColumnNotEmpty(Map<String, Object> param) {
		StringBuilder sb = new StringBuilder();
		int i = 0;
		for (Map.Entry<String, Object> column : param.entrySet()) {
			if (column.getValue() == null)
				continue;
			if (i++ != 0)
				sb.append(" AND ");
			if (!MYBATIS_SPECIAL_STRING.list().contains(column.getKey().toUpperCase())) {
				sb.append(column.getKey().replaceAll("([A-Z])", "_$1").toLowerCase()).append("=#{")
						.append(column.getKey() + "}");
			} else if (MYBATIS_SPECIAL_STRING.LIKE.name().equalsIgnoreCase(column.getKey())) {
				sb.append(column.getValue());
			} else if (MYBATIS_SPECIAL_STRING.COLUMNS.name().equalsIgnoreCase(column.getKey())) {
				sb.append(column.getValue());
			}
		}
		return sb.toString();
	}

	public static String queryColumn(Object obj) {
		return queryColumn(obj.getClass());
	}
	
	public static String queryColumn(Class clazz) {
		StringBuilder sb = new StringBuilder();
		Map<String, String> fieldMap = COLUMN_MAP.get(clazz.getName());
		if(fieldMap!=null){
			int i = 0;
			for (Map.Entry<String, String> column : fieldMap.entrySet()) {
				if (i++ != 0)
					sb.append(',');
				sb.append(column.getValue()).append(" as ").append(column.getKey());
			}
			return sb.toString();
		}
		return "*";
		
	}

	/*
	 * public Integer getId(Serializable obj) { return 0; }
	 */

	/**
	 * 打印类字段信息
	 */
	public static String objString(Object obj) {
		Field[] fields = obj.getClass().getFields();
		StringBuilder sb = new StringBuilder();
		sb.append('[');
		for (Field f : fields) {
			if (Modifier.isStatic(f.getModifiers()) || Modifier.isFinal(f.getModifiers()))
				continue;
			Object value = null;
			try {
				f.setAccessible(true);
				value = f.get(obj);
			} catch (Exception e) {
				LOGGER.error("objString",e);
			}
			if (value != null)
				sb.append(f.getName()).append('=').append(value).append(',');
		}
		sb.append(']');

		return sb.toString();
	}

	public static String getNextSeq(String tableName) {
		tableName = tableName.toUpperCase();
		return IBS_TABLE_SEQ_PREFIX + tableName + IBS_TABLE_SEQ_SUFFIX + "." + IBS_TABLE_SEQ_NEXTVAL;
	}

}
