package com.apptastic.fininsyn.rssfeed;

import com.apptastic.fininsyn.TwitterPublisher;
import com.apptastic.fininsyn.model.RssFeed;
import com.apptastic.fininsyn.repo.RssFeedRepository;
import com.apptastic.rssreader.Item;
import com.apptastic.rssreader.RssReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;


@Component
public class RssFeedPublisher {
    private static final String RSS_FEED_RISKBANKEN = "https://www.riksbank.se/sv/rss/pressmeddelanden";
    private static final String RSS_FEED_FINANSPOLITISKARADET = "http://www.finanspolitiskaradet.se/2.5dd459a31158f2d75c380003166/12.778e24d112a169fd1c1800036576.portlet?state=rss&sv.contenttype=text/xml;charset=UTF-8";
    private static final String RSS_FEED_KONJUNKTURINSTITUTET = "https://www.konj.se/4.2de5c57614f808a95afcc13f/12.2de5c57614f808a95afcc354.portlet?state=rss&sv.contenttype=text/xml;charset=UTF-8";
    private static final String RSS_FEED_SCB = "http://www.scb.se/rss/";
    private static final String RSS_FEED_EKOBROTTSMYNDIGHETEN = "https://www.ekobrottsmyndigheten.se/Templates/Handlers/News/HandlerNewsRss.ashx?languageBranch=sv";
    private static final String RSS_FEED_VECKANS_AFFARER = "https://www.va.se/rss/";
    private static final String RSS_FEED_REALTID = "https://www.realtid.se/rss/senaste";
    private static final String RSS_FEED_PLACERA = "https://www.avanza.se/placera/forstasidan.rss.xml";
    private static final String RSS_FEED_BREAKIT = "https://www.breakit.se/feed/artiklar";
    private static final String RSS_FEED_AFFARSVARLDEN = "https://www.affarsvarlden.se/rss.xml";
    private static final String DATE_TIME_PUBDATE_FORMAT = "EEE, dd MMM yyyy HH:mm:ss ZZZZ";
    private static final String DATE_TIME_PUBDATE_GMT_FORMAT = "EEE, dd MMM yyyy HH:mm:ss zzz";
    private static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private static final String TIME_ZONE = "Europe/Stockholm";
    @Autowired
    RssReader rssReader;
    @Autowired
    RssFeedRepository repository;
    @Autowired
    TwitterPublisher twitter;


    @Scheduled(initialDelay = 30000, fixedRate = 300000)
    public void checkRssFeeds() {
        Logger logger = Logger.getLogger("com.apptastic.fininsyn");

        try {
            if (logger.isLoggable(Level.INFO))
                logger.log(Level.INFO, "Checking RSS feeds");

            RssFeed lastPublished = getLastPublishedRssFeed();
            RssFeed next = new RssFeed(lastPublished);

            checkRiksbankenRssFeed(lastPublished, next);
            checkFinanspolitiskaradetRssFeed(lastPublished, next);
            checkKonjunkturinstitutetRssFeed(lastPublished, next);
            checkScbRssFeed(lastPublished, next);
            checkEkobrottsmyndighetenRssFeed(lastPublished, next);
            checkVeckansAffarerRssFeed(lastPublished, next);
            checkRealtidRssFeed(lastPublished, next);
            checkBreakitRssFeed(lastPublished, next);
            checkAffarsvarldenRssFeed(lastPublished, next);
            checkPlaceraRssFeed(lastPublished, next);

            next.setLastAttempt(now());
            repository.save(next).subscribe();
        }
        catch (Exception e) {
            logger.log(Level.WARNING, "Exception thrown while checking rss feeds. ", e);
        }
    }


    private void checkRiksbankenRssFeed(RssFeed lastPublished, RssFeed next) {
        SimpleDateFormat formatter = new SimpleDateFormat(DATE_TIME_PUBDATE_FORMAT);

        try {
            rssReader.read(RSS_FEED_RISKBANKEN)
                    .filter(i -> filterPubDate(formatter, i.getPubDate().orElse(""), lastPublished.getRiksbankenPubDate()))
                    .filter(RssFeedFilter::filterContentRiksbanken)
                    .sorted(this::sortByPublicationDate)
                    .map(i -> { next.setRiksbankenPubDate(i.getPubDate().orElse("")); return i; })
                    .map(RssFeedTweet::createRiskbankenTweet)
                    .forEach(twitter::publishTweet);
        }
        catch (IOException e) {
            e.printStackTrace();
        }

    }


    private void checkFinanspolitiskaradetRssFeed(RssFeed lastPublished, RssFeed next) {
        SimpleDateFormat formatter = new SimpleDateFormat(DATE_TIME_PUBDATE_FORMAT);

        try {
            rssReader.read(RSS_FEED_FINANSPOLITISKARADET)
                    .filter(i -> filterPubDate(formatter, i.getPubDate().orElse(""), lastPublished.getFinanspolitiskaradetPubDate()))
                    .filter(RssFeedFilter::filterContentFinanspolitiskaradet)
                    .sorted(this::sortByPublicationDate)
                    .map(i -> { next.setFinanspolitiskaradetPubDate(i.getPubDate().orElse("")); return i; })
                    .map(RssFeedTweet::createFinanspolitiskaradetTweet)
                    .forEach(twitter::publishTweet);
        }
        catch (IOException e) {
            e.printStackTrace();
        }

    }


