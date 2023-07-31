package com.daifukuoc.wrxj.custom.ebs.host.util;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;

/**
 * Conversion utility
 * 
 * @author LK
 *
 */
public class ConversionUtil {

    /**
     * Convert yyyyMMddHHmmss format string to java.util.Date
     * 
     * @param dateString yyyyMMddHHmmss format string, should not be null
     * @return java.util.Date Converted from the given string
     * @throws DateTimeParseException if parsing failed
     */
    public static Date convertDateStringToDate(String dateString) throws DateTimeParseException {
        LocalDateTime localDateTime = LocalDateTime.parse(dateString, DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        Date convertedDate = Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
        return convertedDate;
    }
    
    /**
     * Method to format the address to remove leading zeroes.
     * 
     * @param address - without formatted address
     * @return formattedAddress - with formatted address (removed 0's)
     */
    public static String formatAddressForConveyor(String address) {
    	String formattedAddress = address.replaceFirst("^0*", "");
    	return formattedAddress;
    }
}
