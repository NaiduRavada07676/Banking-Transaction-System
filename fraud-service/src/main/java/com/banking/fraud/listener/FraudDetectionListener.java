package com.banking.fraud.listener;

import com.banking.fraud.entity.FraudAlert;
import com.banking.fraud.repository.FraudAlertRepository;
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
public class FraudDetectionListener {

    private final FraudAlertRepository fraudAlertRepository;
    

    private static final BigDecimal FRAUD_LIMIT = new BigDecimal("100000");

    @KafkaListener(topics = "transaction-events", groupId = "fraud-group")
    public void consumeTransactionEvent(TransactionEvent event) {
        log.info("Received transaction event for fraud analysis: {}", event.getId());
        

        if (event.getAmount().compareTo(FRAUD_LIMIT) > 0) {
            triggerAlert(event.getId(), event.getSourceAccountId(), "Amount exceeds 1 Lakh limit");
        }
        

    }

    private void triggerAlert(Long txId, Long accountId, String reason) {
        log.warn("FRAUD ALERT for Account {}: {}", accountId, reason);
        FraudAlert alert = FraudAlert.builder()
                .accountId(accountId)
                .transactionId(txId)
                .reason(reason)
                .timestamp(LocalDateTime.now())
                .build();
        fraudAlertRepository.save(alert);
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
