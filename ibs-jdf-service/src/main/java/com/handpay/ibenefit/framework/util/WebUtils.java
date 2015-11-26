/**
 * Copyright (c) 2005-2009 springside.org.cn
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *
 * $Id: WebUtils.java 763 2009-12-27 18:36:21Z calvinxiu $
 */
package com.handpay.ibenefit.framework.util;

import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.zip.GZIPOutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * @author calvin
 */
public final class WebUtils {

	private static final String DEFAULT_SEARCH_PREFIX = "search_";
	public static final long ONE_YEAR_SECONDS = 60 * 60 * 24 * 365;

	private WebUtils() {
	}

	public static void setExpiresHeader(HttpServletResponse response, long expiresSeconds) {
		// Http 1.0 header
		response.setDateHeader("Expires", System.currentTimeMillis() + expiresSeconds * 1000);
		// Http 1.1 header
		response.setHeader("Cache-Control", "max-age=" + expiresSeconds);
	}

	public static void setNoCacheHeader(HttpServletResponse response) {
		// Http 1.0 header
		response.setDateHeader("Expires", 0);
		// Http 1.1 header
		response.setHeader("Cache-Control", "no-cache");
	}

	public static void setLastModifiedHeader(HttpServletResponse response, long lastModifiedDate) {
		response.setDateHeader("Last-Modified", lastModifiedDate);
	}

	public static void setEtag(HttpServletResponse response, String etag) {
		response.setHeader("ETag", etag);
	}

	public static boolean checkIfModifiedSince(HttpServletRequest request, HttpServletResponse response,
	        long lastModified) {
		long ifModifiedSince = request.getDateHeader("If-Modified-Since");
		if ((ifModifiedSince != -1) && (lastModified < ifModifiedSince + 1000)) {
			response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
			return false;
		}
		return true;
	}

	public static boolean checkIfNoneMatchEtag(HttpServletRequest request, HttpServletResponse response, String etag) {
		String headerValue = request.getHeader("If-None-Match");
		if (headerValue != null) {
			boolean conditionSatisfied = false;
			if (!"*".equals(headerValue)) {
				StringTokenizer commaTokenizer = new StringTokenizer(headerValue, ",");

				while (!conditionSatisfied && commaTokenizer.hasMoreTokens()) {
					String currentToken = commaTokenizer.nextToken();
					if (currentToken.trim().equals(etag)) {
						conditionSatisfied = true;
					}
				}
			} else {
				conditionSatisfied = true;
			}

			if (conditionSatisfied) {
				response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
				response.setHeader("ETag", etag);
				return false;
			}
		}
		return true;
	}

	public static boolean checkAccetptGzip(HttpServletRequest request) {
		// Http1.1 header
		String acceptEncoding = request.getHeader("Accept-Encoding");

		if (StringUtils.contains(acceptEncoding, "gzip")) {
			return true;
		} else {
			return false;
		}
	}

	public static OutputStream buildGzipOutputStream(HttpServletResponse response) throws IOException {
		response.setHeader("Content-Encoding", "gzip");
		response.setHeader("Vary", "Accept-Encoding");
		return new GZIPOutputStream(response.getOutputStream());
	}

	public static void setDownloadableHeader(HttpServletResponse response, String fileName) {
		response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
	}

	@SuppressWarnings("unchecked")
	public static Map<String, Object> getParametersStartingWith(HttpServletRequest request, String prefix) {
		return org.springframework.web.util.WebUtils.getParametersStartingWith(request, prefix);
	}

	public static Map<String, Object> getSearchParameterMap(HttpServletRequest request, Class DomainObjecClass) {
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		Map<String, Object> parameterMap = new HashMap();
		Map requestMap = getParametersStartingWith(request, DEFAULT_SEARCH_PREFIX);
		boolean matchAll = true;
		PropertyDescriptor[] beandesc = PropertyUtils.getPropertyDescriptors(DomainObjecClass);
		for (int i = 0; i < beandesc.length; i++) {
			PropertyDescriptor descriptor = beandesc[i];
			String propertyName = descriptor.getName();
			Class propertyClass = descriptor.getPropertyType();
			if ("class".equals(propertyName)) {
				continue;
			}
			if (propertyClass == Date.class) {
				String after = (String) requestMap.get(propertyName + "After");
				if (after != null && after.trim().length() > 0) {
					try {
						Date afterDate = dateFormat.parse(after);
						parameterMap.put(propertyName + "After", afterDate);
					} catch (Exception e) {
					}
				}
				String befor = (String) requestMap.get(propertyName + "Befor");
				if (befor != null && befor.trim().length() > 0) {
					try {
						Date beforDate = dateFormat.parse(befor);
						// to the end of the day
						long dayTail = beforDate.getTime() + 24 * 3600 * 1000 - 1;
						parameterMap.put(propertyName + "Befor", new Date(dayTail));
					} catch (Exception e) {
					}
				}
			} else if (propertyClass == String.class) {
				String value = (String) requestMap.get(propertyName);
				if (value != null && value.trim().length() > 0) {
					if (matchAll) {
						parameterMap.put(propertyName, "%" + value + "%");
					} else {
						parameterMap.put(propertyName, value + "%");
					}
				}
			} else if (propertyClass == Long.class) {
				String value = (String) requestMap.get(propertyName);
				if (value != null && value.trim().length() > 0) {
					parameterMap.put(propertyName, Long.parseLong(value));
				}
			} else if (propertyClass == Integer.class) {
				String value = (String) requestMap.get(propertyName);
				if (value != null && value.trim().length() > 0) {
					parameterMap.put(propertyName, Integer.parseInt(value));
				}
			} else if (propertyClass == Double.class) {
				String value = (String) requestMap.get(propertyName);
				if (value != null && value.trim().length() > 0) {
					parameterMap.put(propertyName, Double.parseDouble(value));
				}
			} else if (propertyClass == Float.class) {
				String value = (String) requestMap.get(propertyName);
				if (value != null && value.trim().length() > 0) {
					parameterMap.put(propertyName, Float.parseFloat(value));
				}
			} else if (propertyClass == Boolean.class) {
				String value = (String) requestMap.get(propertyName);
				if (value != null && value.trim().length() > 0) {
					parameterMap.put(propertyName, Boolean.parseBoolean(value));
				}
			}
		}
		return parameterMap;
	}
}
