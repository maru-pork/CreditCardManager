package com.nnayram.ccmanager.core;

import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.text.DecimalFormat;

/**
 * Created by Rufo on 11/12/2016.
 */
public class NumberUtil {

    public static DecimalFormat format() {
        return new DecimalFormat("#,###,##0.00");
    }

    public static BigDecimal getBigDecimalOrThrow(float input) {
        if (input == 0)
            throw new IllegalArgumentException("Value is required");

        return setScale(new BigDecimal(input));
    }

    public static BigDecimal getBigDecimalIfExists(float input) {
        if (input == 0)
            return setScale(BigDecimal.ZERO);

        return setScale(new BigDecimal(input));
    }

    public static BigDecimal getBigDecimalIfExists(String input) {
        if (StringUtils.isEmpty(input))
            return setScale(BigDecimal.ZERO);

        return setScale(new BigDecimal(input));
    }

    public static BigDecimal getBigDecimalIfExists(BigDecimal input) {
        if (input == null)
            return setScale(BigDecimal.ZERO);

        return setScale(input);
    }

    public static Integer getIntegerIfExists(String input) {
        if (StringUtils.isEmpty(input))
            return 0;
        return  Integer.valueOf(input);
    }
    private static BigDecimal setScale(BigDecimal value) {
        if (value == null)
            return setScale(BigDecimal.ZERO);

        return value.setScale(2, BigDecimal.ROUND_HALF_UP);
    }

}
