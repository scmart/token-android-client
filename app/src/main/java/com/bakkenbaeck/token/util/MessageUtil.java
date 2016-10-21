package com.bakkenbaeck.token.util;

import java.util.Random;

public class MessageUtil {
    private static final String[] videoMessages = new String[]{
            "Great! Here you go!"
    };

    public static String[] getVideoMessages(){
        return videoMessages;
    }

    public static String getWelcomeMessage(){
        return "Welcome to Toshi! Toshi allows you to earn and send Ethereum. Get started by watching the video below.";
    }

    public static String getRandomMessage(){
        Random r = new Random();
        int ranNur = r.nextInt(2);
        return videoMessages[ranNur];
    }

    public static String parseString(String s){
        if(s != null){
            return s.replace("\\n", System.getProperty("line.separator"));
        }else{
            return "";
        }

    }
}
