package com.apptastic.fininsyn.pdmr;

import com.apptastic.fininsyn.InstrumentLookup;
import com.apptastic.fininsyn.utils.TwitterUtil;
import com.apptastic.insynsregistret.Transaction;
import org.apache.commons.lang3.StringUtils;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.*;
import java.util.stream.Stream;


public class PdmrTransactionTweet {
    private static final String EMOJI_BULL = "\uD83D\uDC02";
    private static final String EMOJI_BEAR = "\uD83D\uDC3B";
    private static final String EMOJI_WEARY_FACE = "\uD83D\uDE29";
    private static final String EMOJI_FACE_SCREAMING_IN_FEAR = "\uD83D\uDE31";
    private static final String EMOJI_STRONG = "\uD83D\uDCAA";
    private static final String EMOJI_MONEY_BAG = "\uD83D\uDCB0";
    private static final String EMOJI_MONEY_DOLLAR = "\uD83D\uDCB5";
    private static final DecimalFormat QUANTITY_FORMATTER = new DecimalFormat("#,###", new DecimalFormatSymbols(Locale.FRANCE));
    private static final DecimalFormat PRICE_FORMATTER = new DecimalFormat("#,##0.00", new DecimalFormatSymbols(Locale.FRANCE));
    private static final DecimalFormat AMOUNT_FORMATTER = new DecimalFormat("#,##0.00",  new DecimalFormatSymbols(Locale.FRANCE));
    private static final int TWITTER_MAX_LENGTH = 280;


    public static String create(List<Transaction> transactions) {
        Transaction transaction = transactions.get(0);

        double quantity = transactions.stream()
                .mapToDouble(PdmrTransactionFilter::toQuantity)
                .sum();

        double amount = transactions.stream()
                .mapToDouble(PdmrTransactionFilter::toAmount)
                .sum();

        InstrumentLookup.Instrument instrument = InstrumentLookup.getInstance().getInstrument(transaction.getIssuer(), transaction.getIsin(), transaction.getCurrency());

        String symbolName = null;
        String instrumentName = null;

        if (instrument != null) {
            symbolName = instrument.getSymbol();
            instrumentName = instrument.getName();
        }

        StringBuilder builder = new StringBuilder();

        builder.append(formatIssuer(transaction.getIssuer()))
                .append(" ")
                .append(formatPosition(transaction.getPosition().trim()));

        if (transaction.getPdmr().length() < 50) {
            builder.append(" ")
                    .append(formatPdmr(transaction.getPdmr().trim()));
        }

        builder.append(" har ")
                .append(formatNatureOfTransaction(transaction.getNatureOfTransaction()))
                .append(" aktier för ")
                .append(formatAmount(amount, transaction.getCurrency()))
                .append(" ")
                .append(formatEmoji(transaction, amount))
                .append("\n\n");


        if (instrumentName != null) {
            builder.append(instrumentName)
                    .append("\n");
        }
        else if (transaction.getInstrument().length() < 50 && !transaction.getInstrument().equalsIgnoreCase("aktie") &&
                !transaction.getInstrument().equalsIgnoreCase("Shares") &&
                !transaction.getInstrument().equalsIgnoreCase("Common Share")) {

            builder.append(transaction.getInstrument())
                    .append("\n");
        }

        builder.append(QUANTITY_FORMATTER.format((long)quantity) + " @ " + PRICE_FORMATTER.format(amount/quantity) + " " + transaction.getCurrency())
                .append("\n")
                .append(transaction.getTransactionDate().substring(0, 10))
                .append("\n");

        if (amount >= 85_000_000)
            builder.append("#insynshandel ");

        if (symbolName != null)
            builder.append(TwitterUtil.toCashTag(symbolName) + " #" + transaction.getIsin());
        else
            builder.append("#" + transaction.getIsin());

        String tweet = builder.toString();

        if (transaction.isAmendment()) {
            String update = "Rättelse: " + transaction.getDetailsOfAmendment() + "\n\n";

            if (tweet.length() + update.length() > TWITTER_MAX_LENGTH)
                update = "Uppdatering\n\n";

            tweet = update + tweet;
        }

        return tweet;
    }

    private static String formatIssuer(String issuer) {
        if ("Telefonaktiebolaget LM Ericsson".equals(issuer))
            issuer = "Ericsson";
        else if ("AB Electrolux".equals(issuer))
            issuer = "Electrolux";

        issuer = issuer.replaceFirst("\\(publ\\)", "");
        issuer = issuer.replaceFirst("\\(PUBL\\)", "");


        int index = indexesOf(issuer," ab", " a/s", " ltd", " bta", ",", ".");
        if (index != -1)
            issuer = issuer.substring(0, index);

        issuer = issuer.trim();

        if (issuer.length() > 2 && issuer.codePointAt(issuer.length()-1) == 'B' && issuer.codePointAt(issuer.length()-2) == ' ')
            issuer = issuer.substring(0, issuer.length()-2);

        Optional<String> formattedIssuer = Arrays.stream(StringUtils.split(issuer, ' '))
                .map(PdmrTransactionTweet::formatWordToCapitalize)
                .reduce((a, b) -> a + " " + b );

        issuer = formattedIssuer.orElse(issuer);

        if (issuer.length() > 2 && issuer.codePointAt(issuer.length()-1) != 's')
            issuer = issuer.concat("s");

        return issuer;
    }

