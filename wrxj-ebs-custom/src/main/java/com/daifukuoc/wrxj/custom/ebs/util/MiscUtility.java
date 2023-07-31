package com.daifukuoc.wrxj.custom.ebs.util;

import com.daifukuamerica.wrxj.util.SKDCUtility;

public class MiscUtility extends SKDCUtility {

	/**
	 * This method returns the string array by splitting using the supplied delimiter
	 * @param str
	 * @param delim
	 * @return
	 */
	public static String[] getTokens(String str, String delim) {
		String[] stringSplit = str.split(delim);
		return stringSplit;
	}
	
	public static int getIntegerValue(String value) {
		int intVal = 0;
		if(value!=null && value.trim().length() > 0) {
			try {
				intVal = Integer.parseInt(value);
			} catch (NumberFormatException e) {

			}
		}
		return intVal;
	}
	
	public static short getShortValue(String value) {
		short shortVal = 0;
		if(value!=null && value.trim().length() > 0) {
			try {
				shortVal = Short.parseShort(value);
			} catch (NumberFormatException e) {

			}
		}
		return shortVal;
	}
}
