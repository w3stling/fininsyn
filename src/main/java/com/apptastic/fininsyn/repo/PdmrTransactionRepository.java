package com.apptastic.fininsyn.repo;

import com.apptastic.fininsyn.model.PdmrTransaction;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;


@Repository
public interface PdmrTransactionRepository extends ReactiveMongoRepository<PdmrTransaction, String> {
    /**
     * Get the date for the last published PDMR transaction.
     *
     * @return date
     */
    Flux<PdmrTransaction> findByTransactionId(String Id);
}
