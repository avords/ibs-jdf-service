package com.handpay.ibenefit.framework.service;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.transaction.annotation.Transactional;

import com.handpay.ibenefit.framework.dao.BaseDao;
import com.handpay.ibenefit.framework.entity.AuditEntity;
import com.handpay.ibenefit.framework.entity.BaseEntity;
import com.handpay.ibenefit.framework.entity.ForeverEntity;
import com.handpay.ibenefit.framework.util.PageSearch;

/**
 * Basic Service Support CRUD, page query and common search function
 * 
 * @author pubx 2010-3-29 02:26:46
 */
public abstract class BaseService<T> implements Manager<T> {

	private static final Logger LOGGER = Logger.getLogger(BaseService.class);

	public abstract BaseDao<T> getDao();
	
	@Override
	@Transactional
	public void delete(Long objectId) {
		if (null != objectId) {
			try {
				T baseEntity = (T) actualArgumentType.newInstance();
				((BaseEntity) baseEntity).setObjectId(objectId);
				getDao().delete(baseEntity);
			} catch (Exception e) {
				LOGGER.error("delete", e);
			}
		}
	}
	
	@Override
	@Transactional
	public void delete(ForeverEntity entity) {
		if (null != entity) {
			entity.setDeleted(ForeverEntity.DELETED_YES);
			getDao().deleteForeverEntity(entity);
		}
	}
	
	@Override
	@Transactional
	public T save(T entity) {
		if (entity instanceof BaseEntity) {
			BaseEntity baseEntity = (BaseEntity) entity;
			if(entity instanceof ForeverEntity){
				((ForeverEntity)baseEntity).setDeleted(ForeverEntity.DELETED_NO);
			}
			if (baseEntity.getObjectId() == null) {
				getDao().save(entity);
			} else if(entity instanceof AuditEntity){
				getDao().updateObjectExcludeCreateInfo(entity);
			} else{
				getDao().updateNotNullById(entity);
			}
		} else {
			getDao().save(entity);
		}
		return entity;
	}
	
	/**
	 * 更新实体中所有属性
	 * @param entity
	 * @return
	 */
	@Transactional
	public T updateById(T entity){
		if (entity instanceof BaseEntity) {
			BaseEntity baseEntity = (BaseEntity) entity;
			if(entity instanceof ForeverEntity){
				((ForeverEntity)baseEntity).setDeleted(ForeverEntity.DELETED_NO);
			}
			if (baseEntity.getObjectId() == null) {
				getDao().save(entity);
			} else if(entity instanceof AuditEntity){
				getDao().updateObjectExcludeCreateInfo(entity);
			} else{
				getDao().updateById(entity);
			}
		} else {
			getDao().save(entity);
		}
		return entity;
	}

	@Override
	public void update(Long id) {
		// Nothing to do
	}

	/**
	 * Page query
	 */
	@Override
	public PageSearch find(PageSearch page) {
		List<T> data = getDao().find(page);
		page.setList(data);
		return page;
	}

	public Long getTotalCount() {
		return getDao().queryTotalCount(actualArgumentType);
	}
	
	@Override
	public List<T> getBySample(T t) {
		return getDao().queryByObject(t);
	}
	
	@Override
	public void deleteBySample(T sample){
		getDao().deleteByObject(sample);
	}

	@Override
	public T getByObjectId(Long objectId) {
		if (null == objectId) {
			return null;
		}
		try {
			T baseEntity = (T) actualArgumentType.newInstance();
			((BaseEntity) baseEntity).setObjectId(objectId);
			return getDao().queryById(baseEntity);
		} catch (Exception e) {
			LOGGER.error("getByObjectId", e);
			return null;
		}
	}

	private Class<T> actualArgumentType;

	public BaseService() {
		final Type thisType = getClass().getGenericSuperclass();
		final Type type;
		try {
			if (thisType instanceof ParameterizedType) {
				type = ((ParameterizedType) thisType).getActualTypeArguments()[0];
			} else if (thisType instanceof Class) {
				type = ((ParameterizedType) ((Class) thisType).getGenericSuperclass()).getActualTypeArguments()[0];
			} else {
				throw new IllegalArgumentException("Problem handling type construction for " + getClass());
			}
			if (type instanceof Class) {
				this.actualArgumentType = (Class<T>) type;
			} else if (type instanceof ParameterizedType) {
				this.actualArgumentType = (Class<T>) ((ParameterizedType) type).getRawType();
			} else {
				throw new IllegalArgumentException("Problem determining the class of the generic for " + getClass());
			}
		} catch (Exception e) {
			LOGGER.warn(e.getMessage());
		}
		LOGGER.debug(getClass() + ":" + actualArgumentType);
	}

	protected Class getActualArgumentType() {
		return actualArgumentType;
	}

	public List<T> getAll() {
		return getDao().getAll(getActualArgumentType());
	}

	@Override
	public boolean isUnique(T entity) {
		Long count = getDao().queryObjectCount(entity);
		if (entity instanceof BaseEntity) {
			BaseEntity baseEntity = (BaseEntity) entity;
			Long objectId = baseEntity.getObjectId();
			//no primary key
			if(objectId==null){
				return count==0;
			}else {
				baseEntity.setObjectId(null);
				//query by checked field
				List<BaseEntity> list = (List<BaseEntity>)getBySample(entity);
				for(BaseEntity temp : list){
					if(!temp.getObjectId().equals(objectId)){
						return false;
					}
				}
				return true;
			}
		} else {
			return count == 0;
		}
	}
	
	public Long getObjectCount(T sample){
		return getDao().queryObjectCount(sample);
	}

}
