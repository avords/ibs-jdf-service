package com.handpay.ibenefit.framework.dao;

import static org.apache.ibatis.jdbc.SqlBuilder.BEGIN;
import static org.apache.ibatis.jdbc.SqlBuilder.DELETE_FROM;
import static org.apache.ibatis.jdbc.SqlBuilder.FROM;
import static org.apache.ibatis.jdbc.SqlBuilder.INSERT_INTO;
import static org.apache.ibatis.jdbc.SqlBuilder.ORDER_BY;
import static org.apache.ibatis.jdbc.SqlBuilder.SELECT;
import static org.apache.ibatis.jdbc.SqlBuilder.SET;
import static org.apache.ibatis.jdbc.SqlBuilder.SQL;
import static org.apache.ibatis.jdbc.SqlBuilder.UPDATE;
import static org.apache.ibatis.jdbc.SqlBuilder.VALUES;
import static org.apache.ibatis.jdbc.SqlBuilder.WHERE;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Map;

import com.handpay.ibenefit.framework.util.PageSearch;
import com.handpay.ibenefit.framework.validate.FieldProperty;


public class MyBatisTemplate<T extends Serializable> {

	public String save(T obj) {
		BEGIN();
		INSERT_INTO(BaseDaoUtils.tableName(obj));
		VALUES(BaseDaoUtils.returnInsertColumnsName(obj), BaseDaoUtils.returnInsertColumnsDefine(obj));
		String sql = SQL();
		return sql;
	}

	public String updateById(T obj) {
		String idName = BaseDaoUtils.id(obj);
		BEGIN();
		UPDATE(BaseDaoUtils.tableName(obj));
		SET(BaseDaoUtils.returnUpdateSetFull(obj));
		whereId(idName);
		return SQL();
	}
	
	public String updateObjectExcludeCreateInfo(T obj){
		String idName = BaseDaoUtils.id(obj);
		BEGIN();
		UPDATE(BaseDaoUtils.tableName(obj));
		SET(BaseDaoUtils.returnUpdateSetExcludeCreateInfo(obj));
		whereId(idName);
		return SQL();
	}

	public String updateNotNullById(T obj) {
		String idName = BaseDaoUtils.id(obj);
		BEGIN();
		UPDATE(BaseDaoUtils.tableName(obj));
		SET(BaseDaoUtils.returnUpdateSet(obj));
		whereId(idName);
		return SQL();
	}

	public String deleteById(T obj) {
		String idName = BaseDaoUtils.id(obj);
		BEGIN();
		DELETE_FROM(BaseDaoUtils.tableName(obj));
		whereId(idName);
		return SQL();
	}
	
	public String deleteForeverEntity(T obj) {
		String idName = BaseDaoUtils.id(obj);
		BEGIN();
		UPDATE(BaseDaoUtils.tableName(obj));
		SET("UPDATED_ON=#{updatedOn},UPDATED_BY=#{updatedBy},DELETED=#{deleted}");
		whereId(idName);
		String sql = SQL();
		return sql;
	}
	

	public String deleteByObject(T obj) {
		BEGIN();
		DELETE_FROM(BaseDaoUtils.tableName(obj));
		WHERE(BaseDaoUtils.whereColumnNotNull(obj));
		return SQL();
	}

	public String deleteByParamNotEmpty(Map<String, Object> param) {
		removeEmpty(param);
		BEGIN();
		Serializable obj = (Serializable) param.get(BaseDaoUtils.SPACE_TABLE_NAME);
		DELETE_FROM(BaseDaoUtils.tableName(obj));
		param.remove(BaseDaoUtils.SPACE_TABLE_NAME);
		WHERE(BaseDaoUtils.whereColumnNotEmpty(param));
		return SQL();
	}

	public String deleteByParam(Map<String, Object> param) {
		BEGIN();
		Serializable obj = (Serializable) param.get(BaseDaoUtils.SPACE_TABLE_NAME);
		DELETE_FROM(BaseDaoUtils.tableName(obj));
		param.remove(BaseDaoUtils.SPACE_TABLE_NAME);
		WHERE(BaseDaoUtils.whereColumn(param));
		return SQL();
	}

	public String queryById(T obj) {
		String idName = BaseDaoUtils.id(obj);
		BEGIN();
		SELECT(BaseDaoUtils.queryColumn(obj));
		FROM(BaseDaoUtils.tableName(obj));
		whereId(idName);
		return SQL();
	}

