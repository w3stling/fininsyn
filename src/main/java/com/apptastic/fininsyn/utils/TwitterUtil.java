package com.apptastic.fininsyn.utils;

public class TwitterUtil {
    private static final String EMOJI_BEAR = "\uD83D\uDC3B";
    private static final String EMOJI_MONEY_BAG = "\uD83D\uDCB0";
    private static final String EMOJI_MONEY_DOLLAR = "\uD83D\uDCB5";

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

    public static String formatEmoji(double amount, String currency, boolean isBuy, boolean isSell) {
        double amountInSek = CurrencyConverter.amountInSek(amount, currency);

        StringBuilder emojiBuilder = new StringBuilder();

        if (isBuy) {
            if (amountInSek < 5_000_000.0)
                emojiBuilder.append(EMOJI_MONEY_DOLLAR);
            else if (amountInSek < 20_000_000.0)
                emojiBuilder.append(EMOJI_MONEY_DOLLAR + EMOJI_MONEY_DOLLAR);
            else if (amountInSek < 50_000_000.0)
                emojiBuilder.append(EMOJI_MONEY_BAG);
            else if (amountInSek < 1_000_000_000.0)
                emojiBuilder.append(EMOJI_MONEY_BAG + EMOJI_MONEY_BAG);
            else
                emojiBuilder.append(EMOJI_MONEY_BAG + EMOJI_MONEY_BAG + EMOJI_MONEY_BAG);
        }
        else if (isSell) {
            if (amountInSek < 50_000_000)
                emojiBuilder.append(EMOJI_BEAR);
            else if (amountInSek < 1_000_000_000.0)
                emojiBuilder.append(EMOJI_BEAR + EMOJI_BEAR);
            else
                emojiBuilder.append(EMOJI_BEAR + EMOJI_BEAR + EMOJI_BEAR);
        }

        return emojiBuilder.toString();
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
