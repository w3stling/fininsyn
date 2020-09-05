package com.apptastic.fininsyn.pdmr.mfsa;

import com.apptastic.fininsyn.InstrumentLookup;
import com.apptastic.fininsyn.utils.TwitterUtil;
import com.apptastic.mfsapdmr.Transaction;

import static com.apptastic.fininsyn.utils.NumberUtil.formatAmount;
import static com.apptastic.fininsyn.utils.NumberUtil.formatQuantityAtPrice;


public class PdmrMfsaTransactionTweet {

    public static String create(Transaction transaction) {
        StringBuilder builder = new StringBuilder();

        if (transaction.isCloselyAssociated()) {
            builder.append("Närstående till ");
        }

        String currency = toCurrency(transaction);

        builder.append(toCompany(transaction))
               .append(" ")
               .append(toRole(transaction))
               .append(transaction.getPdmr())
               .append(" ")
               .append(toNatureOfTransaction(transaction))
               .append(" ")
               .append(formatInstrumentType(transaction))
               .append(" för ")
               .append(formatAmount(transaction.getPrice() * transaction.getVolume(), currency))
               .append(" ")
               .append(formatEmoji(transaction))
               .append("\n\n")
               .append(toStock(transaction))
               .append("\n")
               .append(formatQuantityAtPrice(transaction.getVolume(), transaction.getPrice(), currency))
               .append("\n")
               .append(transaction.getDate());

        String stock = toStock(transaction);
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

    private static String formatInstrumentType(Transaction transaction) {
        String instrumentType = transaction.getInstrumentType();
        if (instrumentType == null) {
            return "aktier";
        }

        instrumentType = instrumentType.toLowerCase();

        if ("equity".equals(instrumentType) || "units".equals(instrumentType) || "unit subscription rights".equals(instrumentType)) {
            instrumentType = "aktier";
        }
        else if ("options".equals(instrumentType)) {
            instrumentType = "optioner";
        }
        else if ("bonds".equals(instrumentType)) {
            instrumentType = "obligationer";
        }
        else if ("warrants".equals(instrumentType)) {
            instrumentType = "warranter";
        }
        else {
            instrumentType = "aktier";
        }

        return instrumentType;
    }

    private static String toCompany(Transaction transaction) {
        String issuer = transaction.getIssuer();

        if (issuer.toLowerCase().endsWith(" plc")) {
            issuer = issuer.substring(0, issuer.length()-4);
        }

        return issuer.trim() + "s";
    }

    private static String toRole(Transaction transaction) {
        return  transaction.getRole()
                           .map(r -> r + " ")
                           .orElse("");
    }

    private static String toStock(Transaction transaction) {
        String issuer = transaction.getIssuer();

        if (issuer.toLowerCase().endsWith(" plc")) {
            issuer = issuer.substring(0, issuer.length()-4);
        }

        return issuer.trim();
    }

    private static String toNatureOfTransaction(Transaction transaction) {
        String typeOfTransaction;
        if (transaction.getNatureOfTransaction().equalsIgnoreCase("Buy") ||
            transaction.getNatureOfTransaction().equalsIgnoreCase("Purchase") ||
            transaction.getNatureOfTransaction().equalsIgnoreCase("Bought")) {

            typeOfTransaction = "köper";
        }
        else if (transaction.getNatureOfTransaction().equalsIgnoreCase("Sell") ||
                transaction.getNatureOfTransaction().equalsIgnoreCase("Sold")) {

            typeOfTransaction = "säljer";
        }
        else {
            typeOfTransaction = "?";
        }

        return typeOfTransaction;
    }

    private static String toCurrency(Transaction transaction) {
        if (transaction.getCurrency().toLowerCase().contains("swedish") ||
            transaction.getCurrency().toLowerCase().contains("krona")) {

            return "SEK";
        }
        else if (transaction.getCurrency().toLowerCase().contains("euro")) {
            return "EUR";
        }

        return transaction.getCurrency();
    }

    private static String formatEmoji(Transaction transaction) {
        double amount = transaction.getVolume() * transaction.getPrice();
        String currency = toCurrency(transaction);
        String natureOfTransaction = toNatureOfTransaction(transaction);
        boolean isBuy = "köper".equals(natureOfTransaction);
        boolean isSell = "säljer".equals(natureOfTransaction);
        return TwitterUtil.formatEmoji(amount, currency, isBuy, isSell);
    }
}
