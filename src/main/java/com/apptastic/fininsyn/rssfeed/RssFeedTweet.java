package com.apptastic.fininsyn.rssfeed;

import com.apptastic.fininsyn.utils.PlaceraUtil;
import com.apptastic.fininsyn.utils.RealtidUtil;
import com.apptastic.fininsyn.utils.TwitterUtil;
import com.apptastic.rssreader.Item;
import net.swisstech.bitly.BitlyClient;
import net.swisstech.bitly.model.Response;
import net.swisstech.bitly.model.v3.ShortenResponse;

import java.util.*;
import java.util.stream.Collectors;


public class RssFeedTweet {
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
        return "Ekobrottsmyndigheten " + EMOJI_POLICE_OFFICER + " " + title + "\n" + url + "\n\n" + "#ekobrottsmyndigheten";
    }

    public static String createVeckansAffarerTweet(Item item) {
        String title = item.getTitle().orElse("").trim();
        String url = toShortUrl(item.getLink().orElse(""));
        String tweet = "Veckans Affärer " + EMOJI_NEWSPAPER + " " + title + "\n" + url;
        return TwitterUtil.trim(tweet);
    }

    public static String createRealtidTweet(Item item) {
        String url = toShortUrl(item.getLink().orElse(""));
        String title = xmlEscape(item.getTitle().orElse("")).trim();
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

        String tweet = "Realtid " + EMOJI_NEWSPAPER + " " + title + "\n" + tickerSymbols + "\n" + url;
        return TwitterUtil.trim(tweet);
    }

    public static String createPlaceraTweet(Item item) {
        String emoji;
        String tickerSymbols = "";
        String titleLowerCase = item.getTitle().orElse("").toLowerCase().trim();

        if (titleLowerCase.startsWith("börsen idag:")) {
            if (titleLowerCase.contains("stark") || titleLowerCase.contains("uppåt") || titleLowerCase.contains("positiv") ||
                    titleLowerCase.contains("över nollan") || titleLowerCase.contains("tjuraktig") || titleLowerCase.contains("blått") ||
                    titleLowerCase.contains("högre") || titleLowerCase.contains("blå") || titleLowerCase.contains("grön") ||
                    titleLowerCase.contains("uppgång") || titleLowerCase.contains("dur")) {

                emoji = EMOJI_CHART_INCREASING;
            }
            else if (titleLowerCase.contains("röd") || titleLowerCase.contains("björn") || titleLowerCase.contains("nedåt") ||
                    titleLowerCase.contains("nedgång") || titleLowerCase.contains("svag") || titleLowerCase.contains("negativ") ||
                    titleLowerCase.contains("moll")) {

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
        String tweet =  "Placera " + EMOJI_NEWSPAPER + " " + title + " " + emoji + "\n" + description;
        tweet = TwitterUtil.trim(tweet, url.length() + 1);
        tweet += "\n" + url;

        if (!tickerSymbols.isEmpty())
            tweet += "\n\n" + tickerSymbols;

        return TwitterUtil.trim(tweet);
    }


    public static String createBreakitTweet(Item item) {
        String title = item.getTitle().orElse("").trim();
        String url = toShortUrl(item.getLink().orElse(""));
        return "Breakit " + EMOJI_NEWSPAPER + " " + title + "\n" + url;
    }


    public static String createAffarsvarldenTweet(Item item) {
        String title = item.getTitle().orElse("").trim();
        String url = toShortUrl(item.getLink().orElse(""));
        return "Affärsvärlden " + EMOJI_NEWSPAPER + " " + title + "\n" + url;
    }


    private static String toShortUrl(String url) {
        if (url == null || url.isEmpty())
            return "";

        BitlyClient client = new BitlyClient("707fb3170e622ee0c650c02fe09151cc2e012515");
        Response<ShortenResponse> respShort = client.shorten()
                .setLongUrl(url)
                .call();

        if (respShort.status_code == 200)
            url = respShort.data.url;

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
}
