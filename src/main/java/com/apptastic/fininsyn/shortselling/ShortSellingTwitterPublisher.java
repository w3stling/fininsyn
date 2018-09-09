package com.apptastic.fininsyn.shortselling;

import com.apptastic.blankningsregistret.Blankningsregistret;
import com.apptastic.blankningsregistret.NetShortPosition;
import com.apptastic.fininsyn.TwitterPublisher;
import com.apptastic.fininsyn.model.ShortSelling;
import com.apptastic.fininsyn.repo.ShortSellingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;


@Component
public class ShortSellingTwitterPublisher {
    private static final String DATE_FORMAT = "yyyy-MM-dd";
    private static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private static final String TIME_ZONE = "Europe/Stockholm";
    @Autowired
    Blankningsregistret register;
    @Autowired
    ShortSellingRepository repository;
    @Autowired
    TwitterPublisher twitter;


    @Scheduled(initialDelay = 20000, fixedRate = 900000)
    public void checkShortSellingPositions() {
        Logger logger = Logger.getLogger("com.apptastic.fininsyn");

        if (logger.isLoggable(Level.INFO))
            logger.log(Level.INFO, "Checking short selling positions");


        ShortSelling last = getLastShortSelling();
        ShortSelling next = new ShortSelling(last);

        try {
            register.search()
                    .filter(ShortSellingFilter::historyLimitFilter)
                    .collect(Collectors.groupingBy(this::groupShortSellingBy, TreeMap::new, toList()))
                    .values().stream()
                    .sorted(this::sortByPublicationDate)
                    .filter(t -> ShortSellingFilter.publishedDateFilter(last.getPublicationDate(), t.get(0).getPublicationDate()))
                    .map(t -> { next.setPublicationDate(t.get(0).getPublicationDate()); return t; })
                    .map(ShortSellingTweet::create)
                    .filter(TwitterPublisher::filterTweetLength)
                    .forEach(twitter::publishTweet);
        }
        catch (Exception e) {
            Logger warningLogger = Logger.getLogger("com.apptastic.fininsyn");

            if (warningLogger.isLoggable(Level.WARNING))
                warningLogger.log(Level.WARNING, "Check short selling positions failed. ", e);
        }

        next.setLastAttempt(now());
        repository.save(next).subscribe();
    }


    private String groupShortSellingBy(NetShortPosition position) {
        return position.getIsin() + position.getPositionHolder();
    }


    private int sortByPublicationDate(List<NetShortPosition> o1, List<NetShortPosition> o2) {
        Comparator<NetShortPosition> comparator = Comparator.comparing(NetShortPosition::getPublicationDate)
            .thenComparing(NetShortPosition::getPositionDate)
            .thenComparing(NetShortPosition::getIssuer)
            .thenComparing(NetShortPosition::getPositionHolder);

        return comparator.compare(o1.get(0), o2.get(0));
    }


    private ShortSelling getLastShortSelling() {
        ShortSelling shortSelling = null;

        try {
            shortSelling = repository.findByTransactionId("1").blockLast(Duration.ofSeconds(10));
        }
        catch (Exception e) { }

        if (shortSelling == null) {
            Calendar today = Calendar.getInstance(TimeZone.getTimeZone(TIME_ZONE));
            //today.add(Calendar.DAY_OF_YEAR, -1);

            SimpleDateFormat formatter = new SimpleDateFormat(DATE_FORMAT);
            String date = formatter.format(today.getTime());

            shortSelling = new ShortSelling("1", "1", date, "");
        }

        return shortSelling;
    }


    private String now() {
        SimpleDateFormat formatter = new SimpleDateFormat(DATE_TIME_FORMAT);
        Calendar now = Calendar.getInstance(TimeZone.getTimeZone(TIME_ZONE));

        return formatter.format(now.getTime());
    }
}
