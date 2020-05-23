package com.apptastic.fininsyn.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Set;
import java.util.TreeSet;


@Document(collection = "PdmrMfsaTransaction")
public class PdmrMfsaTransaction {
    @Id
    public String id;
    public String transactionId;
    public Set<String> transactions;
    public String lastAttempt;

    public PdmrMfsaTransaction() {

    }

    public PdmrMfsaTransaction(String id, String transactionId, Set<String> transactions, String lastAttempt) {
        this.id = id;
        this.transactionId = transactionId;
        this.transactions = transactions;
        this.lastAttempt = lastAttempt;
    }

    public PdmrMfsaTransaction(PdmrMfsaTransaction o) {
        id = o.id;
        transactionId = o.transactionId;
        transactions = o.transactions;
        lastAttempt = o.lastAttempt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public Set<String> getTransactions() {
        if (transactions == null) {
            return new TreeSet<>();
        }
        return transactions;
    }

    public void setTransactions(Set<String> transactions) {
        this.transactions = (transactions != null) ? new TreeSet<>(transactions) : new TreeSet<>();
    }

    public String getLastAttempt() {
        return lastAttempt;
    }

    public void setLastAttempt(String lastAttempt) {
        this.lastAttempt = lastAttempt;
    }
}
