package com.apptastic.fininsyn.utils;

public class TwitterUtil {
    public static final int TWEET_MAX_LENGTH = 280;

    public static String toCashTag(String text) {
        String cashTag;

        if (text == null || text.isEmpty())
            cashTag = "";
        else if (text.matches(".*\\d+.*"))
            cashTag = "#" + text;
        else
            cashTag = "$" + text;

        return cashTag;
    }


    public static String trim(String tweet) {
        return trim(tweet, 0);
    }

    public static String trim(String tweet, int length) {
        if (tweet.length() > (TWEET_MAX_LENGTH - length)) {
            int pos1 = tweet.lastIndexOf(' ', TWEET_MAX_LENGTH - length);
            int pos2 = tweet.lastIndexOf('\n', TWEET_MAX_LENGTH - length);
            int pos = Math.max(pos1, pos2);
            tweet = tweet.substring(0, pos);
        }

        return tweet.trim();
    }
}
