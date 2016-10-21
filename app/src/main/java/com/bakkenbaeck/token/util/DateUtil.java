package com.bakkenbaeck.token.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DateUtil {
    public static final long ONE_DAY = 86400000;

    public static String getDate(String format, Date d){
        Calendar date = Calendar.getInstance();
        Calendar today = Calendar.getInstance();
        date.setTime(d);

        if(date.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)
                && date.get(Calendar.YEAR) == today.get(Calendar.YEAR)) {
            return "Today";
        }else{
            SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.ENGLISH);
            String s = sdf.format(date.getTime());
            return s.substring(0,1).toUpperCase() + s.substring(1).toLowerCase();
        }
    }

    public static String getDate(String format, long d){
        Calendar date = Calendar.getInstance();
        Calendar today = Calendar.getInstance();
        date.setTimeInMillis(d);

        if(date.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)
                && date.get(Calendar.YEAR) == today.get(Calendar.YEAR)) {
            return "Today";
        }else{
            SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.ENGLISH);
            String s = sdf.format(date.getTime());
            return s.substring(0,1).toUpperCase() + s.substring(1).toLowerCase();
        }
    }
}
