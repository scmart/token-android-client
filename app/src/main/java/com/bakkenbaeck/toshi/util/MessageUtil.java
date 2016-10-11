package com.bakkenbaeck.toshi.util;

import java.util.Random;

public class MessageUtil {
    private static final String[] videoMessages = new String[]{
            "Great! Here you go!",
            "You’re getting the hang of it! Here’s your earnings after watching that video."
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
        return s.replace("\\n", System.getProperty("line.separator"));
    }
}
