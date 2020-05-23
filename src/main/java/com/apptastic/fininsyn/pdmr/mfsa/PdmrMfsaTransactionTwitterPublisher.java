package com.apptastic.fininsyn.pdmr.mfsa;

import com.apptastic.fininsyn.TwitterPublisher;
import com.apptastic.fininsyn.model.PdmrMfsaTransaction;
import com.apptastic.fininsyn.repo.PdmrMfsaTransactionRepository;
import com.apptastic.mfsapdmr.PdmrRegistry;
import com.apptastic.mfsapdmr.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

@Component
public class PdmrMfsaTransactionTwitterPublisher {
    private static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private static final String TIME_ZONE = "Europe/Stockholm";
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(DATE_TIME_FORMAT);
    @Autowired
    PdmrRegistry pdmrRegistry;
    @Autowired
    PdmrMfsaTransactionRepository repository;
    @Autowired
    TwitterPublisher twitter;


    @Scheduled(initialDelay = 500, fixedRate = 120000)
    public void checkPdmrMfsaTransactions() {
        try {
            PdmrMfsaTransaction lastPublished = getLastPublishedTransactions();

            List<Transaction> transactions = pdmrRegistry.getTransactions()
                                                         .filter(PdmrMfsaTransactionFilter::sweden)
                                                         .filter(PdmrMfsaTransactionFilter::natureOfTransaction)
                                                         .filter(PdmrMfsaTransactionFilter::dateTime)
                                                         .sorted()
                                                         .collect(Collectors.toList());

            if (!lastPublished.getTransactions().isEmpty()) {
                transactions.stream()
                            .filter(t -> isNewTransaction(lastPublished, t))
                            .map(PdmrMfsaTransactionTweet::create)
                            .filter(TwitterPublisher::filterTweetLength)
                            .forEach(twitter::publishTweet);
            }

            Set<String> keys = transactions.stream()
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

    private boolean isNewTransaction(PdmrMfsaTransaction lastPublished, Transaction transaction) {
        return !lastPublished.getTransactions().contains(toKey(transaction));
    }

    private String toKey(Transaction transaction) {
        return transaction.getDate() + "_" + transaction.getIssuer() + "_" + transaction.getPdmr() + "_" +
               transaction.getNatureOfTransaction() + "_" + transaction.getVolume() + "_" + transaction.getPrice();
    }

    private LocalDateTime nowDateTime() {
        return ZonedDateTime.now(ZoneId.of(TIME_ZONE)).toLocalDateTime();
    }

    private PdmrMfsaTransaction getLastPublishedTransactions() {
        PdmrMfsaTransaction lastPublished = null;

        try {
            lastPublished = repository.findByTransactionId("1").blockLast(Duration.ofSeconds(10));
        }
        catch (Exception e) { }

        if (lastPublished == null) {
            lastPublished = new PdmrMfsaTransaction("1", "1", new TreeSet<>(), "");
        }

        return lastPublished;
    }
}