    private static int indexesOf(String text, String... strings) {
        text = text.toLowerCase();

        OptionalInt minIndex = Stream.of(strings)
                .mapToInt(text::indexOf)
                .filter(s -> s >= 0)
                .min();

        return minIndex.orElse(-1);
    }

    private static String formatWordToLowerCase(String word) {
        if (word.length() > 3)
            word = word.toLowerCase();

        return word;
    }

    private static String formatWordToCapitalize(String word) {
        if (word.length() > 4 && (StringUtils.isAllUpperCase(word) || StringUtils.isAllLowerCase(word)))
            word = StringUtils.capitalize(word.toLowerCase());

        return word;
    }

    private static String formatNatureOfTransaction(String natureOfTransaction) {
        natureOfTransaction = natureOfTransaction.trim();

        if ("Förvärv".equals(natureOfTransaction))
            natureOfTransaction = "köpt";
        else if ("Avyttring".equals(natureOfTransaction))
            natureOfTransaction = "sålt";
        else
            natureOfTransaction = "???";

        return natureOfTransaction;
    }

    private static String formatPosition(String position) {
        if ("vd".equals(position))
            position = "VD";
        else if ("cfo".equals(position))
            position = "CFO";
        else if ("ceo".equals(position) || "cEO".equals(position))
            position = "CEO";

        int index = position.indexOf('/');

        if (index != -1 && position.length() > 2)
            position = position.substring(0, 1).toLowerCase() + position.substring(1, position.length());

        index = position.indexOf('(');

        if (index != -1 && position.length() > 50)
            position = position.substring(0, index);

        Optional<String> formattedPosition = Arrays.stream(StringUtils.split(position, ' '))
                .map(PdmrTransactionTweet::formatWordToLowerCase)
                .reduce((a, b) -> a + ' ' + b );

        position = formattedPosition.orElse(position);
        position = position.trim();

        if (position.codePointAt(position.length() - 1) == '.')
            position = position.substring(0, position.length() - 2);

        return position;
    }

    private static String formatPdmr(String pdmr) {
        if (pdmr.length() > 50)
            return "";

        Optional<String> formattedPdmr = Arrays.stream(StringUtils.split(pdmr, ' '))
                .map(String::toLowerCase)
                .map(StringUtils::capitalize)
                .reduce((a, b) -> a + ' ' + b );

        return formattedPdmr.orElse(pdmr);
    }

    private static String formatAmount(double amount, String currency) {
        String amountString;

        if (amount >= 100000.0)
            amountString = AMOUNT_FORMATTER.format(amount / 1000000.0) + " M" + currency;
        else if (amount >= 1000.0)
            amountString = AMOUNT_FORMATTER.format(amount / 1000.0) + " k" + currency;
        else
            amountString = AMOUNT_FORMATTER.format(amount) + ' ' + currency;

        return amountString;
    }

    private static String formatEmoji(Transaction transaction, double amount) {
        double amountInSek = PdmrTransactionFilter.amountInSek(amount, transaction.getCurrency());

        StringBuilder emojiBuilder = new StringBuilder();

        if ("Förvärv".equals(transaction.getNatureOfTransaction())) {

            if (amountInSek < 5_000_000.0)
                emojiBuilder.append(EMOJI_MONEY_DOLLAR);
            else if (amountInSek < 20_000_000.0)
                emojiBuilder.append(EMOJI_MONEY_DOLLAR + EMOJI_MONEY_DOLLAR);
            else if (amountInSek < 50_000_000.0)
                emojiBuilder.append(EMOJI_MONEY_BAG);
            else if (amountInSek < 1_000_000_000.0)
                emojiBuilder.append(EMOJI_MONEY_BAG + EMOJI_MONEY_BAG);
            else
                emojiBuilder.append(EMOJI_MONEY_BAG + EMOJI_MONEY_BAG + EMOJI_STRONG);
        }
        else if ("Avyttring".equals(transaction.getNatureOfTransaction())) {
            if (amountInSek > -50_000_000)
                emojiBuilder.append(EMOJI_BEAR);
            else if (amountInSek > -1_000_000_000.0)
                emojiBuilder.append(EMOJI_BEAR + EMOJI_WEARY_FACE);
            else
                emojiBuilder.append(EMOJI_BEAR + EMOJI_FACE_SCREAMING_IN_FEAR);
        }

        return emojiBuilder.toString();
    }
}