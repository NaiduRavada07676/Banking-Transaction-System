package com.banking.account.service;

import com.banking.account.entity.Account;
import com.banking.account.repository.AccountRepository;
import com.banking.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;

    public Account createAccount(Long userId) {
        String accNum = "ACC" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        Account account = Account.builder()
                .userId(userId)
                .accountNumber(accNum)
                .balance(BigDecimal.ZERO)
                .status("ACTIVE")
                .build();
        return accountRepository.save(account);
    }

    @Cacheable(value = "accounts", key = "#id")
    public Account getAccount(Long id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Account not found", HttpStatus.NOT_FOUND));
    }

    @CachePut(value = "accounts", key = "#id")
    public Account updateBalance(Long id, BigDecimal newBalance) {
        Account account = getAccount(id);
        account.setBalance(newBalance);
        return accountRepository.save(account);
    }

    @CachePut(value = "accounts", key = "#id")
    public Account freezeAccount(Long id) {
        Account account = getAccount(id);
        account.setStatus("FROZEN");
        return accountRepository.save(account);
    }
}
