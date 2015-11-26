package com.handpay.ibenefit.framework.util;

import org.apache.commons.lang3.StringUtils;

public final class NamingUtils {
    /**
     *
     * @param camelStyleString
     * @return
     */
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
                //Upper character
                upperCaseString += "_";
            }
            upperCaseString += camelStyleString.charAt(i);
        }
        upperCaseString = upperCaseString.toUpperCase();
        return upperCaseString;
    }
}
