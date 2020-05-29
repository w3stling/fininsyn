package com.apptastic.fininsyn.repurchase;

import com.apptastic.fininsyn.InstrumentLookup;
import com.apptastic.fininsyn.utils.TwitterUtil;
import com.apptastic.repurchase.Transaction;

import static com.apptastic.fininsyn.utils.NumberUtil.formatAmount;
import static com.apptastic.fininsyn.utils.NumberUtil.formatQuantityAtPrice;


public class RepurchaseTweet {

    public static String create(Transaction transaction) {
        StringBuilder builder = new StringBuilder();
        String type = toType(transaction.getType());

        if (type != null && transaction.getPrice().isPresent()) {
            builder.append(toCompany(transaction.getCompany()))
                   .append(" ")
                   .append(toType(transaction.getType()))
                   .append(" ")
                   .append("egna aktier för ")
                   .append(formatAmount(Math.abs(transaction.getValue()), "SEK"))
                   .append("\n\n")
                   .append(toStock(transaction.getCompany()))
                   .append("\n")
                   .append(formatQuantityAtPrice(Math.abs(transaction.getQuantity()), Math.abs(transaction.getPrice().get()), "SEK"))
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
}
