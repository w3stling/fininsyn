package com.apptastic.fininsyn;

import com.apptastic.insynsregistret.*;
import com.apptastic.tickersymbol.TickerSymbol;
import com.apptastic.tickersymbol.TickerSymbolSearch;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;


@Scope(value = "singleton")
@Component
public class InstrumentLookup {
    private static InstrumentLookup instance = null;
    private TickerSymbolSearch symbolSearch;
    private Insynsregistret insynsregistret;
    private ConcurrentHashMap<String, Instrument> symbolCache;
    private ConcurrentHashMap<String, Instrument> longNameCache;


    public synchronized static InstrumentLookup getInstance() {
        if (instance == null)
            instance = new InstrumentLookup();

        return instance;
    }


    public InstrumentLookup() {
        symbolSearch = new TickerSymbolSearch();
        insynsregistret = new Insynsregistret();
        symbolCache = new ConcurrentHashMap<>();
        longNameCache = new ConcurrentHashMap<>();
    }


    public Instrument getInstrument(String name) {
        if (name == null || name.isEmpty())
            return null;

        Instrument instrument = null;

        try {
            name = name.trim();

            if (name.endsWith(".") && name.length() > 1)
                name = name.substring(0, name.length() - 1);

            instrument = longNameCache.get(name.toUpperCase());

            if (instrument == null) {
                TickerSymbol tickerSymbol = symbolSearch.searchByName(name)
                                                        .findFirst()
                                                        .orElse(null);

                if (tickerSymbol != null) {
                    String symbol = cleanSymbolName(tickerSymbol.getSymbol());
                    instrument = new Instrument(symbol, tickerSymbol.getName(), tickerSymbol.getIsin());
                    longNameCache.put(name.toUpperCase(), instrument);
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return instrument;
    }

    public Instrument getInstrument(String issuer, String isin, String currency2) {
        isin = isin.trim();
        final String currency = currency2.trim();

        if (isin.isEmpty())
            return null;

        Instrument instrument = fetchAndCacheInstrument(isin, currency);

        if (instrument != null)
            return instrument;

        // Try finding symbol with currency SEK if reported with wrong currency
        if (!currency.isEmpty() && !currency.equals("SEK"))
            instrument = fetchAndCacheInstrument(isin, "SEK");

        if (instrument != null) {
            String key = getKey(isin, "SEK");
            symbolCache.put(key, instrument);
            return instrument;
        }

        // Find isin codes that have been reported for this issuer
        issuer = issuer.trim();
        List<String> isinList = getIsinByIssuer(issuer, currency);

        instrument = isinList.stream()
                             .map(i -> fetchAndCacheInstrument(i, currency))
                             .filter(Objects::nonNull)
                             .findFirst()
                             .orElse(null);

        if (instrument != null) {
            String key = getKey(isin, currency);
            symbolCache.put(key, instrument);
        }

        return instrument;
    }

    private String getKey(String isin, String currency) {
        return isin.toUpperCase() + currency.toUpperCase();
    }

    private Instrument fetchAndCacheInstrument(String isin, String currency) {
        String key = getKey(isin, currency);
        Instrument instrument = symbolCache.getOrDefault(key, null);

        if (instrument != null)
            return instrument;

        instrument = fetchInstrument(isin, currency);

        if (instrument != null)
            symbolCache.put(key, instrument);

        return instrument;
    }

    private Instrument fetchInstrument(String isin, String currency) {
        Instrument instrument = null;

        TickerSymbol tickerSymbol = symbolSearch.searchByIsin(isin)
                                                .filter(t -> filterCurrency(t, currency))
                                                .findFirst()
                                                .orElse(null);

        if (tickerSymbol != null) {
            String symbol = cleanSymbolName(tickerSymbol.getSymbol());
            String isinCode = (tickerSymbol.getIsin() != null) ? tickerSymbol.getIsin() : isin;
            instrument = new Instrument(symbol.trim(), tickerSymbol.getName().trim(), isinCode.trim());
        }

        return instrument;
    }

    private String cleanSymbolName(String symbol) {
        symbol = symbol.toUpperCase();
        symbol = trimBeforeText(symbol," BTA");
        symbol = trimBeforeText(symbol," BTU");
        symbol = trimBeforeText(symbol," SEK");
        symbol = trimBeforeText(symbol," MTF");
        symbol = trimBeforeText(symbol," TR");
        symbol = trimBeforeText(symbol," TO");
        symbol = trimBeforeText(symbol," PREF");
        symbol = trimBeforeText(symbol, "001");
        symbol = trimBeforeText(symbol, "1V");

        symbol = symbol.replaceFirst("(.+) \\d\\d+", "$1");
        symbol = symbol.replace(" ", "");

        return symbol;
    }

    private List<String> getIsinByIssuer(String issuer, String currency) {
        if (issuer == null || issuer.isEmpty())
            return Collections.emptyList();

        try {
            FreeTextQuery issuerQuery = FreeTextQueryBuilder.issuer(issuer).build();
            String issuerName = insynsregistret.search(issuerQuery).findFirst().orElse("");

            TransactionQuery transactionQuery = TransactionQueryBuilder.publicationsLastDays(3 * 365).issuer(issuerName).build();
            return insynsregistret.search(transactionQuery)
                                  .filter(t -> currency.equals(t.getCurrency()) && !t.getIsin().isEmpty())
                                  .map(Transaction::getIsin)
                                  .distinct()
                                  .collect(Collectors.toList());
        }
        catch (IOException e) {
            return Collections.emptyList();
        }
    }


    private String trimBeforeText(String value, String text) {
        int index = value.indexOf(text);

        if (index != -1)
            value = value.substring(0, index);

        return value;
    }


    private boolean filterCurrency(TickerSymbol tickerSymbol, String currency) {
        if (currency.isEmpty())
            return true;

        return currency.equals(tickerSymbol.getCurrency());
    }


    public static class Instrument {
        private String symbol;
        private String name;
        private String isin;

        public Instrument(String symbol, String name, String isin) {
            this.symbol = symbol;
            this.name = name;
            this.isin = isin;
        }

        public String getSymbol() {
            return symbol;
        }

        public String getName() {
            return name;
        }

        public String getIsin() {
            return isin;
        }
    }

}
