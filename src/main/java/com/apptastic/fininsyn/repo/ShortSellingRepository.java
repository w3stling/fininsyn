package com.apptastic.fininsyn.repo;

import com.apptastic.fininsyn.model.ShortSelling;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;


@Repository
public interface ShortSellingRepository extends ReactiveMongoRepository<ShortSelling, String> {
    /**
     * Get the date for the last published PDMR transaction.
     *
     * @return date
     */
    Flux<ShortSelling> findByTransactionId(String Id);
}
