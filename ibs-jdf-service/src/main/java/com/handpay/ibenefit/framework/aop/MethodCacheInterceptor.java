package com.handpay.ibenefit.framework.aop;

import java.lang.reflect.Method;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import com.handpay.ibenefit.framework.cache.ICacheManager;
import com.handpay.ibenefit.framework.cache.SerializeUtil;

public class MethodCacheInterceptor implements MethodInterceptor {

	private ICacheManager cacheManager;

	public Object invoke(MethodInvocation invocation) throws Throwable {
		Object[] arguments = invocation.getArguments();
		Method method = invocation.getMethod();
		String targetName = method.getDeclaringClass().getName();
		String methodName = method.getName();
		String cacheKey = KeyGenerator.getCacheKey(targetName, methodName, arguments);
		String cacheObject = cacheManager.getString(cacheKey);
		Object result = SerializeUtil.unserialize(cacheObject);
		if (null == result) {
			//为空，调用原方法后缓存结果
			result = invocation.proceed();
			//默认缓存1个小时
			cacheManager.set(cacheKey, SerializeUtil.serializeToString(result),3600);
		}
		return result;
	}

	public void setCacheManager(ICacheManager cacheManager) {
		this.cacheManager = cacheManager;
	}

}
