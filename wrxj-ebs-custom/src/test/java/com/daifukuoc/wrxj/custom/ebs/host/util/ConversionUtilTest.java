package com.daifukuoc.wrxj.custom.ebs.host.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Calendar;
import java.util.Date;

import org.apache.logging.log4j.util.Strings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ConversionUtilTest {
    private static Calendar calendar = Calendar.getInstance();

    private static final int VALID_DATETIME_YEAR = 2022;
    private static final int VALID_DATETIME_MONTH = 12;
    private static final int VALID_DATETIME_DAY = 31;
    private static final int VALID_DATETIME_HOUR = 10;
    private static final int VALID_DATETIME_MINUTE = 11;
    private static final int VALID_DATETIME_SECOND = 22;

    private static final String INVALID_YEAR = "xx221231101122";
    private static final String INVALID_MONTH = "20229931101122";
    private static final String INVALID_DAY = "20221299101122";
    private static final String INVALID_HOUR = "20221231991122";
    private static final String INVALID_MINUTE = "20221231109922";
    private static final String INVALID_SECOND = "20221231101199";

    @Test
    void shouldConvertToDateIfValid() {
        LocalDateTime dateTimeToTest = LocalDateTime.of(VALID_DATETIME_YEAR, VALID_DATETIME_MONTH, VALID_DATETIME_DAY,
                VALID_DATETIME_HOUR, VALID_DATETIME_MINUTE, VALID_DATETIME_SECOND);
        String dateTimeStringToConvert = dateTimeToTest.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));

        Date converted = ConversionUtil.convertDateStringToDate(dateTimeStringToConvert);
        calendar.setTime(converted);
        
        assertEquals(VALID_DATETIME_YEAR, calendar.get(Calendar.YEAR));
        assertEquals(VALID_DATETIME_MONTH, calendar.get(Calendar.MONTH) + 1); // Calendar's Month starts from 0
        assertEquals(VALID_DATETIME_DAY, calendar.get(Calendar.DAY_OF_MONTH));
        assertEquals(VALID_DATETIME_HOUR, calendar.get(Calendar.HOUR_OF_DAY));
        assertEquals(VALID_DATETIME_MINUTE, calendar.get(Calendar.MINUTE));
        assertEquals(VALID_DATETIME_SECOND, calendar.get(Calendar.SECOND));
    }

    @Test
    void shouldThrowADateTimeParseExceptionIfInvalid() {

        assertThrows(DateTimeParseException.class,
                () -> ConversionUtil.convertDateStringToDate(INVALID_YEAR));

        assertThrows(DateTimeParseException.class,
                () -> ConversionUtil.convertDateStringToDate(INVALID_MONTH));

        assertThrows(DateTimeParseException.class,
                () -> ConversionUtil.convertDateStringToDate(INVALID_DAY));

        assertThrows(DateTimeParseException.class,
                () -> ConversionUtil.convertDateStringToDate(INVALID_HOUR));

        assertThrows(DateTimeParseException.class,
                () -> ConversionUtil.convertDateStringToDate(INVALID_MINUTE));

        assertThrows(DateTimeParseException.class,
                () -> ConversionUtil.convertDateStringToDate(INVALID_SECOND));
        
        assertThrows(DateTimeParseException.class,
                () -> ConversionUtil.convertDateStringToDate(Strings.EMPTY));
        
        assertThrows(NullPointerException.class,
                () -> ConversionUtil.convertDateStringToDate(null));        
    }
}
