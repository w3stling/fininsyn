package com.apptastic.fininsyn.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;


@Document(collection = "PdmrTransaction")
public class PdmrTransaction {
    @Id
    public String id;
    public String transactionId;
    public String publicationDate;
    public String lastAttempt;

    public PdmrTransaction() {

    }

    public PdmrTransaction(String id, String transactionId, String publicationDate, String lastAttempt) {
        this.id = id;
        this.transactionId = transactionId;
        this.publicationDate = publicationDate;
        this.lastAttempt = lastAttempt;
    }

    public PdmrTransaction(PdmrTransaction o) {
        id = o.id;
        transactionId = o.transactionId;
        publicationDate = o.publicationDate;
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

    public String getPublicationDate() {
        return publicationDate;
    }

    public void setPublicationDate(String publicationDate) {
        this.publicationDate = publicationDate;
    }

    public String getLastAttempt() {
        return lastAttempt;
    }

    public void setLastAttempt(String lastAttempt) {
        this.lastAttempt = lastAttempt;
    }
}
