package com.apptastic.fininsyn.repurchase;

import com.apptastic.fininsyn.InstrumentLookup;
import com.apptastic.fininsyn.utils.TwitterUtil;
import com.apptastic.repurchase.Transaction;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class RepurchaseTweet {
    private static final DecimalFormat QUANTITY_FORMATTER = new DecimalFormat("#,###", new DecimalFormatSymbols(Locale.FRANCE));
    private static final DecimalFormat PRICE_FORMATTER = new DecimalFormat("#,##0.00", new DecimalFormatSymbols(Locale.FRANCE));
    private static final DecimalFormat AMOUNT_FORMATTER = new DecimalFormat("#,##0.00",  new DecimalFormatSymbols(Locale.FRANCE));

    public static String create(Transaction transaction) {
        StringBuilder builder = new StringBuilder();
        String type = toType(transaction.getType());

        if (type != null && transaction.getPrice().isPresent()) {
            builder.append(toCompany(transaction.getCompany()))
                   .append(" ")
                   .append(toType(transaction.getType()))
                   .append(" ")
                   .append("egna aktier för ")
                   .append(formatAmount(transaction.getValue(), "SEK"))
                   .append("\n\n")
                   .append(toStock(transaction.getCompany()))
                   .append("\n")
                   .append(QUANTITY_FORMATTER.format(Math.abs(transaction.getQuantity())) + " @ " + PRICE_FORMATTER.format(Math.abs(transaction.getPrice().get())) + " SEK")
                   .append("\n")
                   .append(transaction.getDate());
        } else {
            return "";
        }

        String stock = toStock(transaction.getCompany());
        InstrumentLookup.Instrument instrument = InstrumentLookup.getInstance().getInstrument(stock);
        if (instrument != null) {
            builder.append("\n")
                   .append(TwitterUtil.toCashTag(instrument.getSymbol()));

            if (instrument.getIsin() != null && !instrument.getIsin().isEmpty()) {
                builder.append(" #")
                       .append(instrument.getIsin().trim());
            }
        }

        return builder.toString();
    }

    private static String toType(String text) {
        if ("Repurchase".equalsIgnoreCase(text)) {
            return "återköper";
        }
        else if ("Sell".equalsIgnoreCase(text)) {
            return "säljer";
        }

        return null;
    }

    public static String toCompany(String text) {
        if (text.endsWith(" AB ser. A") || text.endsWith(" AB ser. B") || text.endsWith(" AB ser. C")) {
            text = text.substring(0, text.length()-10);
        }
        else if (text.endsWith(" A") || text.endsWith(" B") || text.endsWith(" C")) {
            text = text.substring(0, text.length()-2);
        }

        int pos = text.lastIndexOf(',');
        if (pos != -1) {
            text = text.substring(0, pos);
        }

        return text.trim();
    }

    public static String toStock(String text) {
        text = text.replaceAll(" AB ser. ", " ");

        int pos = text.lastIndexOf(',');
        if (pos != -1) {
            text = text.substring(0, pos);
        }

        return text.trim();
    }

    private static String formatAmount(double amount, String currency) {
        String amountString;

        if (Math.abs(amount) >= 100000.0)
            amountString = AMOUNT_FORMATTER.format(Math.abs(amount) / 1000000.0) + " M" + currency;
        else if (Math.abs(amount) >= 1000.0)
            amountString = AMOUNT_FORMATTER.format(Math.abs(amount) / 1000.0) + " k" + currency;
        else
            amountString = AMOUNT_FORMATTER.format(Math.abs(amount)) + ' ' + currency;

        return amountString;
    }
}
