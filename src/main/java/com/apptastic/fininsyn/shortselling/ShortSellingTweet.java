package com.apptastic.fininsyn.shortselling;

import com.apptastic.blankningsregistret.NetShortPosition;
import com.apptastic.fininsyn.InstrumentLookup;
import com.apptastic.fininsyn.utils.TwitterUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Triple;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;


public class ShortSellingTweet {
    private static final String EMOJI_NEW = "\uD83C\uDD95";
    private static final String EMOJI_WHITE_FLAG = "\uD83C\uDFF3️";
    private static final String EMOJI_SMILING = "\uD83D\uDE07";
    private static final String EMOJI_GHOST = "\uD83D\uDC7B";
    private static final DecimalFormat PROCENT_FORMATTER = new DecimalFormat("#,##0.00", new DecimalFormatSymbols(Locale.FRANCE));
    private static final Pattern TRIM = Pattern.compile("(^\\h*)|(\\h*$)");

    public static String create(Triple<NetShortPosition, NetShortPosition, Integer> positionPair) {
        if (positionPair == null || positionPair.getLeft() == null) {
            return "";
        }

        NetShortPosition currentPosition = positionPair.getLeft();
        NetShortPosition previousPosition = positionPair.getMiddle();
        boolean newPositionHolder = newPositionHolder(positionPair);
        boolean increased = increasePosition(positionPair);
        boolean isNewPosition = newPosition(positionPair);

        StringBuilder builder = new StringBuilder();

        if (newPositionHolder) {
            builder.append("Ny blankare ");
        }

        if (isNewPosition) {
            builder.append(formatPositionHolder(currentPosition.getPositionHolder()))
                   .append(" tar en signifikant kort nettoposition på ");
        }
        else {
            String directionText = increased ? "ökar" : "minskar";

            builder.append(formatPositionHolder(currentPosition.getPositionHolder()))
                    .append(" ")
                    .append(directionText)
                    .append(" sin korta nettoposition");

            if (previousPosition != null &&
                    currentPosition.getPositionInPercent() != previousPosition.getPositionInPercent() &&
                    currentPosition.isSignificantPosition()) {

                String change = PROCENT_FORMATTER.format(currentPosition.getPositionInPercent() - previousPosition.getPositionInPercent());

                builder.append(" med ")
                        .append(change)
                        .append(" procentenheter till ");
            } else if (!currentPosition.isSignificantPosition()) {
                builder.append(" till under ");
            } else {
                builder.append(" till ");
            }
        }

        builder.append(PROCENT_FORMATTER.format(currentPosition.getPositionInPercent()))
                .append("% i ")
                .append(formatIssuer(currentPosition.getIssuer()))
                .append(" ")
                .append(getEmoji(isNewPosition, currentPosition.isSignificantPosition(), increased))
                .append("\n")
                .append(currentPosition.getPositionDate().format(DateTimeFormatter.ISO_LOCAL_DATE))
                .append("\n");

        String symbolName = null;
        String isin = currentPosition.getIsin().trim();
        InstrumentLookup.Instrument instrument = InstrumentLookup.getInstance().getInstrument("", isin, "");

        if (instrument != null)
            symbolName = instrument.getSymbol();

        if (currentPosition.getPositionInPercent() >= 2.9)
            builder.append("#blankning ");

        if (symbolName != null)
            builder.append(TwitterUtil.toCashTag(symbolName))
                   .append(" #")
                   .append(isin);
        else
            builder.append("#")
                   .append(isin);

        return builder.toString();
    }


    private static boolean newPositionHolder(Triple<NetShortPosition, NetShortPosition, Integer> positionPair) {
        return Optional.ofNullable(positionPair.getRight()).orElse(-1).longValue() == 1;
    }

