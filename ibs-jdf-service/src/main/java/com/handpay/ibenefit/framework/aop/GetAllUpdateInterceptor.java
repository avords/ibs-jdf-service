package com.handpay.ibenefit.framework.aop;

import java.lang.reflect.Method;

import org.springframework.aop.AfterReturningAdvice;

import com.handpay.ibenefit.framework.cache.ICacheManager;

public class GetAllUpdateInterceptor implements AfterReturningAdvice {
	private ICacheManager cacheManager;

	public void afterReturning(Object returnValue, Method method, Object[] arguments, Object target) throws Throwable {
		String targetName = target.getClass().getName();
		String cacheKey = KeyGenerator.getCacheKey(targetName, "getAll", null);
		cacheManager.delete(cacheKey);
	}

	public void setCacheManager(ICacheManager cacheManager) {
		this.cacheManager = cacheManager;
	}
}
