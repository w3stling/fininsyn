package com.apptastic.fininsyn.shortselling;

import com.apptastic.blankningsregistret.Blankningsregistret;
import com.apptastic.blankningsregistret.NetShortPosition;
import com.apptastic.fininsyn.TwitterPublisher;
import com.apptastic.fininsyn.model.ShortSelling;
import com.apptastic.fininsyn.repo.ShortSellingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;


@Component
public class ShortSellingTwitterPublisher {
    private static final int FILTER_OLDER_THEN_DAYS = -10;
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

        String now = nowDateTime();
        String newPublicationDate = now.substring(0, 10);

        if (!newPublicationDate.equals(last.getPublicationDate())) {
            try {
                Stream<NetShortPosition> lastSearch = register.search(toDate(last.getPublicationDate()), 6)
                        .filter(ShortSellingFilter::historyLimitFilter);

                List<NetShortPosition> newSearch = register.search(toDate(now), 6)
                        .filter(ShortSellingFilter::historyLimitFilter)
                        .collect(Collectors.toList());

                Set<String> diff = diffStreams(newSearch.stream(), lastSearch);

                if (diff.size() > 40)
                    return;

                newSearch.stream()
                        .filter(ShortSellingFilter::historyLimitFilter)
                        .filter(p -> diff.contains(p.getPositionHolder() + p.getIsin()))
                        .collect(Collectors.groupingBy(this::groupShortSellingBy, TreeMap::new, toList()))
                        .values().stream()
                        .sorted(this::sortByPositionDate)
                        .filter(t -> ShortSellingFilter.positionDateFilter(filterPositionDate(), t.get(0).getPositionDate()))
                        .map(t -> {
                            next.setPublicationDate(newPublicationDate);
                            return t;
                        })
                        .map(ShortSellingTweet::create)
                        .filter(TwitterPublisher::filterTweetLength)
                        .forEach(twitter::publishTweet);
            } catch (Exception e) {
                Logger warningLogger = Logger.getLogger("com.apptastic.fininsyn");

                if (warningLogger.isLoggable(Level.WARNING))
                    warningLogger.log(Level.WARNING, "Check short selling positions failed. ", e);
            }
        }

        next.setLastAttempt(now);
        repository.save(next).subscribe();
    }


    private String groupShortSellingBy(NetShortPosition position) {
        return position.getIsin() + position.getPositionHolder();
    }


    private int sortByPositionDate(List<NetShortPosition> o1, List<NetShortPosition> o2) {
        Comparator<NetShortPosition> comparator = Comparator.comparing(NetShortPosition::getPositionDate)
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


    private String nowDateTime() {
        SimpleDateFormat formatter = new SimpleDateFormat(DATE_TIME_FORMAT);
        Calendar now = Calendar.getInstance(TimeZone.getTimeZone(TIME_ZONE));

        return formatter.format(now.getTime());
    }

    private String filterPositionDate() {
        SimpleDateFormat formatter = new SimpleDateFormat(DATE_FORMAT);
        Calendar date = Calendar.getInstance(TimeZone.getTimeZone(TIME_ZONE));
        date.add(Calendar.DAY_OF_YEAR, FILTER_OLDER_THEN_DAYS);

        return formatter.format(date.getTime());
    }

    private Date toDate(String date) throws ParseException {
        return new SimpleDateFormat(DATE_FORMAT).parse(date);
    }

    private Set<String> diffStreams(Stream<NetShortPosition> s1, Stream<NetShortPosition> s2) {
        Set<NetShortPosition> b2 = s2.collect(Collectors.toSet());

        return s1.filter(p -> !b2.contains(p))
                 .map(p -> p.getPositionHolder() + p.getIsin())
                 .collect(Collectors.toSet());
    }
}
