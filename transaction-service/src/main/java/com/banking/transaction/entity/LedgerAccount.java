package com.banking.transaction.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "ledger_accounts")
public class LedgerAccount {
    @Id
    private Long accountId;

    @Column(nullable = false)
    private BigDecimal balance;
}
