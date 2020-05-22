package com.apptastic.fininsyn.repurchase;

import com.apptastic.fininsyn.TwitterPublisher;
import com.apptastic.fininsyn.model.RepurchaseTransaction;
import com.apptastic.fininsyn.repo.RepurchaseRepository;
import com.apptastic.repurchase.Repurchase;
import com.apptastic.repurchase.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

@Component
public class RepurchaseTwitterPublisher {
    private static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private static final String TIME_ZONE = "Europe/Stockholm";
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(DATE_TIME_FORMAT);
    @Autowired
    Repurchase repurchase;
    @Autowired
    RepurchaseRepository repository;
    @Autowired
    TwitterPublisher twitter;

    @Scheduled(initialDelay = 2000, fixedRate = 900000)
    public void checkRepurchaseTransactions() {
        try {
            RepurchaseTransaction lastPublished = getLastPublishedRepurchaseTransactions();
            Set<Transaction> transactions = repurchase.getTransactions(10).collect(Collectors.toSet());

            if (!lastPublished.getTransactions().isEmpty()) {
                transactions.stream()
                            .filter(RepurchaseFilter::quantity)
                            .filter(RepurchaseFilter::type)
                            .filter(t -> !lastPublished.getTransactions().contains(toKey(t)))
                            .map(RepurchaseTweet::create)
                            .filter(TwitterPublisher::filterTweetLength)
                            .forEach(twitter::publishTweet);
            }

            Set<String> keys = transactions.stream()
                                           .filter(RepurchaseFilter::quantity)
                                           .map(this::toKey)
                                           .collect(Collectors.toSet());

            lastPublished.setTransactions(keys);
            String now = nowDateTime().format(DATE_TIME_FORMATTER);
            lastPublished.setLastAttempt(now);
            repository.save(lastPublished).subscribe();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String toKey(Transaction transaction) {
        return transaction.getDate() + "_" + transaction.getCompany() + "_" + transaction.getType() + "_" + transaction.getValue();
    }

    private LocalDateTime nowDateTime() {
        return ZonedDateTime.now(ZoneId.of(TIME_ZONE)).toLocalDateTime();
    }

    private RepurchaseTransaction getLastPublishedRepurchaseTransactions() {
        RepurchaseTransaction lastPublished = null;

        try {
            lastPublished = repository.findByTransactionId("1").blockLast(Duration.ofSeconds(10));
        }
        catch (Exception e) { }

        if (lastPublished == null) {
            lastPublished = new RepurchaseTransaction("1", "1", new TreeSet<>(), "");
        }

        return lastPublished;
    }
}
