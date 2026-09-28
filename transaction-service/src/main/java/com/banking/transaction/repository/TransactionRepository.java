package com.banking.transaction.repository;

import com.banking.transaction.entity.TransactionRecord;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<TransactionRecord, Long> {
    boolean existsByIdempotencyKey(String idempotencyKey);
}
