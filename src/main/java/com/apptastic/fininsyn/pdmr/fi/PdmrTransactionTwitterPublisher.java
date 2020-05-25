package com.apptastic.fininsyn.pdmr.fi;

import com.apptastic.fininsyn.TwitterPublisher;
import com.apptastic.fininsyn.model.PdmrTransaction;
import com.apptastic.fininsyn.repo.PdmrTransactionRepository;
import com.apptastic.insynsregistret.Insynsregistret;
import com.apptastic.insynsregistret.Transaction;
import com.apptastic.insynsregistret.TransactionQuery;
import com.apptastic.insynsregistret.TransactionQueryBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.toList;

@Component
public class PdmrTransactionTwitterPublisher {
    private static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private static final String TIME_ZONE = "Europe/Stockholm";
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(DATE_TIME_FORMAT);
    @Autowired
    Insynsregistret insynsregistret;
    @Autowired
    PdmrTransactionRepository repository;
    @Autowired
    TwitterPublisher twitter;


    @Scheduled(initialDelay = 10000, fixedRate = 120000)
    public void checkPdmrTransactions() {
        Logger logger = Logger.getLogger("com.apptastic.fininsyn");

        if (logger.isLoggable(Level.INFO))
            logger.log(Level.INFO, "Checking PDMR transactions");

        PdmrTransaction last = getLastPublishedPdmrTransaction();
        final PdmrTransaction next = new PdmrTransaction(last);
        LocalDate fromDate = getFromDate(last);
        LocalDate toDate = LocalDate.now(ZoneId.of(TIME_ZONE));

        try {
            TransactionQuery query = TransactionQueryBuilder.publications(fromDate, toDate).build();
            insynsregistret.search(query)
                .filter(t -> PdmrTransactionFilter.transactionFilter(last.getPublicationDate(), t.getPublicationDate().format(DATE_TIME_FORMATTER)))
                .filter(PdmrTransactionFilter::transactionFilter)
                .collect(Collectors.groupingBy(this::groupTransactionBy, TreeMap::new, toList()))
                .values().stream()
                .filter(PdmrTransactionFilter::transactionAmountFilter)
                .filter(not(List::isEmpty))
                .peek(t -> next.setPublicationDate(t.get(0).getPublicationDate().format(DATE_TIME_FORMATTER)))
                .map(PdmrTransactionTweet::create)
                .filter(TwitterPublisher::filterTweetLength)
                .forEach(twitter::publishTweet);
        }
        catch (IOException e) {
            Logger warningLogger = Logger.getLogger("com.apptastic.fininsyn");

            if (warningLogger.isLoggable(Level.WARNING))
                warningLogger.log(Level.WARNING, "Check PDMR transactions failed. ", e);
        }

        next.setLastAttempt(now());
        repository.save(next).subscribe();
    }


    private String groupTransactionBy(Transaction transaction) {
        return transaction.getPublicationDate() + transaction.getIsin() + transaction.getNatureOfTransaction() +
                transaction.getLeiCode() + transaction.getInstrumentType() + transaction.getPdmr();
    }


    private PdmrTransaction getLastPublishedPdmrTransaction() {
        PdmrTransaction lastPublished = null;

        try {
            lastPublished = repository.findByTransactionId("1").blockLast(Duration.ofSeconds(10));
        }
        catch (Exception e) { }

        if (lastPublished == null) {
            LocalDateTime today = LocalDateTime.now(ZoneId.of(TIME_ZONE));
            //today = today.minusDays(1);
            //today = today.withHour(0);
            //today = today.withMinute(0);
            //today = today.withSecond(0);

            String date = today.format(DATE_TIME_FORMATTER);
            lastPublished = new PdmrTransaction("1", "1", date, "");
        }

        return lastPublished;
    }


    private LocalDate getFromDate(PdmrTransaction transaction) {
        LocalDate fromDate;

        try {
            fromDate = LocalDate.parse(transaction.getPublicationDate(), DateTimeFormatter.ISO_LOCAL_DATE);
        }
        catch (DateTimeParseException e) {
            fromDate = LocalDate.now(ZoneId.of(TIME_ZONE));
        }

        return fromDate;
    }


    private String now() {
        LocalDateTime today = LocalDateTime.now(ZoneId.of(TIME_ZONE));
        return today.format(DATE_TIME_FORMATTER);
    }
}
