package com.handpay.ibenefit.framework.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.log4j.Logger;

/**
 * 基于数据库的主键生成器
 * 线程不安全
 * @author bob.pu
 *
 */
public class TableCachedGenerator {
	
	/** Default table name */
	private static final int DEFAULT_CACHE_SIZE = 50;
	
	//最大值
	private long hi;
	//当前值
	private long lo;
	//数据库当前序列值
	private long maxLo;
	//缓存的数值个数
	private int cache;
	
	private static final Logger LOGGER = Logger.getLogger(TableCachedGenerator.class);

	private String tableName = "id_sequences";
	private String valueColumnName = "sequence_next_hi_value";
	private String segmentColumnName = "sequence_name";
	
	private String query;
	private String update;
	private String insert;
	private String sequenceName;
	
	/**
	 * 设置了cacheSize的不能变更为更小的缓存数
	 * @param cacheSize
	 * @param sequenceName
	 */
	public TableCachedGenerator(int cacheSize, String sequenceName) {
		cache = cacheSize;
		this.sequenceName = sequenceName;
		query = buildSelectQuery();
		update = buildUpdateQuery();
		insert = buildInsertQuery();
		maxLo = 1;
		hi = 1;
		lo = hi;
	}

	public TableCachedGenerator(String sequenceName) {
		this(DEFAULT_CACHE_SIZE, sequenceName);
	}
	
	/**
	 * 
	 * @param connection 数据库链接，使用完后会自动关闭
	 * @return
	 */
	public long generate(Connection connection){
		if (maxLo <= 1) {
			maxLo = ((Integer) doWorkInCurrentTransaction(connection, sequenceName)).intValue();
			if (maxLo == 0){
				((Integer) doWorkInCurrentTransaction(connection, sequenceName)).intValue();
			}
			lo = maxLo * cache + 1;
			hi = (maxLo + 1) * cache;
		}
		if (lo > hi) {
			maxLo = ((Integer) doWorkInCurrentTransaction(connection, sequenceName)).intValue();
			lo = (maxLo * cache) + 1;
			hi = (maxLo + 1) * cache;
			LOGGER.debug("new hi value: " + maxLo);
		}
		//自动关闭链接
		if(connection!=null){
			try{
				if(!connection.isClosed()){
					connection.close();
				}
			}catch(Exception e){
			}
		}
		return lo++;
	}

	private Integer doWorkInCurrentTransaction(Connection conn,String sequenceName) {
		int result = -1;
		int rows=0;
		do {
			// The loop ensures atomicity of the
			// select + update even for no transaction
			// or read committed isolation level
			PreparedStatement qps = null;
			try {
				qps = conn.prepareStatement(query);
				qps.setObject(1, sequenceName);
				ResultSet rs = qps.executeQuery();
				if (!rs.next()) {
					PreparedStatement ups = conn.prepareStatement(insert);
					try {
						ups.setObject(1, sequenceName);
						ups.setLong(2, maxLo);
						ups.executeUpdate();
					} catch (SQLException sqle) {
						LOGGER.error("could not update hi value in: " + tableName, sqle);
						throw sqle;
					} finally {
						ups.close();
					}
				}else{
					result = rs.getInt(1);
					rs.close();
				}
			} catch (SQLException sqle) {
				LOGGER.error("could not read a hi value", sqle);
			} finally {
				if(qps!=null){
					try{
						qps.close();
					}catch(Exception e){
					}
				}
			}
			if(result!=-1){
				PreparedStatement ups = null ;
				try {
					ups = conn.prepareStatement(update);
					ups.setInt(1, result + 1);
					ups.setInt(2, result);
					ups.setString(3, sequenceName);
					rows = ups.executeUpdate();
					ups.setObject(3, sequenceName);
				} catch (SQLException sqle) {
					LOGGER.error("could not update hi value in: " + tableName, sqle);
				} finally {
					if(conn!=null){
						try{
							ups.close();
							conn.close();
						}catch(Exception e){
						}
					}
				}
			}
		} while (rows == 0);
		return result;
	}
	
	protected String buildSelectQuery() {
		final String alias = "tbl";
		String query = "select " + qualify( alias, valueColumnName ) +
				" from " + tableName + ' ' + alias +
				" where " + qualify( alias, segmentColumnName ) + "=? for update";
		return query;
	}

	protected String buildUpdateQuery() {
		return "update " + tableName +
				" set " + valueColumnName + "=? " +
				" where " + valueColumnName + "=? and " + segmentColumnName + "=?";
	}

	protected String buildInsertQuery() {
		return "insert into " + tableName + " (" + segmentColumnName + ", " + valueColumnName + ") " + " values (?,?)";
	}
	
	private static String qualify(String prefix, String name) {
		if ( name == null || prefix == null ) {
			throw new NullPointerException();
		}
		return new StringBuilder( prefix.length() + name.length() + 1 )
				.append(prefix)
				.append('.')
				.append(name)
				.toString();
	}
}