package com.banking.transaction.repository;

import com.banking.transaction.entity.LedgerAccount;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface LedgerAccountRepository extends JpaRepository<LedgerAccount, Long> {
    
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT l FROM LedgerAccount l WHERE l.accountId = :accountId")
    Optional<LedgerAccount> findByIdForUpdate(Long accountId);
}
