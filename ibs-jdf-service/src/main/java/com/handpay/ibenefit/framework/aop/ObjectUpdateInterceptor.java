package com.handpay.ibenefit.framework.aop;

import java.lang.reflect.Method;

import org.springframework.aop.AfterReturningAdvice;

import com.handpay.ibenefit.framework.cache.ICacheManager;
import com.handpay.ibenefit.framework.entity.AbstractEntity;

public class ObjectUpdateInterceptor implements AfterReturningAdvice {
	
	private ICacheManager cacheManager;

	public void afterReturning(Object returnValue, Method method, Object[] arguments, Object target) throws Throwable {
		String targetName = target.getClass().getName();
		if(arguments.length==1){
			if(arguments[0] instanceof AbstractEntity || arguments[0] instanceof Long){
				String cacheKey = KeyGenerator.getCacheKey(targetName, "getByObjectId", arguments);
				//Delete directly, because the transit property's load is not called automatically so can not override it.
				cacheManager.delete(cacheKey);
			}
		}
	}

	public void setCacheManager(ICacheManager cacheManager) {
		this.cacheManager = cacheManager;
	}
	
}
