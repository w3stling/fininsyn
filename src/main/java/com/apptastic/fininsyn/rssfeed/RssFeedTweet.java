package com.apptastic.fininsyn.rssfeed;

import com.apptastic.fininsyn.bitly.BitlyClient;
import com.apptastic.fininsyn.utils.TextUtil;
import com.apptastic.fininsyn.utils.TwitterUtil;
import com.apptastic.rssreader.Item;

import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;


public class RssFeedTweet {
    private static final String BITLY_ACCESSTOKEN = "707fb3170e622ee0c650c02fe09151cc2e012515";
    private static final String EMOJI_BANK = "\uD83C\uDFE6";
    private static final String EMOJI_BAR_CHART = "\uD83D\uDCCA";
    private static final String EMOJI_CHART_INCREASING = "\uD83D\uDCC8";
    private static final String EMOJI_CHART_DECREASING = "\uD83D\uDCC9";
    private static final String EMOJI_POLICE_OFFICER = "\uD83D\uDC6E";
    private static final String EMOJI_OFFICE_BUILDING = "\uD83C\uDFE2";
    private static final String EMOJI_BRIEFCASE = "\uD83D\uDCBC";
    private static final String EMOJI_NEWSPAPER = "\uD83D\uDCF0";
    private static final String EMOJI_NEWSPAPER_ROLLED_UP = "\uD83D\uDDDE️";
    private static final String EMOJI_LIGHT_BULB = "\uD83D\uDCA1";
    private static final String EMOJI_FACE_WITH_MONOCLE = "\uD83E\uDDD0";
    private static final String EMOJI_LOUDSPEAKER = "\uD83D\uDCE2";
    private static final String EMOJI_THUMBS_UP = "\uD83D\uDC4D";
    private static final String EMOJI_THUMBS_DOWN = "\uD83D\uDC4E";
    private static final String EMOJI_POLICE_CAR_LIGHT = "\uF09F\u9AA8";

    public static String createRiskbankenTweet(Item item) {
        String title = item.getTitle().orElse("").trim();
        String url = toShortUrl(item.getLink().orElse(""));
        return "Riksbanken " + EMOJI_BANK + " " + title + "\n" + "#riksbanken" + "\n\n" + url;
    }

    public static String createFinanspolitiskaradetTweet(Item item) {
        String title = item.getTitle().orElse("").trim();
        String url = toShortUrl(item.getLink().orElse(""));
        return "Finanspolitiskaradet " + EMOJI_BRIEFCASE + " " + title + "\n" + url + "\n\n" + "#finanspolitiskaradet";
    }

    public static String createKonjunkturinstitutetTweet(Item item) {
        String title = item.getTitle().orElse("").trim();
        String url = toShortUrl(item.getLink().orElse(""));
        return "Konjunkturinstitutet " + EMOJI_OFFICE_BUILDING + " " + title + "\n" + url + "\n\n" + "#konjunkturinstitutet";
    }

    public static String createScbTweet(Item item) {
        String title = item.getTitle().orElse("").trim();
        String url = toShortUrl(item.getLink().orElse(""));
        return "SCB " + EMOJI_BAR_CHART + " " + title + "\n" + "#SCB" + "\n\n" + url;
    }

    public static String createEkobrottsmyndighetenTweet(Item item) {
        String title = item.getTitle().orElse("").trim();
        String url = toShortUrl(item.getLink().orElse(""));
        return "Ekobrottsmyndigheten " + EMOJI_POLICE_OFFICER + " " + title + "\n" + url + "\n\n" + "#EBM";
    }

    public static String createVeckansAffarerTweet(Item item) {
        String title = item.getTitle().orElse("").trim();
        String description = item.getDescription().orElse("").trim();
        String url = toShortUrl(item.getLink().orElse(""));
        String tweet = "Veckans Affärer " + EMOJI_NEWSPAPER + " " + title + " " + generalTitleSuffix(title, description) + "\n" + url;
        return TwitterUtil.trim(tweet);
    }

    public static String createRealtidTweet(Item item) {
        String url = toShortUrl(item.getLink().orElse(""));
        String title = xmlEscape(item.getTitle().orElse("")).trim();
        String description = item.getDescription().orElse("").trim();
        title = removeQuotes(title);


        List<String> symbols = Collections.emptyList();

/*
        try {
            symbols = RealtidUtil.getTickerSymbols(item.getLink());
        }
        catch (Exception e) {
            symbols = Collections.emptyList();
        }
*/

        String tickerSymbols = toSymbols(symbols);

        String tweet = "Realtid " + EMOJI_NEWSPAPER + " " + title + " " + generalTitleSuffix(title, description) + "\n" + tickerSymbols + "\n" + url;
        return TwitterUtil.trim(tweet);
    }

