package com.apptastic.fininsyn;

import com.apptastic.fininsyn.utils.TwitterUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import twitter4j.*;
import twitter4j.conf.Configuration;

import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;


@Component
public class TwitterPublisher {
    @Autowired
    Configuration config;
    private ReentrantLock publishLock;


    public TwitterPublisher() {
        publishLock = new ReentrantLock();
    }

    private Twitter getTwitterInstance() {
        TwitterFactory tf = new TwitterFactory(config);
        Twitter twitter = tf.getInstance();
        return twitter;
    }

    public static boolean filterTweetLength(String tweet) {
        if (tweet == null || tweet.length() <= 2) {
            return false;
        }

        if (tweet.length() > TwitterUtil.TWEET_MAX_LENGTH) {
            java.util.logging.Logger logger = Logger.getLogger("com.apptastic.fininsyn");

            if (logger.isLoggable(Level.WARNING))
                logger.log(Level.WARNING, "Tweet to long. Maximum tweet length is 280 this tweet is " + tweet.length());

            return false;
        }

        return true;
    }

    public void publishTweet(String tweet) {
        java.util.logging.Logger logger = Logger.getLogger("com.apptastic.fininsyn");

        try {
            if (logger.isLoggable(Level.INFO))
                logger.info("Tweet: " + tweet);

            publishLock.lock();


            Twitter twitter = getTwitterInstance();
            StatusUpdate statusUpdate = new StatusUpdate(tweet);
            Status status = twitter.updateStatus(statusUpdate);
        }
        catch (TwitterException e) {
            if (logger.isLoggable(Level.WARNING))
                logger.log(Level.WARNING, "Failed to send tweet: ", e);
        }
        finally {
            publishLock.unlock();
        }
    }
}
