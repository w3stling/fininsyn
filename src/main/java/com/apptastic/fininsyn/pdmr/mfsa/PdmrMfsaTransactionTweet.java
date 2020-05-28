package com.apptastic.fininsyn.pdmr.mfsa;

import com.apptastic.fininsyn.InstrumentLookup;
import com.apptastic.fininsyn.pdmr.fi.PdmrTransactionFilter;
import com.apptastic.fininsyn.utils.TwitterUtil;
import com.apptastic.mfsapdmr.Transaction;

import static com.apptastic.fininsyn.utils.NumberUtil.formatAmount;
import static com.apptastic.fininsyn.utils.NumberUtil.formatQuantityAtPrice;


public class PdmrMfsaTransactionTweet {
    private static final String EMOJI_BEAR = "\uD83D\uDC3B";
    private static final String EMOJI_MONEY_BAG = "\uD83D\uDCB0";
    private static final String EMOJI_MONEY_DOLLAR = "\uD83D\uDCB5";

    public static String create(Transaction transaction) {
        StringBuilder builder = new StringBuilder();

        if (transaction.isCloselyAssociated()) {
            builder.append("Närstående till ");
        }

        builder.append(toCompany(transaction))
               .append(" ")
               .append(toRole(transaction))
               .append(transaction.getPdmr())
               .append(" ")
               .append(toNatureOfTransaction(transaction))
               .append(" aktier för ")
               .append(formatAmount(transaction.getPrice() * transaction.getVolume(), toCurrency(transaction)))
               .append(" ")
               .append(formatEmoji(transaction))
               .append("\n\n")
               .append(toStock(transaction))
               .append("\n")
               .append(formatQuantityAtPrice(transaction.getVolume(), transaction.getPrice(), transaction.getCurrency()))
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
        double amountInSek = PdmrTransactionFilter.amountInSek(amount, toCurrency(transaction));

        StringBuilder emojiBuilder = new StringBuilder();
        String natureOfTransaction = toNatureOfTransaction(transaction);

        if ("köper".equals(natureOfTransaction)) {
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
        else if ("säljer".equals(natureOfTransaction)) {
            if (amountInSek < 50_000_000)
                emojiBuilder.append(EMOJI_BEAR);
            else if (amountInSek < 1_000_000_000.0)
                emojiBuilder.append(EMOJI_BEAR + EMOJI_BEAR);
            else
                emojiBuilder.append(EMOJI_BEAR + EMOJI_BEAR + EMOJI_BEAR);
        }

        return emojiBuilder.toString();
    }
}
