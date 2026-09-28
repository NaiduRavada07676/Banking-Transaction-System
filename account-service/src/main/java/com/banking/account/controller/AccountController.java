package com.banking.account.controller;

import com.banking.account.entity.Account;
import com.banking.account.service.AccountService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @PostMapping("/create")
    public ResponseEntity<Account> createAccount(@RequestHeader("X-UserId") Long userId) {
        return ResponseEntity.ok(accountService.createAccount(userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Account> getAccount(@PathVariable Long id) {
        return ResponseEntity.ok(accountService.getAccount(id));
    }

    @GetMapping("/balance/{id}")
    public ResponseEntity<BigDecimal> getBalance(@PathVariable Long id) {
        return ResponseEntity.ok(accountService.getAccount(id).getBalance());
    }
    

    @PutMapping("/{id}/balance")
    public ResponseEntity<Account> updateBalance(@PathVariable Long id, @RequestBody UpdateBalanceRequest request) {
        return ResponseEntity.ok(accountService.updateBalance(id, request.getNewBalance()));
    }

    @PutMapping("/{id}/freeze")
    public ResponseEntity<Account> freezeAccount(@PathVariable Long id) {
        return ResponseEntity.ok(accountService.freezeAccount(id));
    }

    @Data
    public static class UpdateBalanceRequest {
        private BigDecimal newBalance;
    }
}
