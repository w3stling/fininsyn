package com.apptastic.fininsyn.shortselling;

import com.apptastic.blankningsregistret.Blankningsregistret;
import com.apptastic.blankningsregistret.NetShortPosition;
import com.apptastic.fininsyn.TwitterPublisher;
import com.apptastic.fininsyn.model.ShortSelling;
import com.apptastic.fininsyn.repo.ShortSellingRepository;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Component
public class ShortSellingTwitterPublisher {
    private static final String DATE_FORMAT = "yyyy-MM-dd";
    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    static final String TIME_ZONE = "Europe/Stockholm";
    @Autowired
    Blankningsregistret register;
    @Autowired
    ShortSellingRepository repository;
    @Autowired
    TwitterPublisher twitter;


    //@Scheduled(initialDelay = 0, fixedRate = 900000)
    @Scheduled(cron = "0 40 15 * * ?", zone = TIME_ZONE)
    @Scheduled(cron = "0 55 15 * * ?", zone = TIME_ZONE)
    @Scheduled(cron = "0 30 16 * * ?", zone = TIME_ZONE)
    public void checkShortSellingPositions() {
        Logger logger = Logger.getLogger("com.apptastic.fininsyn");

        if (logger.isLoggable(Level.INFO))
            logger.log(Level.INFO, "Checking short selling positions");

        ShortSelling last = getLastShortSelling();
        ShortSelling next = new ShortSelling(last);

        final LocalDate today = nowDate();
        final LocalDate lastPublicationDate = LocalDate.parse(last.getPublicationDate());

        if (today.isAfter(lastPublicationDate)) {
            try {
                final Map<String, Triple<NetShortPosition, NetShortPosition, Integer>> positionsMap = new HashMap<>();
                final HashMap<String, AtomicInteger> positionsPerPositionsHolders = new HashMap<>();

                register.search(today, 5)
                        .filter(ShortSellingFilter::badPositions)
                        .filter(ShortSellingFilter::historyLimitFilter)
                        .forEach(p -> {
                            String key = toKey(p);
                            Triple<NetShortPosition, NetShortPosition, Integer> position = positionsMap.get(key);
                            if (position == null) {
                                positionsMap.put(key, Triple.of(p, null, null));
                            } else if (position.getMiddle() == null) {
                                positionsMap.put(key, Triple.of(position.getLeft(), p, null));
                            }
                            positionsPerPositionsHolders.computeIfAbsent(p.getPositionHolder(), a -> new AtomicInteger(0))
                                                        .incrementAndGet();
                        });

                List<Triple<NetShortPosition, NetShortPosition, Integer>> positions = positionsMap.values().stream()
                    .map(t -> {
                            Integer numberOfTransaction = positionsPerPositionsHolders.getOrDefault(t.getLeft().getPositionHolder(), new AtomicInteger(0)).intValue();
                            return Triple.of(t.getLeft(), t.getMiddle(), numberOfTransaction);
                        })
                    .collect(Collectors.toList());


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
                         .filter(t -> ShortSellingFilter.positionDateFilter(lastPublicationDate, t.getLeft().getPositionDate()))
                         .filter(ShortSellingFilter::positionChange)
                         .limit(25)
                         .peek(t -> {
                            LocalDate lastPublication = LocalDate.parse(next.getPublicationDate());
                            if (t.getLeft().getPositionDate().isAfter(lastPublication)) {
                                next.setPublicationDate(t.getLeft().getPositionDate().format(DateTimeFormatter.ISO_DATE));
                            }
                         })
                         .sorted(Comparator.comparing(Triple::getLeft))
                         .map(ShortSellingTweet::create)
                         .filter(TwitterPublisher::filterTweetLength)
                         .forEach(twitter::publishTweet);
            } catch (Exception e) {
                Logger warningLogger = Logger.getLogger("com.apptastic.fininsyn");

                if (warningLogger.isLoggable(Level.WARNING))
                    warningLogger.log(Level.WARNING, "Check short selling positions failed. ", e);
            }
        }

        String now = nowDateTime().format(DATE_TIME_FORMAT);
        next.setLastAttempt(now);
        repository.save(next).subscribe();
    }

    private static String toKey(NetShortPosition position) {
        return position.getIssuer() + position.getPositionHolder();
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

    private LocalDate nowDate() {
        return ZonedDateTime.now(ZoneId.of(TIME_ZONE)).toLocalDate();
    }

    private LocalDateTime nowDateTime() {
        return ZonedDateTime.now(ZoneId.of(TIME_ZONE)).toLocalDateTime();
    }

}