    private void checkKonjunkturinstitutetRssFeed(RssFeed lastPublished, RssFeed next) {
        SimpleDateFormat formatter = new SimpleDateFormat(DATE_TIME_PUBDATE_FORMAT);

        try {
            rssReader.read(RSS_FEED_KONJUNKTURINSTITUTET)
                    .filter(i -> filterPubDate(formatter, i.getPubDate().orElse(""), lastPublished.getKonjunkturinstitutetPubDate()))
                    .filter(RssFeedFilter::filterContentKonjunkturinstitutet)
                    .sorted(this::sortByPublicationDate)
                    .map(i -> { next.setKonjunkturinstitutetPubDate(i.getPubDate().orElse("")); return i; })
                    .map(RssFeedTweet::createKonjunkturinstitutetTweet)
                    .forEach(twitter::publishTweet);
        }
        catch (IOException e) {
            e.printStackTrace();
        }

    }


    private void checkScbRssFeed(RssFeed lastPublished, RssFeed next) {
        SimpleDateFormat formatter = new SimpleDateFormat(DATE_TIME_PUBDATE_GMT_FORMAT);

        try {
            rssReader.read(RSS_FEED_SCB)
                    .filter(i -> filterPubDate(formatter, i.getPubDate().orElse(""), lastPublished.getScbPubDate()))
                    .filter(RssFeedFilter::filterContentScb)
                    .sorted(this::sortByPublicationDateGMT)
                    .map(i -> { next.setScbPubDate(i.getPubDate().orElse("")); return i; })
                    .map(RssFeedTweet::createScbTweet)
                    .forEach(twitter::publishTweet);
        }
        catch (IOException e) {
            e.printStackTrace();
        }

    }


    private void checkEkobrottsmyndighetenRssFeed(RssFeed lastPublished, RssFeed next) {
        SimpleDateFormat formatter = new SimpleDateFormat(DATE_TIME_PUBDATE_GMT_FORMAT);

        try {
            rssReader.read(RSS_FEED_EKOBROTTSMYNDIGHETEN)
                    .filter(i -> filterPubDate(formatter, i.getPubDate().orElse(""), lastPublished.getEkobrottsmyndighetenPubDate()))
                    .filter(RssFeedFilter::filterContentEkobrottsmyndigheten)
                    .sorted(this::sortByPublicationDateGMT)
                    .map(i -> { next.setEkobrottsmyndighetenPubDate(i.getPubDate().orElse("")); return i; })
                    .map(RssFeedTweet::createEkobrottsmyndighetenTweet)
                    .forEach(twitter::publishTweet);
        }
        catch (IOException e) {
            e.printStackTrace();
        }

    }


    private void checkVeckansAffarerRssFeed(RssFeed lastPublished, RssFeed next) {
        SimpleDateFormat formatter = new SimpleDateFormat(DATE_TIME_PUBDATE_GMT_FORMAT);

        try {
            rssReader.read(RSS_FEED_VECKANS_AFFARER)
                    .filter(i -> filterPubDate(formatter, i.getPubDate().orElse(""), lastPublished.getVeckansAffarerPubDate()))
                    .filter(RssFeedFilter::filterContentVeckansAffarer)
                    .sorted(this::sortByPublicationDateGMT)
                    .map(i -> { next.setVeckansAffarerPubDate(i.getPubDate().orElse("")); return i; })
                    .map(RssFeedTweet::createVeckansAffarerTweet)
                    .forEach(twitter::publishTweet);
        }
        catch (IOException e) {
            e.printStackTrace();
        }

    }


    private void checkRealtidRssFeed(RssFeed lastPublished, RssFeed next) {
        SimpleDateFormat formatter = new SimpleDateFormat(DATE_TIME_PUBDATE_FORMAT);

        try {
            rssReader.read(RSS_FEED_REALTID)
                    .filter(i -> filterPubDate(formatter, i.getPubDate().orElse(""), lastPublished.getRealtidPubDate()))
                    .filter(RssFeedFilter::filterContentRealtid)
                    .sorted(this::sortByPublicationDateGMT)
                    .map(i -> { next.setRealtidPubDate(i.getPubDate().orElse("")); return i; })
                    .map(RssFeedTweet::createRealtidTweet)
                    .forEach(twitter::publishTweet);
        }
        catch (IOException e) {
            e.printStackTrace();
        }

    }


