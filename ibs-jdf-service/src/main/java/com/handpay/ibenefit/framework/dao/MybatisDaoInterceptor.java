package com.handpay.ibenefit.framework.dao;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.persistence.Id;

import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.MappedStatement.Builder;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.mapping.ResultMap;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.scripting.defaults.DefaultParameterHandler;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.apache.log4j.Logger;

import com.handpay.ibenefit.framework.entity.BaseEntity;
import com.handpay.ibenefit.framework.util.PageSearch;

@Intercepts({
		@Signature(type = Executor.class, method = "update", args = { MappedStatement.class, Object.class }),
		@Signature(type = Executor.class, method = "query", args = { MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class }),
})
public class MybatisDaoInterceptor implements Interceptor {

	private static final Logger LOGGER = Logger.getLogger(MybatisDaoInterceptor.class);
	private static Method[] methods = BaseDao.class.getMethods();
	private static Map<String, Class<?>> classMap = new ConcurrentHashMap<String, Class<?>>();
	private final static Lock lock = new ReentrantLock();

	public Object intercept(Invocation invocation) throws Throwable {
		try {
			MappedStatement mappedStatement = (MappedStatement) invocation.getArgs()[0];
			Object parameter = (Object) invocation.getArgs()[1];
			String sqlId = mappedStatement.getId();
			String className = sqlId.substring(0, sqlId.lastIndexOf("."));
			Class<?> currentClass = getClass(className);
			String methodId = getMethodName(sqlId);
			Class entity = getActualArgumentType(currentClass);
			if (!isBaseMethod(currentClass, methodId)) {
				return invocation.proceed();
			}
			if(methodId.equals("save") && parameter instanceof BaseEntity){
				BaseEntity baseEntity = (BaseEntity)parameter;
				//如果objectId为空，则根据命令规则从对应的SEQUENCE获得新的序列作为objectId
				if(baseEntity.getObjectId()==null){
					Connection connection = mappedStatement.getConfiguration().getEnvironment().getDataSource().getConnection();
					String sql = "select " + BaseDaoUtils.getNextSeq(baseEntity.getTableName()) + " from dual";
					PreparedStatement getSequnceStatement = connection.prepareStatement(sql);
					ResultSet rs = getSequnceStatement.executeQuery();
					Long id = 0L;
					if (rs.next()) {
						id = rs.getLong(1);
					}
					rs.close();
					getSequnceStatement.close();
					connection.close();
					baseEntity.setObjectId(id);
				}
			}
			//BaseDao find方法的总记录数实现
			if (parameter instanceof PageSearch) {
				PageSearch pageSearch =(PageSearch) parameter;
				BoundSql boundSql = mappedStatement.getBoundSql(parameter);
				String originalSql = boundSql.getSql().trim();
				Object parameterObject = boundSql.getParameterObject();
				String countSql = getCountSql(originalSql);
				Connection connection = mappedStatement.getConfiguration().getEnvironment().getDataSource().getConnection();
				PreparedStatement countStmt = connection.prepareStatement(countSql);
				BoundSql countBS = copyFromBoundSql(mappedStatement, boundSql, countSql);
				DefaultParameterHandler parameterHandler = new DefaultParameterHandler(mappedStatement,parameterObject, countBS);
				parameterHandler.setParameters(countStmt);
				ResultSet rs = countStmt.executeQuery();
				int totalCount = 0;
				if (rs.next()) {
					totalCount = rs.getInt(1);
				}
				rs.close();
				countStmt.close();
				connection.close();
				pageSearch.setTotalCount(totalCount);
				//改写原始SQL为分页SQL
				BoundSql newBoundSql = copyFromBoundSql(mappedStatement,boundSql,getPageSql(pageSearch.getBegin(), pageSearch.getEnd(),originalSql));
				MappedStatement newMs = copyFromMappedStatement(mappedStatement, new BoundSqlSqlSource(newBoundSql));
				invocation.getArgs()[0]= newMs;  
			}
			setResultClass(mappedStatement, entity, methodId);
			return invocation.proceed();
		} catch (Exception e) {
			LOGGER.error("intercept",e);
			return invocation.proceed();
		}
	}

	private MappedStatement copyFromMappedStatement(MappedStatement ms, SqlSource newSqlSource) {
		Builder builder = new Builder(ms.getConfiguration(), ms.getId(), newSqlSource, ms.getSqlCommandType());

		builder.resource(ms.getResource());
		builder.fetchSize(ms.getFetchSize());
		builder.statementType(ms.getStatementType());
		builder.keyGenerator(ms.getKeyGenerator());
		StringBuilder keyProperty = new StringBuilder();
		for(String s : ms.getKeyProperties()){
			keyProperty.append(s).append(",");
		}
		builder.keyProperty(keyProperty.toString());
		builder.timeout(ms.getTimeout());
		builder.parameterMap(ms.getParameterMap());
		builder.resultMaps(ms.getResultMaps());
		builder.resultSetType(ms.getResultSetType());
		builder.cache(ms.getCache());
		builder.flushCacheRequired(ms.isFlushCacheRequired());
		builder.useCache(ms.isUseCache());
		return builder.build();
	}

	public class BoundSqlSqlSource implements SqlSource {
		BoundSql boundSql;
		public BoundSqlSqlSource(BoundSql boundSql) {
			this.boundSql = boundSql;
		}
		public BoundSql getBoundSql(Object parameterObject) {
			return boundSql;
		}
	}
	
	private String getCountSql(String sql) {
		return "SELECT COUNT(*) FROM (" + sql + ") aliasForPage";
	}
	
