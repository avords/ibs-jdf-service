package com.handpay.ibenefit.framework.util;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;
/**
 * @author Bob.Pu
 * Date variable replace utils
 */
public final class DateTemplateUtils {
	
	private static final Logger LOGGER = Logger.getLogger(DateTemplateUtils.class);

	public static final String TEMPLATE_BEGIN = "\\[";

	public static final String TEMPLATE_END = "\\]";
	
	private static final String DATE_TYPE_YYYY = "yyyy";

	private static final String DATE_TYPE_YYYYMM = DATE_TYPE_YYYY +"MM";

	private static final String DATA_TYPE_DD = "dd";

	private static final String DATE_TYPE_YYYYMMDD = DATE_TYPE_YYYYMM + DATA_TYPE_DD;

	private static final String DATE_TYPE_YYYY_MM = DATE_TYPE_YYYY + "-MM";

	private static final String DATE_TYPE_YYYY_MM_DD = DATE_TYPE_YYYY_MM + "-" + DATA_TYPE_DD;

	private static final String ALGORITHEM_PLUS_SIGN = "+";

	private static final String ALGORITHEM_SUBTRACTION_SIGN = "-";

	private static final String TABLE_SUFFIX_BEGIN = "_";

	private DateTemplateUtils(){}

	public static String parseTemplate(String in,Date defaultDate){
		Date today = new Date();
		if(null==defaultDate){
			defaultDate = today;
		}
		StringBuilder result = new StringBuilder(in.length());
		String[] arr = in.split("[" + TEMPLATE_BEGIN + "]");
		String template = "";
		if(arr.length>1){
			result.append(arr[0]);
			for(int i=1;i<arr.length;i++){
				String[] tempArr = arr[i].split("[" + TEMPLATE_END + "]");
				if(tempArr.length>=1){
					template = tempArr[0];
					//以"_"开始的，作特殊处理，认为是表名后缀的开始，哪果当前时间相等和系统时间相等，忽略此后缀
					boolean isIgnore = false;
					if(template.startsWith(TABLE_SUFFIX_BEGIN)){
						template = template.substring(1);
						String a = DateUtils.getDateFormat(template).format(today);
						String b = DateUtils.getDateFormat(template).format(defaultDate);
						isIgnore = a.equals(b);
						if(!isIgnore){
							result.append(TABLE_SUFFIX_BEGIN);
						}
					}
					if(!isIgnore){
						result.append(parseDateTemplate(template,defaultDate));
					}
					result.append(tempArr.length>1?tempArr[1]:"");
				}else{
					LOGGER.error("输入有误");
				}
			}
		}else{
			result.append(in);
		}
		return result.toString();
	}

	public static String parseDateTemplate(String template,Date defaultDate){
		String realDate = "";

		String upperTemplate = template.toUpperCase();
		String currentTemplate = null;
		//从最长的开始
		if(upperTemplate.indexOf(DATE_TYPE_YYYY_MM_DD.toUpperCase())!=-1){
			currentTemplate = DATE_TYPE_YYYY_MM_DD;
		}else if(upperTemplate.indexOf(DATE_TYPE_YYYY_MM.toUpperCase())!=-1){
			currentTemplate = DATE_TYPE_YYYY_MM;
		}else if(upperTemplate.indexOf(DATE_TYPE_YYYYMMDD.toUpperCase())!=-1){
			currentTemplate = DATE_TYPE_YYYYMMDD;
		}else if(upperTemplate.indexOf(DATE_TYPE_YYYYMM.toUpperCase())!=-1){
			currentTemplate = DATE_TYPE_YYYYMM;
		}else if(upperTemplate.indexOf(DATE_TYPE_YYYY.toUpperCase())!=-1){
			currentTemplate = DATE_TYPE_YYYY;
		}
		if(currentTemplate!=null){
			realDate = getRealDateStr(upperTemplate, currentTemplate, defaultDate);
		}
		return realDate;
	}

	private static String getRealDateStr(String upperTemplate, String currentTemplate,Date realDate) {
		//包含+/-
		if(upperTemplate.length()>currentTemplate.length()){
			String dayStr;
			//+
			if(upperTemplate.indexOf(ALGORITHEM_PLUS_SIGN)!=-1){
				dayStr = upperTemplate.split("\\" + ALGORITHEM_PLUS_SIGN)[1].trim();
				int num = Integer.parseInt(dayStr);
				if(currentTemplate.indexOf(DATA_TYPE_DD)!=-1){
					realDate =DateUtils.getPreviousOrNextDaysOfDate(realDate,num);
				}else{
					realDate =DateUtils.getPreviousOrNextMonthsOfDate(realDate,num);
				}
			}else if(upperTemplate.indexOf(ALGORITHEM_SUBTRACTION_SIGN)!=-1){
				String[] arr = upperTemplate.split(ALGORITHEM_SUBTRACTION_SIGN);
				dayStr = arr[arr.length-1].trim();
				int num = Integer.parseInt(dayStr);
				if(currentTemplate.indexOf(DATA_TYPE_DD)!=-1){
					realDate =DateUtils.getPreviousOrNextDaysOfDate(realDate,-num);
				}else{
					realDate =DateUtils.getPreviousOrNextMonthsOfDate(realDate,-num);
				}
			}
		}
		return new SimpleDateFormat(currentTemplate).format(realDate);
	}
}