    private void checkPlaceraRssFeed(RssFeed lastPublished, RssFeed next) {
        SimpleDateFormat formatter = new SimpleDateFormat(DATE_TIME_PUBDATE_FORMAT);

        try {
            rssReader.read(RSS_FEED_PLACERA)
                    .filter(i -> filterPubDate(formatter, i.getPubDate().orElse(""), lastPublished.getPlaceraPubDate()))
                    .filter(RssFeedFilter::filterContentPlacera)
                    .sorted(this::sortByPublicationDate)
                    .map(i -> { next.setPlaceraPubDate(i.getPubDate().orElse("")); return i; })
                    .map(RssFeedTweet::createPlaceraTweet)
                    .forEach(twitter::publishTweet);
        }
        catch (IOException e) {
            e.printStackTrace();
        }

    }


    private void checkBreakitRssFeed(RssFeed lastPublished, RssFeed next) {
        SimpleDateFormat formatter = new SimpleDateFormat(DATE_TIME_PUBDATE_FORMAT);

        try {
            rssReader.read(RSS_FEED_BREAKIT)
                    .filter(i -> filterPubDate(formatter, i.getPubDate().orElse(""), lastPublished.getBreakitPubDate()))
                    .filter(RssFeedFilter::filterContentBreakit)
                    .sorted(this::sortByPublicationDate)
                    .map(i -> { next.setBreakitPubDate(i.getPubDate().orElse("")); return i; })
                    .map(RssFeedTweet::createBreakitTweet)
                    .forEach(twitter::publishTweet);
        }
        catch (IOException e) {
            e.printStackTrace();
        }

    }


    private void checkAffarsvarldenRssFeed(RssFeed lastPublished, RssFeed next) {
        SimpleDateFormat formatter = new SimpleDateFormat(DATE_TIME_PUBDATE_FORMAT);

        try {
            rssReader.read(RSS_FEED_AFFARSVARLDEN)
                    .filter(i -> filterPubDate(formatter, i.getPubDate().orElse(""), lastPublished.getAffarsvarldenPubDate()))
                    .filter(RssFeedFilter::filterContentAffarsvarlden)
                    .sorted(this::sortByPublicationDate)
                    .map(i -> { next.setAffarsvarldenPubDate(i.getPubDate().orElse("")); return i; })
                    .map(RssFeedTweet::createAffarsvarldenTweet)
                    .forEach(twitter::publishTweet);
        }
        catch (IOException e) {
            e.printStackTrace();
        }

    }


    private boolean filterPubDate(SimpleDateFormat formatter, String pubDate, String lastPublished) {
        boolean filter;

        try {
            long pubDateTime = formatter.parse(pubDate).getTime();
            long lastPublishedDateTime = formatter.parse(lastPublished).getTime();
            filter = Long.compare(pubDateTime, lastPublishedDateTime) > 0;
        }
        catch (ParseException e) {
            e.printStackTrace();
            filter = false;
        }

        return filter;
    }


    private RssFeed getLastPublishedRssFeed() {
        RssFeed lastPublished = null;

        try {
            lastPublished = repository.findByRssFeedId("1").blockLast(Duration.ofSeconds(10));
        }
        catch (Exception e) { }

        if (lastPublished == null) {
            Calendar today = Calendar.getInstance(TimeZone.getTimeZone(TIME_ZONE));
            //today.add(Calendar.DAY_OF_YEAR, -1);
            //today.set(Calendar.HOUR, -10);
            //today.set(Calendar.MINUTE, 0);
            //today.set(Calendar.SECOND, 0);

            SimpleDateFormat formatter1 = new SimpleDateFormat(DATE_TIME_PUBDATE_FORMAT);
            SimpleDateFormat formatter2 = new SimpleDateFormat(DATE_TIME_PUBDATE_GMT_FORMAT);
            String date1 = formatter1.format(today.getTime());
            String date2 = formatter2.format(today.getTime());

            lastPublished = new RssFeed("1", "1", date1, date1, date1, date2, date2, date2, date1, date1, date1, date1, "");
        }

        return lastPublished;
    }


    private String now() {
        SimpleDateFormat formatter = new SimpleDateFormat(DATE_TIME_FORMAT);
        Calendar now = Calendar.getInstance(TimeZone.getTimeZone(TIME_ZONE));

        return formatter.format(now.getTime());
    }


    private int sortByPublicationDate(Item o1, Item o2) {
        SimpleDateFormat formatter = new SimpleDateFormat(DATE_TIME_PUBDATE_FORMAT);

        try {
            long dateTime1 = formatter.parse(o1.getPubDate().orElse("")).getTime();
            long dateTime2 = formatter.parse(o2.getPubDate().orElse("")).getTime();

            return Long.compare(dateTime1, dateTime2);
        }
        catch (ParseException e) {
            e.printStackTrace();

        }

        return 0;
    }


    private int sortByPublicationDateGMT(Item o1, Item o2) {
        SimpleDateFormat formatter = new SimpleDateFormat(DATE_TIME_PUBDATE_GMT_FORMAT);

        try {
            long dateTime1 = formatter.parse(o1.getPubDate().orElse("")).getTime();
            long dateTime2 = formatter.parse(o2.getPubDate().orElse("")).getTime();

            return Long.compare(dateTime1, dateTime2);
        }
        catch (ParseException e) {
            e.printStackTrace();
        }

        return 0;
    }

}
