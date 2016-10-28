package com.bakkenbaeck.token.util;

public class MessageUtil {

    public static String parseString(final String s){
        if(s != null){
            return s.replace("\\n", System.getProperty("line.separator"));
        }else{
            return "";
        }

    }
}
