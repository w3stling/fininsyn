package com.apptastic.fininsyn.repo;

import com.apptastic.fininsyn.model.RssFeed;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;


@Repository
public interface RssFeedRepository extends ReactiveMongoRepository<RssFeed, String> {

    /**
     * Get the date for the last published RSS feed item.
     *
     * @return date
     */
    Flux<RssFeed> findByRssFeedId(String Id);
}
