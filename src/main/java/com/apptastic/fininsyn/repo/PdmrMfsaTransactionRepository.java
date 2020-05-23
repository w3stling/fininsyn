package com.apptastic.fininsyn.repo;

import com.apptastic.fininsyn.model.PdmrMfsaTransaction;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;


@Repository
public interface PdmrMfsaTransactionRepository extends ReactiveMongoRepository<PdmrMfsaTransaction, String> {
    /**
     * Get the date for the last published PDMR transaction.
     *
     * @return date
     */
    Flux<PdmrMfsaTransaction> findByTransactionId(String Id);
}