    public static String createPlaceraTweet(Item item) {
        String emoji;
        String tickerSymbols;
        String titleLowerCase = item.getTitle().orElse("").toLowerCase().trim();

        if (titleLowerCase.startsWith("börsen idag:") || titleLowerCase.startsWith("börsen:")) {
            if (titleLowerCase.contains("stark") || titleLowerCase.contains("uppåt") || titleLowerCase.contains("positiv") ||
                    titleLowerCase.contains("över nollan") || titleLowerCase.contains("tjuraktig") || titleLowerCase.contains("blått") ||
                    titleLowerCase.contains("högre") || titleLowerCase.contains("blå") || titleLowerCase.contains("grön") ||
                    titleLowerCase.contains("uppgång") || titleLowerCase.contains("dur") || titleLowerCase.contains("stigande") ||
                    titleLowerCase.contains("tjurstart") || titleLowerCase.contains("norrut") || titleLowerCase.contains("modest upp") ||
                    titleLowerCase.contains("plusöppning") || titleLowerCase.contains("svagt upp")) {

                emoji = EMOJI_CHART_INCREASING;
            }
            else if (titleLowerCase.contains("röd") || titleLowerCase.contains("rött") || titleLowerCase.contains("minusöppning") ||
                    titleLowerCase.contains("björn") || titleLowerCase.contains("nedåt") || titleLowerCase.contains("nedgång") ||
                    titleLowerCase.contains("svag") || titleLowerCase.contains("negativ") || titleLowerCase.contains("moll") ||
                    titleLowerCase.contains("utför") || titleLowerCase.contains("fallande") || titleLowerCase.contains("sur") ||
                    titleLowerCase.contains("motvind") || titleLowerCase.contains("under nollan") || titleLowerCase.contains("ned") ||
                    titleLowerCase.contains("tapp")) {

                emoji = EMOJI_CHART_DECREASING;
            }
            else {
                emoji = "";
            }
        }
        else if (titleLowerCase.contains("aktierekar") || titleLowerCase.contains("aktieråd") || titleLowerCase.contains("aktietips")) {
            emoji = EMOJI_LIGHT_BULB;
        }
        else {
            emoji = "";
        }

        List<String> symbols = Collections.emptyList();

        /*
        try {
            symbols = PlaceraUtil.getTickerSymbols(item.getLink());
        }
        catch (Exception e) {
            symbols = Collections.emptyList();
        }
        */

        tickerSymbols = toSymbols(symbols);

        String title = item.getTitle().orElse("").trim();
        String description = item.getDescription().orElse("").trim();
        String url = toShortUrl(item.getLink().orElse(""));
        String tweet =  "Placera " + EMOJI_NEWSPAPER + " " + title + " " + emoji + generalTitleSuffix(title, description) + "\n" + description;
        tweet = TwitterUtil.trim(tweet, url.length() + 1);
        tweet += "\n" + url;

        if (!tickerSymbols.isEmpty())
            tweet += "\n\n" + tickerSymbols;

        return TwitterUtil.trim(tweet);
    }


    public static String createBreakitTweet(Item item) {
        String title = item.getTitle().orElse("").trim();
        String description = item.getDescription().orElse("").trim();
        String titleLowerCase = title.toLowerCase();

        String emoji = "";
        if (titleLowerCase.contains("det händer idag") || titleLowerCase.contains("detta händer idag") ||
                titleLowerCase.contains("det händer i dag")) {
            emoji = EMOJI_FACE_WITH_MONOCLE;
        }

        String url = toShortUrl(item.getLink().orElse(""));
        return "Breakit " + EMOJI_NEWSPAPER + " " + title + " " + emoji + generalTitleSuffix(title, description) + "\n" + url;
    }


    public static String createAffarsvarldenTweet(Item item) {
        String title = item.getTitle().orElse("").trim();
        String description = item.getDescription().orElse("").trim();
        String url = toShortUrl(item.getLink().orElse(""));
        return "Affärsvärlden " + EMOJI_NEWSPAPER + " " + title + " " + generalTitleSuffix(title, description) + "\n" + url;
    }


