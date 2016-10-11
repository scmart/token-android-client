package com.bakkenbaeck.toshi.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateUtil {

    public static String getDate(String format, Date date){
        SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.getDefault());
        String s = sdf.format(date);
        String ss = s.substring(0,1).toUpperCase() + s.substring(1).toLowerCase();
        return ss;
    }
}
