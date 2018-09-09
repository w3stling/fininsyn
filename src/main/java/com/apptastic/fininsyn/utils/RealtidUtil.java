package com.apptastic.fininsyn.utils;

import com.apptastic.fininsyn.InstrumentLookup;
import com.apptastic.insynsregistret.FreeTextQuery;
import com.apptastic.insynsregistret.FreeTextQueryBuilder;
import com.apptastic.insynsregistret.Insynsregistret;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;


public class RealtidUtil {
    private static final String HTTP_USER_AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_13_5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/66.0.3359.181 Safari/537.36";

    public static List<String> getTickerSymbols(String url) {
        List<String> symbols = Collections.emptyList();
        try(BufferedReader reader = downloadPage(url, "UTF-8")) {
            String line;

            while ((line = reader.readLine()) != null) {
                if (line.contains("field field-name-field-tags"))
                    symbols = getTickerName(line);
            }

            if (symbols.size() != 1)
                return Collections.emptyList();

            Insynsregistret ir = new Insynsregistret();

            symbols = symbols.stream()
                    .parallel()
                    .filter(i -> isIssuer(ir, i))
                    .limit(5)
                    .map(InstrumentLookup.getInstance()::getInstrument)
                    .filter(Objects::nonNull)
                    .map(InstrumentLookup.Instrument::getSymbol)
                    .collect(Collectors.toList());

        }
        catch (IOException e) {

        }

        return symbols;
    }

    private static boolean isIssuer(Insynsregistret ir, String issuer) {
        if (issuer.length() <= 3 && !issuer.equals("ÅF") && issuer.equals("HM"))
            return false;

        try {
            FreeTextQuery query = FreeTextQueryBuilder.issuer(issuer).build();
            return ir.search(query).count() > 0;
        }
        catch (IOException e) {

        }

        return false;
    }


    private static List<String> getTickerName(String line) {
        List<String> symbols = new ArrayList<>();
        int end = line.indexOf("<a");

        while (end != -1) {
            int start = line.indexOf("\">", end);

            if (start == -1)
                break;

            end = line.indexOf("</a>", start);

            if (end == -1 || start + 2 > end)
                break;

            String symbol = line.substring(start + 2, end).trim();
            symbols.add(symbol);
        }

        return symbols;
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
        //String url = "https://www.realtid.se/kraftigt-vinstlyft-i-d-carnegie";
        //String url = "https://www.realtid.se/ipo-klimatet-fortsatt-starkt-i-sverige";
        //String url = "https://www.realtid.se/ekn-far-ny-ordforande";
        //String url = "https://www.realtid.se/capios-styrelse-nobbar-franskt-bud";
        String url = "https://www.realtid.se/swedish-match-blir-storagare-i-gotlandssnus";

        List<String> symbols = getTickerSymbols(url);

        symbols.stream().forEach(System.out::println);

    }
}
