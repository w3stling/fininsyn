package com.apptastic.fininsyn.utils;

public class CurrencyConverter {
    private static final double EXCHANGE_RATE_EUR_SEK = 10.28;
    private static final double EXCHANGE_RATE_USD_SEK = 8.60;
    private static final double EXCHANGE_RATE_CAD_SEK = 6.70;
    private static final double EXCHANGE_RATE_DKK_SEK = 1.38;
    private static final double EXCHANGE_RATE_NOK_SEK = 1.07;

    public static double amountInSek(double amount, String currency) {
        if ("EUR".equals(currency))
            return amount * EXCHANGE_RATE_EUR_SEK;
        else if ("USD".equals(currency))
            return amount * EXCHANGE_RATE_USD_SEK;
        else if ("CAD".equals(currency))
            return amount * EXCHANGE_RATE_CAD_SEK;
        else if ("DKK".equals(currency))
            return amount * EXCHANGE_RATE_DKK_SEK;
        else if ("NOK".equals(currency))
            return amount * EXCHANGE_RATE_NOK_SEK;
        else
            return amount;
    }

}
