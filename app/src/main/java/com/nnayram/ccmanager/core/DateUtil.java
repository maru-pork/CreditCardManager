package com.nnayram.ccmanager.core;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;

import java.util.Date;

/**
 * Created by Rufo on 11/12/2016.
 */
public class DateUtil {

    public final static String DATE_PATTERN = "d-MMM-yy";

    public static Date getDateIfExists(Long dateInLong) {
        if (dateInLong == null || dateInLong == 0)
            return null;

        return new Date(dateInLong);
    }

    public static Date getDateOrThrow(Long dateInLong) {
        if (dateInLong == null || dateInLong == 0)
            throw  new IllegalArgumentException("Value(dateInLong) is required.");

        return getDateIfExists(dateInLong);
    }

    public static LocalDate convertStringToLocalDate(String date, String pattern) {
        if (StringUtils.isEmpty(date))
            return null;
        return LocalDate.parse(date,
                DateTimeFormat.forPattern(pattern));
    }

    public static LocalDate convertStringToLocalDate(String date) {
        return convertStringToLocalDate(date, DATE_PATTERN);
    }

    public static Date convertString(String date, String pattern) {
        LocalDate localDate = convertStringToLocalDate(date, pattern);
        return localDate == null ? null : localDate.toDate();
    }

    public static Date convertString(String date) {
        return convertString(date, DATE_PATTERN);
    }
}
