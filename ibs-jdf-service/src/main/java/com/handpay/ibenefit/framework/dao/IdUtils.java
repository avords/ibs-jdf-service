package com.handpay.ibenefit.framework.dao;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import javax.persistence.Entity;

/**
 * Utility for generating ID
 */
public final class IdUtils {
	private IdUtils (){
		
	}
	private static final String UN_CACHE_CUSTOMISE_ID_PREFIX = "U_C_C_ID_";
	
	/**
	 * Cached object ID generator
	 */
	private static final Map<String, TableCachedGenerator> OBJECT_ID_GENERATORS = new Hashtable<String, TableCachedGenerator>();

	/**
	 * Each table has a key(Table name + ".objectId") Cache size is 50
	 * 
	 * @param entity
	 * @return
	 */
	public static Long getObjectId(Class entity, Connection connection) throws Exception{
		Entity entityClass = (Entity) entity.getAnnotation(Entity.class);
		String keyValue = null;
		// Nuknown object,may be will not occur
		if (entityClass == null) {
			keyValue = "UnknownObject_objectId";
		} else {
			keyValue = entityClass.name() + ".objectId";
		}
		keyValue = keyValue.toLowerCase();
		TableCachedGenerator objectIdGenerator = getObjectIdGenerator(keyValue);
		return (Long) objectIdGenerator.generate(connection);
	}

	/**
	 * Generate uncached customize ID
	 * 
	 * @param sessionFactory
	 * @param customizeIdKey
	 *            the unique ID name,such as "OrderNo"
	 * @return
	 */
	public static Long getUnCacheCustomizeId(String customizeIdKey, Connection connection) throws Exception {
		String keyValue = UN_CACHE_CUSTOMISE_ID_PREFIX + customizeIdKey;
		keyValue = keyValue.toLowerCase();
		TableCachedGenerator objectIdGenerator = getCustomizeIdGenerator(keyValue);
		return (Long) objectIdGenerator.generate(connection);

	}

	private static synchronized TableCachedGenerator getCustomizeIdGenerator(String keyValue) {
		TableCachedGenerator objectIdGenerator = OBJECT_ID_GENERATORS.get(keyValue);
		if (objectIdGenerator == null) {
			objectIdGenerator = new TableCachedGenerator(1,keyValue);
			OBJECT_ID_GENERATORS.put(keyValue, objectIdGenerator);
		}
		return objectIdGenerator;
	}

	private static synchronized TableCachedGenerator getObjectIdGenerator(String keyValue) {
		TableCachedGenerator objectIdGenerator = OBJECT_ID_GENERATORS.get(keyValue);
		if (objectIdGenerator == null) {
			objectIdGenerator = new TableCachedGenerator(keyValue);
			OBJECT_ID_GENERATORS.put(keyValue, objectIdGenerator);
		}
		return objectIdGenerator;
	}
}