	public String queryByObject(T obj) {
		BEGIN();
		SELECT(BaseDaoUtils.queryColumn(obj));
		FROM(BaseDaoUtils.tableName(obj));
		String where = BaseDaoUtils.whereColumnNotNull(obj);
		if (!"".equals(where)) {
			WHERE(where);
		}
		return SQL();
	}
	
	public String queryNextSequenceValue(String sequenceName){
		BEGIN();
		SELECT(sequenceName + ".Nextval ");
		FROM("dual");
		return SQL();
	}

	public String queryByParamNotEmpty(Map<String, Object> param) {
		removeEmpty(param);
		Serializable obj = (Serializable) param.get(BaseDaoUtils.SPACE_TABLE_NAME);
		param.remove(BaseDaoUtils.SPACE_TABLE_NAME);
		Object orderBy = param.get(BaseDaoUtils.MYBATIS_SPECIAL_STRING.ORDER_BY.name());
		param.remove(BaseDaoUtils.MYBATIS_SPECIAL_STRING.ORDER_BY.name());
		BEGIN();
		SELECT(BaseDaoUtils.queryColumn(obj));
		FROM(BaseDaoUtils.tableName(obj));
		if (!param.isEmpty())
			WHERE(BaseDaoUtils.whereColumnNotEmpty(param));
		if (null != orderBy)
			ORDER_BY(orderBy.toString());
		return SQL();
	}

	public String queryByParam(Map<String, Object> param) {
		Serializable obj = (Serializable) param.get(BaseDaoUtils.SPACE_TABLE_NAME);
		Object orderBy = param.get(BaseDaoUtils.MYBATIS_SPECIAL_STRING.ORDER_BY.name());
		param.remove(BaseDaoUtils.SPACE_TABLE_NAME);
		param.remove(BaseDaoUtils.MYBATIS_SPECIAL_STRING.ORDER_BY.name());
		BEGIN();
		SELECT(BaseDaoUtils.queryColumn(obj));
		FROM(BaseDaoUtils.tableName(obj));
		if (!param.isEmpty())
			WHERE(BaseDaoUtils.whereColumn(param));
		if (null != orderBy)
			ORDER_BY(orderBy.toString());
		return SQL();
	}

	public String queryObjectCount(T obj) {
		BEGIN();
		SELECT(" count(*) total ");
		FROM(BaseDaoUtils.tableName(obj));
		WHERE(BaseDaoUtils.whereColumnNotNull(obj));
		return SQL();
	}
	
	public String queryTotalCount(Class obj) {
		BEGIN();
		SELECT(" count(*) total ");
		FROM(BaseDaoUtils.tableName(obj));
		return SQL();
	}

	public String queryByParamNotEmptyCount(Map<String, Object> param) {
		removeEmpty(param);
		Serializable obj = (Serializable) param.remove(BaseDaoUtils.SPACE_TABLE_NAME);
		BEGIN();
		SELECT(" count(*) total ");
		FROM(BaseDaoUtils.tableName(obj));
		Object orderBy = param.remove(BaseDaoUtils.MYBATIS_SPECIAL_STRING.ORDER_BY.name());
		if (!param.isEmpty())
			WHERE(BaseDaoUtils.whereColumn(param));
		param.put(BaseDaoUtils.MYBATIS_SPECIAL_STRING.ORDER_BY.name(), orderBy);
		return SQL();
	}

	public String queryByParamCount(Map<String, Object> param) {
		Serializable obj = (Serializable) param.get(BaseDaoUtils.SPACE_TABLE_NAME);
		param.remove(BaseDaoUtils.SPACE_TABLE_NAME);
		BEGIN();
		SELECT(" count(1) total ");
		FROM(BaseDaoUtils.tableName(obj));
		Object orderBy = param.remove(BaseDaoUtils.MYBATIS_SPECIAL_STRING.ORDER_BY.name());
		if (!param.isEmpty())
			WHERE(BaseDaoUtils.whereColumn(param));
		param.put(BaseDaoUtils.MYBATIS_SPECIAL_STRING.ORDER_BY.name(), orderBy);
		return SQL();
	}

