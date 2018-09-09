package com.apptastic.fininsyn.pdmr;

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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

@Component
public class PdmrTransactionTwitterPublisher {
    private static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private static final String TIME_ZONE = "Europe/Stockholm";
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
        Date fromDate = getFromDate(last);
        Date toDate = new Date();

        try {
            TransactionQuery query = TransactionQueryBuilder.publications(fromDate, toDate).build();
            insynsregistret.search(query)
                .filter(t -> PdmrTransactionFilter.transactionFilter(last.getPublicationDate(), t.getPublicationDate()))
                .filter(PdmrTransactionFilter::transactionFilter)
                .collect(Collectors.groupingBy(this::groupTransactionBy, TreeMap::new, toList()))
                .values().stream()
                .filter(PdmrTransactionFilter::transactionAmountFilter)
                .map(t -> { next.setPublicationDate(t.get(0).getPublicationDate()); return t; })
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
        return transaction.getPublicationDate() + transaction.getIsin() + transaction.getNatureOfTransaction() + transaction.getLeiCode();
    }


    private PdmrTransaction getLastPublishedPdmrTransaction() {
        PdmrTransaction lastPublished = null;

        try {
            lastPublished = repository.findByTransactionId("1").blockLast(Duration.ofSeconds(10));
        }
        catch (Exception e) { }

        if (lastPublished == null) {
            Calendar today = Calendar.getInstance(TimeZone.getTimeZone(TIME_ZONE));
            //today.add(Calendar.DAY_OF_YEAR, -1);
            //today.set(Calendar.HOUR, 0);
            //today.set(Calendar.MINUTE, 0);
            //today.set(Calendar.SECOND, 0);

            SimpleDateFormat formatter = new SimpleDateFormat(DATE_TIME_FORMAT);
            String date = formatter.format(today.getTime());

            lastPublished = new PdmrTransaction("1", "1", date, "");
        }

        return lastPublished;
    }


    private Date getFromDate(PdmrTransaction transaction) {
        Date fromDate;
        SimpleDateFormat formatter = new SimpleDateFormat(DATE_TIME_FORMAT);

        try {
            fromDate = formatter.parse(transaction.getPublicationDate());
        }
        catch (ParseException e) {
            Calendar now = Calendar.getInstance(TimeZone.getTimeZone(TIME_ZONE));
            fromDate = now.getTime();
        }

        return fromDate;
    }


    private String now() {
        SimpleDateFormat formatter = new SimpleDateFormat(DATE_TIME_FORMAT);
        Calendar now = Calendar.getInstance(TimeZone.getTimeZone(TIME_ZONE));

        return formatter.format(now.getTime());
    }
}
