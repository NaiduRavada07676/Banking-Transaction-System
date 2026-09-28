package com.banking.audit.listener;

import com.banking.audit.entity.AuditLog;
import com.banking.audit.repository.AuditLogRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditListener {

    private final AuditLogRepository auditLogRepository;

    @KafkaListener(topics = "transaction-events", groupId = "audit-group")
    public void consumeTransactionEvent(TransactionEvent event) {
        log.info("Auditing transaction event: {}", event.getId());
        
        AuditLog auditLog = AuditLog.builder()
                .action("TRANSACTION_CREATED")
                .details(String.format("Transfer of %s from account %d to %d (Type: %s)", 
                        event.getAmount(), event.getSourceAccountId(), event.getTargetAccountId(), event.getType()))
                .timestamp(LocalDateTime.now())
                .build();
                
        auditLogRepository.save(auditLog);
    }

    @KafkaListener(topics = "user-events", groupId = "audit-group")
    public void consumeUserEvent(String eventDetails) {
        AuditLog auditLog = AuditLog.builder()
                .action("USER_ACTION")
                .details(eventDetails)
                .timestamp(LocalDateTime.now())
                .build();
        auditLogRepository.save(auditLog);
    }

    @Data
    public static class TransactionEvent {
        private Long id;
        private Long sourceAccountId;
        private Long targetAccountId;
        private BigDecimal amount;
        private String type;
    }
}
