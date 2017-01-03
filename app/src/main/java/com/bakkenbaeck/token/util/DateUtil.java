package com.bakkenbaeck.token.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class DateUtil {
    public static String getDate(final String format, final long d){
        Calendar date = Calendar.getInstance();
        Calendar today = Calendar.getInstance();
        date.setTimeInMillis(d);

        if(date.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)
                && date.get(Calendar.YEAR) == today.get(Calendar.YEAR)) {
            return "Today";
        }else{
            SimpleDateFormat sdf = new SimpleDateFormat(format, LocaleUtil.getLocale());
            String s = sdf.format(date.getTime());
            return s.substring(0,1).toUpperCase() + s.substring(1).toLowerCase();
        }
    }
}
