package com.handpay.ibenefit.framework.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;

import com.handpay.ibenefit.framework.util.PropertyFilter;
import com.handpay.ibenefit.framework.util.PropertyFilter.MatchType;
import com.handpay.ibenefit.framework.util.WebUtils;
import com.handpay.ibenefit.framework.validate.FieldProperty;

public final class MyBatisUtils {

	private MyBatisUtils() {
	}

	public static List<PropertyFilter> buildPropertyFilters(final HttpServletRequest request,
			final String defaultObectName) {
		return buildPropertyFilters(request, "search_", defaultObectName);
	}

	/**
	 * eg. search_EQS_name
	 */
	public static List<PropertyFilter> buildPropertyFilters(final HttpServletRequest request,
			final String filterPrefix, final String defaultObjectName) {
		List<PropertyFilter> filterList = new ArrayList<PropertyFilter>();
		// Get request parameter
		Map<String, Object> filterParamMap = WebUtils.getParametersStartingWith(request, filterPrefix);
		// Construct PropertyFilter list
		for (Map.Entry<String, Object> entry : filterParamMap.entrySet()) {
			String filterName = entry.getKey();
			String value = entry.getValue().toString();
			// Ignore when parameter value is blank
			if (StringUtils.isNotBlank(value)) {
				PropertyFilter filter = new PropertyFilter(defaultObjectName, filterName, value);
				filterList.add(filter);
			}
		}
		return filterList;
	}

	public static String upperFirstName(String objectName) {
		return objectName.substring(0, 1).toUpperCase() + objectName.substring(1);
	}

	public static String lowerFirstName(String objectName) {
		return objectName.substring(0, 1).toLowerCase() + objectName.substring(1);
	}

	public static Object getQueryValues(PropertyFilter propertyFilter) {
		MatchType matchType = propertyFilter.getMatchType();
		Object result = propertyFilter.getPropertyValue();
		if (result instanceof String) {
			switch (matchType) {
			case LIKE:
			case NLIKE:
				result = "%" + result + "%";
				break;
			case START:
				result = result + "%";
				break;
			case END:
				result = "%" + result;
			default:
				break;
			}
		}
		return result;
	}

	public static Object[] getQueryParameter(final List<PropertyFilter> filters) {
		List<Object> result = new ArrayList<Object>();
		for (PropertyFilter filter : filters) {
			if (MatchType.OR.equals(filter.getMatchType())) {
				for (PropertyFilter propertyFilter : filter.getOrPropertyFilters()) {
					if (filter.getMatchType() != MatchType.IN && filter.getMatchType() != MatchType.NN) {
						result.add(getQueryValues(propertyFilter));
					}
				}
			} else if(MatchType.IC.equals(filter.getMatchType())||MatchType.NC.equals(filter.getMatchType())){
				List<Object> list = filter.getPropertyValues();
				result.addAll(list);
			}else {
				if (filter.getMatchType() != MatchType.IN && filter.getMatchType() != MatchType.NN) {
					result.add(getQueryValues(filter));
				}
			}
		}
		return result.toArray();
	}


	public static String convertCamelStyleToUpperCase(String camelStyleString) {
        if (StringUtils.isBlank(camelStyleString)) {
            return null;
        }
        if (camelStyleString.length() == 1) {
            return camelStyleString.toUpperCase();
        }
        String upperCaseString = "";
        upperCaseString += camelStyleString.charAt(0);
        for (int i = 1; i < camelStyleString.length(); i++) {
            if ((camelStyleString.charAt(i) >= 'A')
                    && (camelStyleString.charAt(i) <= 'Z')) {
                //如果为大写字母,说明为单词开头,则加下划线分割
                upperCaseString += "_";
            }
            upperCaseString += camelStyleString.charAt(i);
        }
        upperCaseString = upperCaseString.toUpperCase();
        return upperCaseString;
    }

	public static String buildSql(final List<PropertyFilter> filters){
		StringBuilder result = new StringBuilder();
		for(PropertyFilter filter : filters){
			if(result.length()>0){
				result.append(" AND ");
			}
			if(filter.getMatchType().equals(MatchType.IC)||filter.getMatchType().equals(MatchType.NC)){
				result.append(FieldProperty.propertyToField(filter.getPropertyName())).append(" ").append(getCompare(filter.getMatchType())).append(" (");
				result.append(filter.getPropertyValueString()).append(") ");
			} else {
				//为大于、小于等添加别名
				if(filter.getMatchType()== MatchType.GE || filter.getMatchType() == MatchType.GT){
					result.append(FieldProperty.propertyToField(filter.getPropertyName())).append(" ").append(getCompare(filter.getMatchType()));
					result.append(" #{paramMap.").append(filter.getPropertyName()).append("After} ");
				}else if(filter.getMatchType()== MatchType.LE || filter.getMatchType() == MatchType.LT){
					result.append(FieldProperty.propertyToField(filter.getPropertyName())).append(" ").append(getCompare(filter.getMatchType()));
					result.append(" #{paramMap.").append(filter.getPropertyName()).append("Before} ");
				}else{
					result.append(FieldProperty.propertyToField(filter.getPropertyName())).append(" ").append(getCompare(filter.getMatchType()));
					result.append(" #{paramMap.").append(filter.getPropertyName()).append("} ");
				}
			}
		}
		if(result.length()>0){
			return result.toString();
		}
		return "";
	}

	private static String getCompare(MatchType matchType){
		String result = null;
		switch (matchType) {
		case EQ:
			result = "=";
			break;
		case START:
		case LIKE:
			result= "like";
			break;
		case LE:
			result = "<=";
			break;
		case LT:
			result = "<";
			break;
		case GE:
			result = ">=";
			break;
		case GT:
			result = ">";
			break;
		case NE:
			result = "!=";
			break;
		case IN:
			result = "is null";
			break;
		case NN:
			result = "is not null";
			break;
		case NLIKE:
			result = "not like";
			break;
		case IC:
			result = "in";
			break;
		case NC:
			result= "not in";
			break;
		default:
			result = "=";
			break;
		}
		return result;
	}
}
