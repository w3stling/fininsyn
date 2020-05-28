package com.apptastic.fininsyn.utils;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class NumberUtil {
    private static final DecimalFormat QUANTITY_FORMATTER = new DecimalFormat("#,###", new DecimalFormatSymbols(Locale.FRANCE));
    private static final DecimalFormat PRICE_FORMATTER = new DecimalFormat("#,##0.00", new DecimalFormatSymbols(Locale.FRANCE));
    private static final DecimalFormat AMOUNT_FORMATTER = new DecimalFormat("#,##0.00",  new DecimalFormatSymbols(Locale.FRANCE));

    public static String formatQuantity(double quantity) {
        return QUANTITY_FORMATTER.format((long)quantity);
    }

    public static String formatPrice(double amount, double quantity) {
        return PRICE_FORMATTER.format(amount/quantity);
    }

    public static String formatPrice(double price) {
        return PRICE_FORMATTER.format(price);
    }

    public static String formatQuantityAtPrice(double quantity, double price, String currency) {
        return formatQuantity(quantity) + " @ " + formatPrice(price) + " " + currency;
    }

    public static String formatAmount(double amount, String currency) {
        String amountString;

        if (amount >= 1000000.0)
            amountString = AMOUNT_FORMATTER.format(amount / 1000000.0) + " M" + currency;
        else if (amount >= 10000.0)
            amountString = AMOUNT_FORMATTER.format(amount / 1000.0) + " k" + currency;
        else
            amountString = AMOUNT_FORMATTER.format(amount) + ' ' + currency;

        return amountString;
    }
}
