package com.apptastic.fininsyn.shortselling;

import com.apptastic.blankningsregistret.NetShortPosition;
import com.apptastic.fininsyn.InstrumentLookup;
import com.apptastic.fininsyn.utils.TwitterUtil;
import org.apache.commons.lang3.StringUtils;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;


public class ShortSellingTweet {
    private static final String EMOJI_SMILING = "\uD83D\uDE07";
    private static final String EMOJI_GHOST = "\uD83D\uDC7B";
    private static final String EMOJI_THUMBS_UP = "\uD83D\uDC4D";
    private static final DecimalFormat PROCENT_FORMATTER = new DecimalFormat("#,##0.00", new DecimalFormatSymbols(Locale.FRANCE));
    private static final Pattern TRIM = Pattern.compile("(^\\h*)|(\\h*$)");

    public static String create(List<NetShortPosition> positionHistoryPerHolder) {
        NetShortPosition currentPosition = positionHistoryPerHolder.get(0);

        boolean increased = increasePosition(positionHistoryPerHolder);
        String directionText = increased ? "ökar" : "minskar";

        StringBuilder builder = new StringBuilder();
        builder.append(formatPositionHolder(currentPosition.getPositionHolder()))
                .append(" ")
                .append(directionText)
                .append(" sin korta nettoposition");

        if (positionHistoryPerHolder.size() >= 2 &&
                currentPosition.getPositionInPercent() != positionHistoryPerHolder.get(1).getPositionInPercent() &&
                positionHistoryPerHolder.get(1).getPositionInPercent() > 0.5) {

            NetShortPosition previousPosition = positionHistoryPerHolder.get(1);

            builder.append(" från ")
                    .append(PROCENT_FORMATTER.format(previousPosition.getPositionInPercent()))
                    .append("% till ");
        }
        else {
            builder.append(" till ");
        }

        builder.append(PROCENT_FORMATTER.format(currentPosition.getPositionInPercent()))
                .append("% i ")
                .append(formatIssuer(currentPosition.getIssuer()))
                .append(" ")
                .append(getEmoji(increased, currentPosition.getPositionInPercent()))
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

    private static boolean increasePosition(List<NetShortPosition> positionHistory) {
        if (positionHistory.isEmpty()) {
            return false;
        }
        else if (positionHistory.size() == 1) {
            return positionHistory.get(0).getPositionInPercent() >= 0.5;
        }
        else {
            if (positionHistory.get(1).getPositionInPercent() < 0.5)
                return positionHistory.get(0).getPositionInPercent() >= 0.5;
            else
                return positionHistory.get(0).getPositionInPercent() > positionHistory.get(1).getPositionInPercent();
        }
    }

    private static String getEmoji(boolean increasePosition, double currentPosition) {
        String emoji = (increasePosition) ? EMOJI_GHOST : EMOJI_SMILING;

        if (currentPosition < 0.5)
            emoji += EMOJI_THUMBS_UP;

        return emoji;
    }

    private static String formatIssuer(String issuer) {
        if (issuer.equals("AB SKF"))
            issuer = "SKF";
        else if (issuer.equals("BillerudKorsnas publ AB"))
            issuer = "BillerudKorsnas";

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
