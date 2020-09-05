package com.apptastic.fininsyn.pdmr.mfsa;

import com.apptastic.mfsapdmr.Transaction;

import java.time.LocalDate;

public class PdmrMfsaTransactionFilter {

    public static boolean transactionFilter(Transaction transaction) {
        return  transaction != null &&
                transaction.getPrice() != 0.0 &&
                transaction.getVolume() != 0.0;
    }

    public static boolean sweden(Transaction transaction) {
        return "SEK".equalsIgnoreCase(transaction.getCurrency()) ||
               transaction.getCurrency().toLowerCase().contains("swedish") ||
               transaction.getCurrency().toLowerCase().contains("krona") ||
               transaction.getPlaceOfTransaction().toLowerCase().contains("stockholm");
    }

    public static boolean natureOfTransaction(Transaction transaction) {
        return transaction.getNatureOfTransaction().equalsIgnoreCase("Buy") ||
               transaction.getNatureOfTransaction().equalsIgnoreCase("Purchase") ||
               transaction.getNatureOfTransaction().equalsIgnoreCase("Bought") ||
               transaction.getNatureOfTransaction().equalsIgnoreCase("Sell") ||
               transaction.getNatureOfTransaction().equalsIgnoreCase("Sold");
    }

    public static boolean dateTime(Transaction transaction) {
        return transaction.getDate().isAfter(LocalDate.now().minusDays(60));
    }
}