	public String queryPageByParamNotEmpty(Map<String, Object> param) {
		removeEmpty(param);
		Serializable obj = (Serializable) param.remove(BaseDaoUtils.SPACE_TABLE_NAME);
		Integer startRow = (Integer) param.remove(BaseDaoUtils.MYBATIS_SPECIAL_STRING.START_ROW.name());
		Integer endRow = (Integer) param.remove(BaseDaoUtils.MYBATIS_SPECIAL_STRING.END_ROW.name());
		if (startRow == null) {
			startRow = 0;
		}
		if (startRow < 0) {
			startRow = 0;
		}
		if (endRow == null) {
			endRow = 10;
		}
		if (endRow <= 0) {
			endRow = BaseDaoUtils.DEAFULT_PAGE_SIZE;
		}
		Object orderBy = param.remove(BaseDaoUtils.MYBATIS_SPECIAL_STRING.ORDER_BY.name());
		BEGIN();
		SELECT(BaseDaoUtils.queryColumn(obj));
		FROM(BaseDaoUtils.tableName(obj));
		if (!param.isEmpty())
			WHERE(BaseDaoUtils.whereColumnNotEmpty(param));
		if (orderBy != null) {
			ORDER_BY(orderBy.toString());
		}
		return getPageSql(startRow, endRow, SQL());
	}

	public String queryPageByParam(Map<String, Object> param) {

		Serializable obj = (Serializable) param.remove(BaseDaoUtils.SPACE_TABLE_NAME);
		Integer startRow = (Integer) param.remove(BaseDaoUtils.MYBATIS_SPECIAL_STRING.START_ROW.name());
		Integer endRow = (Integer) param.remove(BaseDaoUtils.MYBATIS_SPECIAL_STRING.END_ROW.name());
		if (startRow == null) {
			startRow = 0;
		}
		if (startRow < 0) {
			startRow = 0;
		}
		if (endRow == null) {
			endRow = 10;
		}
		if (endRow <= 0) {
			endRow = BaseDaoUtils.DEAFULT_PAGE_SIZE;
		}
		Object orderBy = param.remove(BaseDaoUtils.MYBATIS_SPECIAL_STRING.ORDER_BY.name());
		BEGIN();
		SELECT(BaseDaoUtils.queryColumn(obj));
		FROM(BaseDaoUtils.tableName(obj));
		if (!param.isEmpty())
			WHERE(BaseDaoUtils.whereColumn(param));
		if (orderBy != null) {
			ORDER_BY(orderBy.toString());
		}

		return getPageSql(startRow, endRow, SQL());
	}

	public String getPageSql(int startRow, int endRow, String sql) {
		StringBuilder sqlBuilder = new StringBuilder(sql.length() + 120);
		sqlBuilder.append("select * from ( select tmp_page.*, rownum row_id from ( ");
		sqlBuilder.append(sql);
		sqlBuilder.append(" ) tmp_page where rownum <= " + endRow + " ) where row_id > " + startRow + "");
		return sqlBuilder.toString();
	}

	public String find(PageSearch pageSearch) {
		BEGIN();
		SELECT(BaseDaoUtils.queryColumn(pageSearch.getEntityClass()));
		FROM(BaseDaoUtils.tableName(pageSearch.getEntityClass()));
		//没有条件不添加where语句
		if (!pageSearch.getFilters().isEmpty()){
			WHERE(MyBatisUtils.buildSql(pageSearch.getFilters()));
		}
		String[] properties = pageSearch.getSortProperties();
		if(properties!=null){
			String[] orders = pageSearch.getSortOrders();
			StringBuilder orderBy = new StringBuilder();
			for(int i=0;i<properties.length;i++){
				if(i>0){
					orderBy.append(",");
				}
				orderBy.append(FieldProperty.propertyToField(properties[i])).append(" ").append(orders[i]);
			}
			if (orderBy.length()>0) {
				ORDER_BY(orderBy.toString());
			}
		}
		return SQL();
	}

	public String isUnique(T obj) {
		BEGIN();
		SELECT("count(1)");
		FROM(BaseDaoUtils.tableName(obj));
		WHERE(BaseDaoUtils.whereColumnNotNull(obj));
		return SQL();
	}

	public String getAll(Class obj) {
		BEGIN();
		SELECT(BaseDaoUtils.queryColumn(obj));
		FROM(BaseDaoUtils.tableName(obj));
		return SQL();
	}

	public void whereId(final String idname) {
		WHERE(idname.replaceAll("([A-Z])", "_$1").toLowerCase() + "=#{" + idname + "}");
	}

	private void removeEmpty(Map<String, Object> params) {
		Iterator<String> iterator = params.keySet().iterator();
		while (iterator.hasNext()) {
			String key = iterator.next();
			if (key == null)
				params.remove(key);
			if (params.get(key) == null) {
				params.remove(key);
				iterator = params.keySet().iterator();
			}
		}
	}

}
