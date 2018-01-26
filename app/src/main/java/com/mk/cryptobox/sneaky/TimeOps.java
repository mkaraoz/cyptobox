package com.mk.cryptobox.sneaky;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by mk on 26.01.2018.
 */

public class TimeOps
{

    private static DateTimeFormatter dtf = DateTimeFormat.forPattern("yyyyMMdd_HHmmss");

    public static String print(DateTime date)
    {
        return dtf.print(date);
    }

    public static DateTime parse(String dateTime)
    {
        return dtf.parseDateTime(dateTime);
    }

    public static DateTime nowInLocalTime()
    {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String currentDateandTime = sdf.format(new Date());
        return dtf.parseDateTime(currentDateandTime);
    }
}
