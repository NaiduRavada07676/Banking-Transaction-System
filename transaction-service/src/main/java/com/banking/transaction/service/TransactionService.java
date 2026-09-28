package com.banking.transaction.service;

import com.banking.common.exception.BusinessException;
import com.banking.transaction.client.AccountServiceClient;
import com.banking.transaction.entity.LedgerAccount;
import com.banking.transaction.entity.TransactionRecord;
import com.banking.transaction.repository.LedgerAccountRepository;
import com.banking.transaction.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final LedgerAccountRepository ledgerRepository;
    private final TransactionRepository transactionRepository;
    private final AccountServiceClient accountClient;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Transactional
    public TransactionRecord transfer(Long sourceId, Long targetId, BigDecimal amount, String idempotencyKey) {
        if (idempotencyKey != null && transactionRepository.existsByIdempotencyKey(idempotencyKey)) {
            throw new BusinessException("Duplicate transaction request", HttpStatus.CONFLICT);
        }


        LedgerAccount first = sourceId < targetId ? getLedgerForUpdate(sourceId) : getLedgerForUpdate(targetId);
        LedgerAccount second = sourceId < targetId ? getLedgerForUpdate(targetId) : getLedgerForUpdate(sourceId);

        LedgerAccount source = sourceId.equals(first.getAccountId()) ? first : second;
        LedgerAccount target = targetId.equals(first.getAccountId()) ? first : second;

        if (source.getBalance().compareTo(amount) < 0) {
            throw new BusinessException("Insufficient balance", HttpStatus.BAD_REQUEST);
        }


        source.setBalance(source.getBalance().subtract(amount));
        target.setBalance(target.getBalance().add(amount));

        ledgerRepository.save(source);
        ledgerRepository.save(target);


        accountClient.updateBalance(sourceId, new AccountServiceClient.UpdateBalanceRequest(source.getBalance()));
        accountClient.updateBalance(targetId, new AccountServiceClient.UpdateBalanceRequest(target.getBalance()));


        TransactionRecord record = TransactionRecord.builder()
                .sourceAccountId(sourceId)
                .targetAccountId(targetId)
                .amount(amount)
                .type("TRANSFER")
                .status("SUCCESS")
                .timestamp(LocalDateTime.now())
                .idempotencyKey(idempotencyKey)
                .build();
        
        transactionRepository.save(record);


        kafkaTemplate.send("transaction-events", record);

        return record;
    }
    
    @Transactional
    public void initializeLedger(Long accountId) {
        if (!ledgerRepository.existsById(accountId)) {
            ledgerRepository.save(new LedgerAccount(accountId, BigDecimal.ZERO));
        }
    }

    private LedgerAccount getLedgerForUpdate(Long accountId) {
        return ledgerRepository.findByIdForUpdate(accountId)
                .orElseThrow(() -> new BusinessException("Account ledger not found", HttpStatus.NOT_FOUND));
    }
}
