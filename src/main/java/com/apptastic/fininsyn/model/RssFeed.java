package com.apptastic.fininsyn.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;


@Document(collection = "RssFeed")
public class RssFeed {
    @Id
    public String id;
    public String rssFeedId;
    public String riksbankenPubDate;
    public String finanspolitiskaradetPubDate;
    public String konjunkturinstitutetPubDate;
    public String scbPubDate;
    public String ekobrottsmyndighetenPubDate;
    public String veckansAffarerPubDate;
    public String realtidPubDate;
    public String placeraPubDate;
    public String breakitPubDate;
    public String affarsvarldenPubDate;
    public String investingComPubDate;
    public String lastAttempt;


    public RssFeed() {

    }

    public RssFeed(String id, String rssFeedId, String riksbankenPubDate, String finanspolitiskaradetPubDate,
                   String konjunkturinstitutetPubDate, String scbPubDate, String ekobrottsmyndighetenPubDate,
                   String veckansAffarerPubDate, String realtidPubDate, String placeraPubDate, String breakitPubDate,
                   String affarsvarldenPubDate, String investingComPubDate, String lastAttempt) {

        this.id = id;
        this.rssFeedId = rssFeedId;
        this.riksbankenPubDate = riksbankenPubDate;
        this.finanspolitiskaradetPubDate = finanspolitiskaradetPubDate;
        this.konjunkturinstitutetPubDate = konjunkturinstitutetPubDate;
        this.scbPubDate = scbPubDate;
        this.ekobrottsmyndighetenPubDate = ekobrottsmyndighetenPubDate;
        this.veckansAffarerPubDate = veckansAffarerPubDate;
        this.realtidPubDate = realtidPubDate;
        this.placeraPubDate = placeraPubDate;
        this.breakitPubDate = breakitPubDate;
        this.affarsvarldenPubDate = affarsvarldenPubDate;
        this.investingComPubDate = investingComPubDate;
        this.lastAttempt = lastAttempt;
    }

    public RssFeed(RssFeed o) {

        id = o.id;
        rssFeedId = o.rssFeedId;
        riksbankenPubDate = o.riksbankenPubDate;
        finanspolitiskaradetPubDate = o.finanspolitiskaradetPubDate;
        konjunkturinstitutetPubDate = o.konjunkturinstitutetPubDate;
        scbPubDate = o.scbPubDate;
        ekobrottsmyndighetenPubDate = o.ekobrottsmyndighetenPubDate;
        veckansAffarerPubDate = o.veckansAffarerPubDate;
        realtidPubDate = o.realtidPubDate;
        placeraPubDate = o.placeraPubDate;
        breakitPubDate = o.breakitPubDate;
        affarsvarldenPubDate = o.affarsvarldenPubDate;
        investingComPubDate = o.investingComPubDate;
        lastAttempt = o.lastAttempt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRssFeedId() {
        return rssFeedId;
    }

    public void setRssFeedId(String rssFeedId) {
        this.rssFeedId = rssFeedId;
    }

    public String getRiksbankenPubDate() {
        return riksbankenPubDate;
    }

    public void setRiksbankenPubDate(String riksbankenPubDate) {
        this.riksbankenPubDate = riksbankenPubDate;
    }

    public String getFinanspolitiskaradetPubDate() {
        return finanspolitiskaradetPubDate;
    }

    public void setFinanspolitiskaradetPubDate(String finanspolitiskaradetPubDate) {
        this.finanspolitiskaradetPubDate = finanspolitiskaradetPubDate;
    }

    public String getKonjunkturinstitutetPubDate() {
        return konjunkturinstitutetPubDate;
    }

    public void setKonjunkturinstitutetPubDate(String konjunkturinstitutetPubDate) {
        this.konjunkturinstitutetPubDate = konjunkturinstitutetPubDate;
    }

    public String getScbPubDate() {
        return scbPubDate;
    }

    public void setScbPubDate(String scbPubDate) {
        this.scbPubDate = scbPubDate;
    }

    public String getEkobrottsmyndighetenPubDate() {
        return ekobrottsmyndighetenPubDate;
    }

    public void setEkobrottsmyndighetenPubDate(String ekobrottsmyndighetenPubDate) {
        this.ekobrottsmyndighetenPubDate = ekobrottsmyndighetenPubDate;
    }

    public String getVeckansAffarerPubDate() {
        return veckansAffarerPubDate;
    }

    public void setVeckansAffarerPubDate(String veckansAffarerPubDate) {
        this.veckansAffarerPubDate = veckansAffarerPubDate;
    }

    public String getRealtidPubDate() {
        return realtidPubDate;
    }

    public void setRealtidPubDate(String realtidPubDate) {
        this.realtidPubDate = realtidPubDate;
    }

    public String getPlaceraPubDate() {
        return placeraPubDate;
    }

    public void setPlaceraPubDate(String placeraPubDate) {
        this.placeraPubDate = placeraPubDate;
    }

    public String getBreakitPubDate() {
        return breakitPubDate;
    }

    public void setBreakitPubDate(String breakitPubDate) {
        this.breakitPubDate = breakitPubDate;
    }

    public String getAffarsvarldenPubDate() {
        return affarsvarldenPubDate;
    }

    public void setAffarsvarldenPubDate(String affarsvarldenPubDate) {
        this.affarsvarldenPubDate = affarsvarldenPubDate;
    }

    public String getInvestingComPubDate() {
        return investingComPubDate;
    }

    public void setInvestingComPubDate(String investingComPubDate) {
        this.investingComPubDate = investingComPubDate;
    }

    public String getLastAttempt() {
        return lastAttempt;
    }

    public void setLastAttempt(String lastAttempt) {
        this.lastAttempt = lastAttempt;
    }

}
