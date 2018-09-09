package com.apptastic.fininsyn.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;


@Document(collection = "ShortSelling")
public class ShortSelling {
    @Id
    public String id;
    public String transactionId;
    public String publicationDate;
    public String lastAttempt;

    public ShortSelling() {

    }

    public ShortSelling(String id, String transactionId, String publicationDate, String lastAttempt) {
        this.id = id;
        this.transactionId = transactionId;
        this.publicationDate = publicationDate;
        this.lastAttempt = lastAttempt;
    }

    public ShortSelling(ShortSelling o) {
        this.id = o.id;
        this.transactionId = o.transactionId;
        this.publicationDate = o.publicationDate;
        this.lastAttempt = o.lastAttempt;
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
