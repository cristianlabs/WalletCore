package br.com.User.walletcore.dtos;

import br.com.User.walletcore.entities.Account;
import br.com.User.walletcore.entities.AccountType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record AccountResponse(
        UUID id,
        String name,
        BigDecimal balance,
        AccountType type,
        Instant createdAt,
        Instant updatedAt
) {

    public static AccountResponse fromEntity(Account account) {
        return new AccountResponse(
                account.getId(),
                account.getName(),
                account.getBalance(),
                account.getType(),
                account.getCreatedAt(),
                account.getUpdatedAt()
        );
    }
}
