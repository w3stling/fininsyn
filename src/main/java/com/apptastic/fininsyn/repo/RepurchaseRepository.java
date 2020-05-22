package com.apptastic.fininsyn.repo;

import com.apptastic.fininsyn.model.RepurchaseTransaction;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;


@Repository
public interface RepurchaseRepository extends ReactiveMongoRepository<RepurchaseTransaction, String> {
    /**
     * Get the last repurchase transactions.
     *
     * @return date
     */
    Flux<RepurchaseTransaction> findByTransactionId(String Id);
}
