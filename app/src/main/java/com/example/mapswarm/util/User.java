package com.example.mapswarm.util;

public class User {

    public static String getUsernameFromEmail(String str)
    {
        String finalOutput = "";
        String arrayOfStr[] = str.split("@");
        if (arrayOfStr.length == 2) {
            finalOutput = arrayOfStr[0];
        }
        return finalOutput;
    }
}
