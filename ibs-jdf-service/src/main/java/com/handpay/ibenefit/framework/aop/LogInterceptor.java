package com.handpay.ibenefit.framework.aop;

import java.lang.reflect.Method;
import java.util.Date;

import org.apache.log4j.Logger;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

//@Aspect
//@Component
public class LogInterceptor {
	
	private static final Logger LOGGER = Logger.getLogger(LogInterceptor.class);
	
	@Pointcut("execution(public * com.handpay.ibenefit.**.*(..))")
	public void pointCut() {
	}

	@Around("pointCut()")
	public Object log(ProceedingJoinPoint jp) throws Throwable {
		MethodSignature joinPointObject = (MethodSignature) jp.getSignature();
		Method method = joinPointObject.getMethod();
		String name = method.getName();
		Class<?>[] parameterTypes = method.getParameterTypes();
		Object target = jp.getTarget();
		method = target.getClass().getMethod(name, parameterTypes);
		Date begin = new Date();
		LOGGER.info("BEGIN:" + begin);
		Object result = jp.proceed();
		LOGGER.info("SPEND:" + (new Date().getTime()-begin.getTime()));
		return result;
	}
}
