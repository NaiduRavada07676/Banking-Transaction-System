package com.banking.transaction.controller;

import com.banking.transaction.entity.TransactionRecord;
import com.banking.transaction.service.TransactionService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping("/transfer")
    public ResponseEntity<TransactionRecord> transfer(
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @RequestBody TransferRequest request) {
        return ResponseEntity.ok(transactionService.transfer(
                request.getSourceAccountId(), 
                request.getTargetAccountId(), 
                request.getAmount(), 
                idempotencyKey
        ));
    }
    
    @PostMapping("/ledger/{accountId}")
    public ResponseEntity<String> initLedger(@PathVariable Long accountId) {
        transactionService.initializeLedger(accountId);
        return ResponseEntity.ok("Ledger Initialized");
    }

    @Data
    public static class TransferRequest {
        private Long sourceAccountId;
        private Long targetAccountId;
        private BigDecimal amount;
    }
}
