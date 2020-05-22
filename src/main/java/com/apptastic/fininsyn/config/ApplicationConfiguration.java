package com.apptastic.fininsyn.config;

import com.apptastic.blankningsregistret.Blankningsregistret;
import com.apptastic.fininsyn.model.PdmrTransaction;
import com.apptastic.insynsregistret.Insynsregistret;
import com.apptastic.repurchase.Repurchase;
import com.apptastic.rssreader.RssReader;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.mongodb.config.AbstractReactiveMongoConfiguration;
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories;
import twitter4j.conf.ConfigurationBuilder;


@Configuration
@PropertySource("classpath:fininsyn.properties")
@EnableReactiveMongoRepositories(basePackageClasses = PdmrTransaction.class)
public class ApplicationConfiguration extends AbstractReactiveMongoConfiguration {
    @Value( "${fininsyn.twitter.oauthconsumerkey}" )
    private String oAuthConsumerKey;
    @Value( "${fininsyn.twitter.oauthconsumersecret}" )
    private String oAuthConsumerSecret;
    @Value( "${fininsyn.twitter.oauthaccesstoken}" )
    private String oAuthAccessToken;
    @Value( "${fininsyn.twitter.oauthaccesstokensecret}" )
    private String oAuthAccessTokenSecret;
    @Value( "${fininsyn.mongodb.connectionstring}" )
    private String connectionString;

    @Bean
    public Insynsregistret insynsregistret() {
        return new Insynsregistret();
    }

    @Bean
    public Blankningsregistret blankningsregistret() {
        return new Blankningsregistret();
    }

    @Bean
    public Repurchase repurchase() {
        return new Repurchase();
    }

    @Bean
    public RssReader rssReader() {
        return new RssReader();
    }

    @Bean
    public twitter4j.conf.Configuration twitterConfiguration() {
        ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setOAuthConsumerKey(oAuthConsumerKey)
                .setOAuthConsumerSecret(oAuthConsumerSecret)
                .setOAuthAccessToken(oAuthAccessToken)
                .setOAuthAccessTokenSecret(oAuthAccessTokenSecret)
                .setTweetModeExtended(true);

        twitter4j.conf.Configuration config = cb.build();
        return config;
    }

    @Override
    protected String getDatabaseName() {
        return "fininsyn";
    }

    @Override
    public MongoClient reactiveMongoClient() {
        return  MongoClients.create(connectionString);
    }

}