    public static String createInvestingComTweet(Item item) {
        String title = item.getTitle().orElse("").trim();
        String url = toShortUrl(item.getLink().orElse(""));
        return "Investing com " + EMOJI_NEWSPAPER + " " + title + "\n" + url;
    }


    public static String createDiDigitalTweet(Item item) {
        String title = item.getTitle().orElse("").trim();
        String url = toShortUrl(item.getLink().orElse(""));
        return "DI Digital " + EMOJI_NEWSPAPER + " " + title + "\n" + url;
    }


    public static String createFiSanktionerTweet(Item item) {
        String title = item.getTitle().orElse("").trim();
        String description = item.getDescription().orElse("").trim();
        String hashTags = toFiSanktionerHashTags(title, description);
        String url = toShortUrl(item.getLink().orElse(""));

        String tweet = "FI " + EMOJI_POLICE_OFFICER + " " + title + "\n" + description;
        tweet = TwitterUtil.trim(tweet, hashTags.length() + url.length() + 3);
        return tweet + "\n" + url + "\n\n" + hashTags;
    }

    private static String toFiSanktionerHashTags(String title, String description) {
        String hashTags = "";

        /*
        description = description.toLowerCase();
        title = title.toLowerCase();

        if (description.contains("penningtvätt")) {
            hashTags += "#penningtvätt ";
        }
        if (description.contains("sanktionsavgift")) {
            hashTags += "#sanktionsavgift ";
        }
        if (description.contains("straffavgift")) {
            hashTags += "#straffavgift ";
        }
        if (title.contains("tillstånd återkallas") || description.contains("återkallar tillståndet")) {
            hashTags += "#tillståndåterkallas ";
        }
        */

        return hashTags.trim();
    }


    private static String toShortUrl(String url) {
        if (url == null || url.isEmpty())
            return "";

        try {
            BitlyClient theURLService = new BitlyClient(url, BITLY_ACCESSTOKEN);
            theURLService.shortenLink();
            url = theURLService.finalUrl;
        } catch (Exception e) {
            Logger logger = Logger.getLogger("com.apptastic.fininsyn");
            logger.warning("Failed to get short url. " + e.getMessage());
        }

        return url;
    }


    private static String xmlEscape(String text) {
        return text.replace("&amp;", "&")
                   .replace("&quot;", "\"")
                   .replace("&apos;", "'")
                   .replace("&lt;","<")
                   .replace("&gt;", ">")
                   .replace("&#039;", "'");
    }


    private static String toSymbols(List<String> symbols) {
        return symbols.stream()
                      .map(TwitterUtil::toCashTag)
                      .collect (Collectors.joining(" "))
                      .trim();
    }


    private static String removeQuotes(String text) {
        if (text != null && text.length() > 2 && text.charAt(0) == '"' && text.charAt(text.length() - 1) == '"')
            text = text.substring(1, text.length() - 1);

        return text;
    }


    private static String generalTitleSuffix(String title, String description) {
        String titleSuffix = "";

        if (isReportAnnouncement(title, description)) {
            titleSuffix += EMOJI_LOUDSPEAKER;
        }

        return titleSuffix;
    }

    private static boolean isReportAnnouncement(String title, String description) {
        description = Optional.ofNullable(description).orElse("").toLowerCase();

        return TextUtil.containsAny(description,"redovisar ett resultat", "redovisade ett result",
                "redovisar ett rörelseresultat", "redovisade ett rörelseresultat",
                "redovisar ett ebitda-resultat", "redovisade ett ebitda-resultat") ||
                (TextUtil.containsAny(description, "redovisar", "redovisade") && TextUtil.containsAtLeast(description, 2, "nettoomsättning", "rörelseresultat", "kassaflöde",
                        "bruttomarginal", "nettoresultat", "kvartal",
                        "resultatet per aktie", "resultat per aktie",
                        "resultatet före skatt", "resultat före skatt",
                        "resultatet efter skatt", "resultat efter skatt"));
    }

    private static String reportSentiment(String title) {
        String sentiment = "";
        title = Optional.ofNullable(title).orElse("").toLowerCase();

        if (TextUtil.containsAny(title, "över förväntan", "stigande omsättning", "ökade vinsten")) {
            sentiment = EMOJI_THUMBS_UP;
        }
        else if (TextUtil.containsAny(title, "sämre", "under förväntan", "förlust", "minusresultat")) {
            sentiment = EMOJI_THUMBS_DOWN;
        }

        return sentiment;
    }
}