    private static boolean increasePosition(Triple<NetShortPosition, NetShortPosition, Integer> positionPair) {
        NetShortPosition currentPosition = positionPair.getLeft();
        NetShortPosition previousPosition = positionPair.getMiddle();

        if (previousPosition == null) {
            return currentPosition.getPositionInPercent() >= 0.5;
        }
        else if (currentPosition.getPositionInPercent() == previousPosition.getPositionInPercent()) {
            double currentPositionInPercent = currentPosition.getPositionInPercent();
            if (!currentPosition.isSignificantPosition()) {
                currentPositionInPercent -= 0.01;
            }

            double previousPositionInPercent = previousPosition.getPositionInPercent();
            if (!previousPosition.isSignificantPosition()) {
                previousPositionInPercent -= 0.01;
            }

            return currentPositionInPercent >= previousPositionInPercent;
        }
        else {
            return currentPosition.getPositionInPercent() >= previousPosition.getPositionInPercent();
        }
    }

    private static boolean newPosition(Triple<NetShortPosition, NetShortPosition, Integer> positionPair) {
        return positionPair.getLeft() != null && positionPair.getMiddle() == null;
    }

    private static String getEmoji(boolean newPosition, boolean significantPosition, boolean increasePosition) {
        String emoji = increasePosition ? EMOJI_GHOST : EMOJI_SMILING;

        if (newPosition) {
            emoji += EMOJI_NEW;
        }
        else if (!significantPosition && !increasePosition) {
            emoji += EMOJI_WHITE_FLAG;
        }

        return emoji;
    }

    private static String formatIssuer(String issuer) {
        if (issuer.length() > 0 && issuer.endsWith(".")) {
            issuer = issuer.substring(0, issuer.length()-1).trim();
        }

        if ("SSAB AB".equals(issuer))
            return "SSAB";
        else if ("SAAB AKTIEBOLAG".equals(issuer))
            return "SAAB";
        else if ("AB SKF".equals(issuer))
            return "SKF";
        else if ("BillerudKorsnas publ AB".equals(issuer))
            return "BillerudKorsnas";
        else if ("H & M HENNES & MAURITZ AB".equals(issuer)) {
            return "Hennes & Mauritz";
        }

        issuer = issuer.replaceFirst("TELE2", "Tele2");
        issuer = issuer.replaceFirst("\\(PUBL\\)", "");
        issuer = issuer.replaceFirst("\\(publ\\)", "");
        issuer = issuer.replaceFirst("\\(Publ\\)", "");
        issuer = issuer.replaceFirst("Aktiebolag", "");
        issuer = issuer.replaceFirst("AKTIEBOLAG", "");
        issuer = issuer.replaceFirst("\\h+AB\\h*$", "");
        issuer = trim(issuer, "");

        Optional<String> formattedIssuer = Arrays.stream(StringUtils.split(issuer, ' '))
                .map(ShortSellingTweet::formatWordToCapitalize3)
                .reduce((a, b) -> a.trim() + " " + b.trim() );

        issuer = formattedIssuer.orElse(issuer).trim();

        return issuer;
    }

    private static String formatPositionHolder(String name) {
        name = name.replace(",", "");
        name = name.replace(".", "");

        Optional<String> formattedIssuer = Arrays.stream(StringUtils.split(name, ' '))
                .map(ShortSellingTweet::formatWordToCapitalize3)
                .reduce((a, b) -> a.trim() + " " + b.trim() );

        name = formattedIssuer.orElse(name).trim();

        return name;
    }


    private static String formatWordToCapitalize3(String word) {
        return formatWordToCapitalize(word, 3);
    }

    private static String formatWordToCapitalize(String word, int nofChars) {
        word = trim(word, "");

        if (word.length() > nofChars && (StringUtils.isAllUpperCase(word) || StringUtils.isAllLowerCase(word)))
            word = StringUtils.capitalize(word.toLowerCase());

        return word;
    }


    private static String trim(String text, String replacement) {
        return TRIM.matcher(text).replaceAll(replacement);
    }
}
