package com.apptastic.fininsyn.utils;

import com.apptastic.fininsyn.InstrumentLookup;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;


public class PlaceraUtil {
    private static final String HTTP_USER_AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_13_5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/66.0.3359.181 Safari/537.36";


    // Recommendation
    public static List<String> getTickerSymbols(String url) {
        if (url == null || url.isEmpty())
            return Collections.emptyList();

        try (BufferedReader reader = downloadPage(url, "UTF-8")) {
            String line;
            List<String> tickerNames = new ArrayList<>();

            while ((line = reader.readLine()) != null) {

                if (line.contains("ellipsis")) {
                    String tickerName = getTickerName(line);
                    tickerNames.add(tickerName);
                }

                if (tickerNames.size() >= 4)
                    break;
            }

            InstrumentLookup lookup = InstrumentLookup.getInstance();

            // TODO: test only!!!
            return tickerNames.stream()
                    .filter(n -> !n.toLowerCase().contains("omx"))
                    .limit(4)
                    .parallel()
                    .map(lookup::getInstrument)
                    .filter(Objects::nonNull)
                    .map(InstrumentLookup.Instrument::getSymbol)
                    .collect(Collectors.toList());
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return Collections.emptyList();
    }


    private static String getTickerName(String line) {
        int start = line.lastIndexOf("\">");
        int end = line.lastIndexOf("</a>");

        if (start == -1 || end == -1 || start + 2 > end)
            return null;

        return line.substring(start + 2, end).trim();
    }


    private static BufferedReader downloadPage(String url, String characterEncoding) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();

        connection.setRequestProperty("Accept-Encoding", "gzip, deflate");
        connection.setRequestProperty("User-Agent", HTTP_USER_AGENT);

        InputStream inputStream = connection.getInputStream();

        if ("gzip".equals(connection.getContentEncoding()))
            inputStream = new GZIPInputStream(inputStream);

        return new BufferedReader(new InputStreamReader(inputStream, characterEncoding));
    }


    public static void main(String[] args) {
        //final String url = "https://www.avanza.se/placera/redaktionellt/2018/07/13/dagens-nya-aktierekar.html";
        //final String url = "https://www.avanza.se/placera/redaktionellt/2018/07/12/dagens-nya-aktierekar.html";
        //final String url = "https://www.avanza.se/placera/redaktionellt/2018/07/05/dagens-nya-aktierekar.html";
        //final String url = "https://www.avanza.se/placera/redaktionellt/2018/07/13/vi-ar-absolut-inte-nojda.html";
        final String url = "https://www.avanza.se/placera/redaktionellt/2018/07/13/rapportfloden-blandade-resultat.html";

        long start = System.currentTimeMillis();
        getTickerSymbols(url)
                .stream().forEach(System.out::println);
        long end = System.currentTimeMillis();

        System.out.println("Total time: " + (end - start));

    }

}
