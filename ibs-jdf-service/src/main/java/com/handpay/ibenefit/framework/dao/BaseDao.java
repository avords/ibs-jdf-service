package com.handpay.ibenefit.framework.dao;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.DeleteProvider;
import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.annotations.UpdateProvider;

import com.handpay.ibenefit.framework.entity.ForeverEntity;
import com.handpay.ibenefit.framework.util.PageSearch;

/**
 * Base Dao
 * 提供基础的增、删、改、查模板方法，不霜要再在Mapping文件中实现
 * @param <T>
 */
public interface BaseDao<T> {

	/**
	 * 根据ID删除对象
	 * @param t
	 */
	@DeleteProvider(type = MyBatisTemplate.class, method = "deleteById")
	void delete(T t);
	

	/**
	 * 根据ID删除对象
	 * @param t
	 */
	@DeleteProvider(type = MyBatisTemplate.class, method = "deleteForeverEntity")
	void deleteForeverEntity(ForeverEntity t);
	
	/**
	 * 保存对象
	 * @param entity
	 */
	@InsertProvider(type = MyBatisTemplate.class, method = "save")
	@Options(useGeneratedKeys = false)  
	void save(T entity);

	/**
	 * 分页查询
	 * @param page
	 * @return
	 */
	@SelectProvider(type = MyBatisTemplate.class, method = "find")
	List<T> find(PageSearch page);

	/**
	 * 查询对象的全部记录
	 * @param obj
	 * @return
	 */
	@SelectProvider(type = MyBatisTemplate.class, method = "getAll")
	List<T> getAll(Class obj);
	
	/**
	 * 根据主键更新数据中不为空的属性
	 * @param obj
	 * @return
	 */
	@UpdateProvider(type = MyBatisTemplate.class, method = "updateNotNullById")
	int updateNotNullById(T obj);

	/**
	 * 更新数据，不包括创建人、创建时间
	 * @param obj
	 * @return
	 */
	@UpdateProvider(type = MyBatisTemplate.class, method = "updateObjectExcludeCreateInfo")
	int updateObjectExcludeCreateInfo(T obj);
	
	/**
	 * 根据主键更新数据的全部属性
	 * @param obj
	 * @return
	 */
	@UpdateProvider(type = MyBatisTemplate.class, method = "updateById")
	int updateById(T obj);

	/**
	 * 根据主键删除数据
	 * @param id
	 * @return
	 */
	@DeleteProvider(type = MyBatisTemplate.class, method = "deleteById")
	int deleteById(Number id);

	/**
	 * 根据对象中不为空的条件删除数据
	 * @param obj
	 * @return
	 */
	@DeleteProvider(type = MyBatisTemplate.class, method = "deleteByObject")
	int deleteByObject(T obj);

	/**
	 * 根据参数中不为空的条件删除数据,key对应实体中的属性
	 * @param param
	 * @return
	 */
	@DeleteProvider(type = MyBatisTemplate.class, method = "deleteByParamNotEmpty")
	int deleteByParamNotEmpty(Map<String, Object> param);

	/**
	 * 根据参数中条件删除数据,key对应实体中的属性
	 * @param param
	 * @return
	 */
	@DeleteProvider(type = MyBatisTemplate.class, method = "deleteByParam")
	int deleteByParam(Map<String, Object> param);

	/**
	 * 根据主键查询数据
	 * @param obj
	 * @return
	 */
	@SelectProvider(type = MyBatisTemplate.class, method = "queryById")
	T queryById(T obj);

	/**
	 * 根据对象中不为空的属性查询列表
	 * @param obj
	 * @return
	 */
	@SelectProvider(type = MyBatisTemplate.class, method = "queryByObject")
	List<T> queryByObject(T obj);

	/**
	 * 根据参数中不为空的属性查询列表，key对应实体中的属性
	 * @param params
	 * @return
	 */
	@SelectProvider(type = MyBatisTemplate.class, method = "queryByParamNotEmpty")
	List<T> queryByParamNotEmpty(Map<String, Object> params);

	/**
	 * 根据参数查询列表，key对应实体中的属性
	 * @param params
	 * @return
	 */
	@SelectProvider(type = MyBatisTemplate.class, method = "queryByParam")
	List<T> queryByParam(Map<String, Object> params);

	/**
	 * 根据对象中不为空的属性查询记录数
	 * @param obj
	 * @return
	 */
	@SelectProvider(type = MyBatisTemplate.class, method = "queryObjectCount")
	Long queryObjectCount(T obj);

	/**
	 * 根据对象中不为空的属性查询总数
	 * @param obj
	 * @return
	 */
	@SelectProvider(type = MyBatisTemplate.class, method = "queryTotalCount")
	Long queryTotalCount(Class obj);
	
	/**
	 * 取序列的值
	 * @param sequenceName
	 * @return
	 */
	@SelectProvider(type = MyBatisTemplate.class, method = "queryNextSequenceValue")
	Long queryNextSequenceValue(String sequenceName);
}
