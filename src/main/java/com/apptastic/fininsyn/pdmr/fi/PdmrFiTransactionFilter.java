package com.apptastic.fininsyn.pdmr.fi;

import com.apptastic.insynsregistret.Transaction;

import java.util.List;

import static com.apptastic.fininsyn.utils.CurrencyConverter.amountInSek;

public class PdmrFiTransactionFilter {
    private static final double EXCHANGE_RATE_EUR_SEK = 10.28;
    private static final double EXCHANGE_RATE_USD_SEK = 8.60;
    private static final double EXCHANGE_RATE_CAD_SEK = 6.70;
    private static final double EXCHANGE_RATE_DKK_SEK = 1.38;
    private static final double EXCHANGE_RATE_NOK_SEK = 1.07;

    public static boolean transactionFilter(String lastProcessed, String publicationDate) {
        return lastProcessed != null && publicationDate != null && lastProcessed.compareTo(publicationDate) < 0;
    }

    public static boolean transactionFilter(Transaction transaction) {
        return  transaction != null &&
                transaction.getPrice() != 0.0 &&
                transaction.getQuantity() != 0.0 &&
                !transaction.getIsin().isEmpty() &&
                "Aktuell".equals(transaction.getStatus()) &&
                !"Syntetisk option".equals(transaction.getInstrumentName()) &&
                ("Förvärv".equals(transaction.getNatureOfTransaction()) || "Avyttring".equals(transaction.getNatureOfTransaction()));
    }

    public static boolean transactionAmountFilter(List<Transaction> transactions) {
        if (transactions.isEmpty()) {
            return false;
        }

        double totalAmount = transactions.stream()
                .mapToDouble(PdmrFiTransactionFilter::toAmount)
                .sum();

        String currency = transactions.get(0).getCurrency();
        double currencyConvertedAmount = amountInSek(totalAmount, currency);

        return currencyConvertedAmount >= 5000.0;
    }

    public static double toAmount(Transaction transaction) {
        double amount = 0.0;

        if ("Antal".equals(transaction.getUnit()))
            amount = transaction.getPrice() * transaction.getQuantity();
        else if ("Belopp".equals(transaction.getUnit()))
            amount = transaction.getQuantity();

        return amount;
    }

    public static double toQuantity(Transaction transaction) {
        double quantity = 0.0;

        if ("Antal".equals(transaction.getUnit()))
            quantity = transaction.getQuantity();
        else if ("Belopp".equals(transaction.getUnit()) && transaction.getPrice() > 0.0)
            quantity = transaction.getQuantity() / transaction.getPrice();

        return quantity;
    }

}
