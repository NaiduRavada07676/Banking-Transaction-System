package com.banking.notification.listener;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@Slf4j
public class NotificationListener {

    @KafkaListener(topics = "transaction-events", groupId = "notification-group")
    public void consumeTransactionEvent(TransactionEvent event) {
        log.info("Sending Email/SMS notification to account {} regarding {} of amount {}", 
                event.getSourceAccountId(), event.getType(), event.getAmount());
        

        sendEmail(event.getSourceAccountId(), "Transaction Successful", "Your transaction of " + event.getAmount() + " was successfully processed.");
    }

    private void sendEmail(Long accountId, String subject, String body) {

        log.info("Email sent - Subject: {}, Body: {}", subject, body);
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
