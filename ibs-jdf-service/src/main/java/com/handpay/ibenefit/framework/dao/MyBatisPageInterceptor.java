package com.handpay.ibenefit.framework.dao;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;

import org.apache.ibatis.executor.ErrorContext;
import org.apache.ibatis.executor.ExecutorException;
import org.apache.ibatis.executor.statement.BaseStatementHandler;
import org.apache.ibatis.executor.statement.RoutingStatementHandler;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.mapping.ParameterMode;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.property.PropertyTokenizer;
import org.apache.ibatis.scripting.xmltags.ForEachSqlNode;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.type.TypeHandler;
import org.apache.ibatis.type.TypeHandlerRegistry;

import com.handpay.ibenefit.framework.util.PageSearch;

@Intercepts({ @Signature(type = StatementHandler.class, method = "prepare", args = { Connection.class })
})

public class MyBatisPageInterceptor implements Interceptor {

	@Override
	public Object intercept(Invocation invocation) throws Throwable {
		if (!(invocation.getTarget() instanceof RoutingStatementHandler)){
			return invocation.proceed();
		}
		RoutingStatementHandler statementHandler = (RoutingStatementHandler) invocation.getTarget();
		BoundSql boundSql = statementHandler.getBoundSql();
		// 分析是否含有分页参数，如果没有则不是分页查询
		// 注意：在多参数的情况下，只处理第一个分页参数
		PageSearch page = null;
		Object paramObj = boundSql.getParameterObject();
		if (paramObj instanceof PageSearch) { // 只有一个参数的情况
			page = (PageSearch) paramObj;
		}
		if (page == null){
			return invocation.proceed();
		}
		String pageSql = boundSql.getSql();
		//如果在 MybatisDaoInterceptor中没有做分页处理，就查找总记录数，并设置Page的相关参数
		if(page.getTotalCount() == null){
			int total = this.getTotal(invocation);
			page.setTotalCount(total);
			// 生成分页SQL
			// 强制修改最终要执行的SQL
			pageSql = generatePageSql(boundSql.getSql(), page);
			setFieldValue(boundSql, "sql", pageSql);
		}
		return invocation.proceed();
	}

	/**
	 * 获取记录总数
	 */
	@SuppressWarnings("unchecked")
	private int getTotal(Invocation invocation) throws Exception {
		RoutingStatementHandler statementHandler = (RoutingStatementHandler) invocation.getTarget();
		BoundSql boundSql = statementHandler.getBoundSql();
		 /*
		 * 为了设置查找总数SQL的参数，必须借助MappedStatement、Configuration等这些类，
		 * 但statementHandler并没有开放相应的API，所以只好用反射来强行获取。
		 */
		BaseStatementHandler delegate = (BaseStatementHandler) getFieldValue(statementHandler, "delegate");
		MappedStatement mappedStatement = (MappedStatement) getFieldValue(delegate, "mappedStatement");
		int total = 0;
		String sql = boundSql.getSql();
		String countSql = "select count(1) from (" + sql + ") "; // 记录统计
		try {
			Connection conn = (Connection) invocation.getArgs()[0];
			PreparedStatement ps = conn.prepareStatement(countSql);
			setParameters(ps, mappedStatement, boundSql,boundSql.getParameterObject());
			ResultSet rs = ps.executeQuery();
			rs.next();
			total = rs.getInt(1);
			rs.close();
			ps.close();
		} catch (Exception e) {
			throw new RuntimeException("分页查询无法获取总记录数", e);
		}
		return total;
	}

	private void setParameters(PreparedStatement ps,
			MappedStatement mappedStatement, BoundSql boundSql,
			Object parameterObject) throws SQLException {
		ErrorContext.instance().activity("setting parameters").object(mappedStatement.getParameterMap().getId());
		List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
		if (parameterMappings != null) {
			Configuration configuration = mappedStatement.getConfiguration();
			TypeHandlerRegistry typeHandlerRegistry = configuration.getTypeHandlerRegistry();
			MetaObject metaObject = parameterObject == null ? null : configuration.newMetaObject(parameterObject);
			for (int i = 0; i < parameterMappings.size(); i++) {
				ParameterMapping parameterMapping = parameterMappings.get(i);
				if (parameterMapping.getMode() != ParameterMode.OUT) {
					Object value;
					String propertyName = parameterMapping.getProperty();
					PropertyTokenizer prop = new PropertyTokenizer(propertyName);
					if (parameterObject == null) {
						value = null;
					} else if (typeHandlerRegistry.hasTypeHandler(parameterObject.getClass())) {
						value = parameterObject;
					} else if (boundSql.hasAdditionalParameter(propertyName)) {
						value = boundSql.getAdditionalParameter(propertyName);
					} else if (propertyName .startsWith(ForEachSqlNode.ITEM_PREFIX)
							&& boundSql.hasAdditionalParameter(prop.getName())) {
						value = boundSql.getAdditionalParameter(prop.getName());
						if (value != null) {
							value = configuration.newMetaObject(value) .getValue(propertyName.substring(prop.getName().length()));
						}
					} else {
						value = metaObject == null ? null : metaObject.getValue(propertyName);
					}
					TypeHandler typeHandler = parameterMapping.getTypeHandler();
					if (typeHandler == null) {
						throw new ExecutorException(
								"There was no TypeHandler found for parameter " + propertyName + " of statement " + mappedStatement.getId());
					}
					typeHandler.setParameter(ps, i + 1, value, parameterMapping.getJdbcType());
				}
			}
		}
	}

	/**
	 * 生成分页SQL
	 */
	private String generatePageSql(String sql, PageSearch page) {
		StringBuilder pageSql = new StringBuilder();
		pageSql.append("select * from (select t.*, ROWNUM num from (")
				.append(sql).append(") t where ROWNUM <= ")
				.append(page.getBegin() + page.getPageSize())
				.append(") where num > ").append(page.getBegin());
		return pageSql.toString();
	}

	/**
	 * 用反射取对象的属性值
	 */
	private Object getFieldValue(Object obj, String fieldName) throws Exception {
		for (Class<?> superClass = obj.getClass(); superClass != Object.class; superClass = superClass
				.getSuperclass()) {
			try {
				Field field = superClass.getDeclaredField(fieldName);
				field.setAccessible(true);
				return field.get(obj);
			} catch (Exception e) {
			}
		}
		return null;
	}

	/**
	 * 用反射设置对象的属性值
	 */
	private void setFieldValue(Object obj, String fieldName, Object fieldValue)
			throws Exception {
		Field field = obj.getClass().getDeclaredField(fieldName);
		field.setAccessible(true);
		field.set(obj, fieldValue);
	}

	@Override
	public Object plugin(Object target) {
		return Plugin.wrap(target, this);
	}

	@Override
	public void setProperties(Properties props) {

	}
}