package com.apptastic.fininsyn.repurchase;

import com.apptastic.repurchase.Transaction;

public class RepurchaseFilter {

    public static boolean quantity(Transaction transaction) {
        return transaction.getQuantity() != 0.0;
    }

    public static boolean type(Transaction transaction) {
        return "Repurchase".equalsIgnoreCase(transaction.getType()) || "Sell".equalsIgnoreCase(transaction.getType());
    }
}