	public String getPageSql(int startRow, int endRow, String sql) {
		StringBuilder sqlBuilder = new StringBuilder(sql.length() + 120);
		sqlBuilder.append("select * from ( select tmp_page.*, rownum row_id from ( ");
		sqlBuilder.append(sql);
		sqlBuilder.append(" ) tmp_page where rownum <= " + endRow + " ) where row_id > " + startRow + "");
		return sqlBuilder.toString();
	}
	
	private BoundSql copyFromBoundSql(MappedStatement ms, BoundSql boundSql, String sql) {
		BoundSql newBoundSql = new BoundSql(ms.getConfiguration(), sql, boundSql.getParameterMappings(),
				boundSql.getParameterObject());
		for (ParameterMapping mapping : boundSql.getParameterMappings()) {
			String prop = mapping.getProperty();
			if (boundSql.hasAdditionalParameter(prop)) {
				newBoundSql.setAdditionalParameter(prop, boundSql.getAdditionalParameter(prop));
			}
		}
		return newBoundSql;
	}

	public Object plugin(Object target) {
		if (target instanceof Executor) {
			return Plugin.wrap(target, this);
		} else {
			return target;
		}
	}

	public void setProperties(Properties properties) {
	}

	private void setObject(final MappedStatement statement, final Object parameter, final String[] keys,
			String[] keyColumns) {
		if (statement.getSqlCommandType().equals(SqlCommandType.INSERT)) {
			keys[0] = BaseDaoUtils.id((Serializable) parameter);
			if (keyColumns == null) {
				keyColumns = new String[1];
			}
			keyColumns[0] = keys[0].replaceAll("([A-Z])", "_$1").toLowerCase();
		}
	}

	private void setTableName(Object parameter, Invocation invocation, Class entity, String methodId)
			throws InstantiationException, IllegalAccessException {
		if (methodId.equals("queryById") || methodId.equals("deleteById")) {
			Object param = entity.newInstance();
			Field[] fields = entity.getDeclaredFields();
			for (Field field : fields) {
				Id id = field.getAnnotation(Id.class);
				if (null != id) {
					field.setAccessible(true);
					if (field.getType().getSimpleName().equals(Long.class.getSimpleName())) {
						if (parameter instanceof Integer) {
							field.set(param, ((Integer) parameter).longValue());
						} else {
							field.set(param, parameter);
						}
					} else {
						if (parameter instanceof Long) {
							field.set(param, ((Long) parameter).intValue());
						} else {
							field.set(param, parameter);
						}
					}
					invocation.getArgs()[1] = param;
					break; // 新加
				}
			}
		}
	}
	
	public static Class getActualArgumentType(Class clazz){
		final Type[] types = clazz.getGenericInterfaces();
		final Type type;
		try{
			if(types.length>=1){
				Type thisType = types[0];
				if (thisType instanceof ParameterizedType) {
					type = ((ParameterizedType) thisType).getActualTypeArguments()[0];
				} else if (thisType instanceof Class) {
					type = ((ParameterizedType) ((Class) thisType).getGenericSuperclass()).getActualTypeArguments()[0];
				} else {
					throw new IllegalArgumentException("Problem handling type construction for " + clazz);
				}
				if (type instanceof Class) {
					return (Class) type;
				} else if (type instanceof ParameterizedType) {
					return (Class) ((ParameterizedType) type).getRawType();
				} else {
					throw new IllegalArgumentException("Problem determining the class of the generic for " + clazz);
				}
			}
		}catch(Exception e){
			LOGGER.warn(e.getMessage());
		}
		return null;
	}

	private void setResultClass(MappedStatement statement, Class entity, String methodId)
			throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException,
			ClassNotFoundException {
		if (methodId.endsWith("Count") || methodId.endsWith("SequenceValue") ) {
			ResultMap resultMap = statement.getResultMaps().get(0);
			Field rf = resultMap.getClass().getDeclaredField("type");
			rf.setAccessible(Boolean.TRUE);
			rf.set(resultMap, Long.class);
		}else{
			List<ResultMap> resultMaps = statement.getResultMaps();
			ResultMap resultMap = resultMaps.get(0);
			Field rf = resultMap.getClass().getDeclaredField("type");
			rf.setAccessible(Boolean.TRUE);
			rf.set(resultMap, entity);
		} 
	}

	private boolean isBaseMethod(Class<?> currentClass, String methodId) throws ClassNotFoundException {
		Boolean isBaseMethod = Boolean.FALSE;
		for (Method method : methods) {
			String methodName = method.getName();
			if (methodId.equals(methodName)) {
				isBaseMethod = Boolean.TRUE;
			}
		}
		if (!isBaseMethod) {
			return Boolean.FALSE;
		}
		isBaseMethod = Boolean.FALSE;
		for (Class<?> clazz : currentClass.getInterfaces()) {
			if (clazz.equals(BaseDao.class)) {
				isBaseMethod = Boolean.TRUE;
			}
		}
		return isBaseMethod;
	}

	private String getMethodName(String methodFullName) {
		String[] strs = methodFullName.split("\\.");
		String methodId = strs[strs.length - 1];
		return methodId;
	}

	private Class<?> getClass(String className) throws ClassNotFoundException {
		Class<?> currentClass = classMap.get(className);
		if (currentClass != null)
			return currentClass;

		currentClass = Class.forName(className);
		for (Class<?> clazz : currentClass.getInterfaces()) {
			if (clazz.equals(BaseDao.class)) {
				if (!classMap.containsKey(className)) {
					lock.lock();
					classMap.put(className, currentClass);
					lock.unlock();
				}
				break;
			}
		}

		return currentClass;
	}
}
