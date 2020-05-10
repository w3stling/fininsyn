package com.apptastic.fininsyn.shortselling;

import com.apptastic.blankningsregistret.Blankningsregistret;
import com.apptastic.blankningsregistret.NetShortPosition;
import com.apptastic.fininsyn.TwitterPublisher;
import com.apptastic.fininsyn.model.ShortSelling;
import com.apptastic.fininsyn.repo.ShortSellingRepository;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

@Component
public class ShortSellingTwitterPublisher {
    private static final int FILTER_OLDER_THEN_DAYS = 10;
    private static final String DATE_FORMAT = "yyyy-MM-dd";
    private static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    static final String TIME_ZONE = "Europe/Stockholm";
    @Autowired
    Blankningsregistret register;
    @Autowired
    ShortSellingRepository repository;
    @Autowired
    TwitterPublisher twitter;


    //@Scheduled(initialDelay = 0, fixedRate = 900000)
    @Scheduled(cron = "0 40 15 * * ?", zone = TIME_ZONE)
    public void checkShortSellingPositions() {
        Logger logger = Logger.getLogger("com.apptastic.fininsyn");

        if (logger.isLoggable(Level.INFO))
            logger.log(Level.INFO, "Checking short selling positions");

        ShortSelling last = getLastShortSelling();
        ShortSelling next = new ShortSelling(last);

        String now = nowDateTime();
        final String newPublicationDate = now.substring(0, 10);

        if (!newPublicationDate.equals(last.getPublicationDate())) {
            try {
                final Map<String, Pair<NetShortPosition, NetShortPosition>> positionsMap = new HashMap<>();

                register.search(toDate(now), 5)
                        .filter(ShortSellingFilter::badPositions)
                        .filter(ShortSellingFilter::historyLimitFilter)
                        .forEach(p -> {
                            String key = toKey(p);
                            Pair<NetShortPosition, NetShortPosition> position = positionsMap.get(key);
                            if (position == null) {
                                positionsMap.put(key, Pair.of(p, null));
                            } else if (position.getRight() == null) {
                                positionsMap.put(key, Pair.of(position.getLeft(), p));
                            }
                        });

                List<Pair<NetShortPosition, NetShortPosition>> positions = new LinkedList<>(positionsMap.values());
                positions.sort((a, b) -> {
                    int value = b.getLeft().compareTo(a.getLeft());
                    if (value == 0) {
                        value = a.getLeft().getIssuer().compareTo(b.getLeft().getIssuer());
                    }
                    if (value == 0) {
                        value = a.getLeft().getPositionHolder().compareTo(b.getLeft().getPositionHolder());
                    }
                    return value;
                });

                positions.stream()
                        .filter(t -> ShortSellingFilter.positionDateFilter(filterPositionDate(), t.getLeft().getPositionDate()))
                        .filter(ShortSellingFilter::positionChange)
                        .limit(25)
                        .peek(t -> next.setPublicationDate(newPublicationDate))
                        .sorted(Comparator.comparing(Pair::getLeft))
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

    private static String toKey(NetShortPosition position) {
        return position.getIsin() + position.getPositionHolder();
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

    private LocalDate filterPositionDate() {
        /*
        SimpleDateFormat formatter = new SimpleDateFormat(DATE_FORMAT);
        Calendar date = Calendar.getInstance(TimeZone.getTimeZone(TIME_ZONE));
        date.add(Calendar.DAY_OF_YEAR, FILTER_OLDER_THEN_DAYS);

        return formatter.format(date.getTime());
        */
        LocalDate date = LocalDate.now(ZoneId.of(TIME_ZONE));
        return date.minusDays(FILTER_OLDER_THEN_DAYS);
    }

    private LocalDate toDate(String date) throws ParseException {
        if (date.length() > 10) {
            date = date.substring(0, 10);
        }
        return LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE);
    }
}
