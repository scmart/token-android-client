package com.bakkenbaeck.token.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class DateUtil {
    public static String getDate(final String format, final long d){
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

    // Returns true if both timestamps are from the same day
    // Returns false if both timestamps are from different days
    public static boolean areSameDay(final long t1, final long t2) {
        final Calendar d1 = Calendar.getInstance(LocaleUtil.getLocale());
        final Calendar d2 = Calendar.getInstance(LocaleUtil.getLocale());
        d1.setTimeInMillis(t1);
        d2.setTimeInMillis(t2);

        return    d1.get(Calendar.YEAR) == d2.get(Calendar.YEAR)
               && d1.get(Calendar.DAY_OF_YEAR) == d2.get(Calendar.DAY_OF_YEAR);
    }
}
