package com.banking.transaction.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.math.BigDecimal;

@FeignClient(name = "account-service", url = "http://localhost:8082/accounts")
public interface AccountServiceClient {

    @PutMapping("/{id}/balance")
    void updateBalance(@PathVariable("id") Long id, @RequestBody UpdateBalanceRequest request);

    class UpdateBalanceRequest {
        private BigDecimal newBalance;
        
        public UpdateBalanceRequest(BigDecimal newBalance) { this.newBalance = newBalance; }
        public BigDecimal getNewBalance() { return newBalance; }
        public void setNewBalance(BigDecimal newBalance) { this.newBalance = newBalance; }
    }
}
