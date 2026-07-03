package br.com.User.walletcore.dtos;

import br.com.User.walletcore.entities.Transaction;
import br.com.User.walletcore.entities.TransactionType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record TransactionResponse(
        UUID id,
        UUID accountId,
        UUID categoryId,
        TransactionType type,
        BigDecimal amount,
        String description,
        Instant occurredAt,
        Instant createdAt,
        Instant updatedAt
) {

    public static TransactionResponse fromEntity(Transaction transaction) {
        return new TransactionResponse(
                transaction.getId(),
                transaction.getAccount().getId(),
                transaction.getCategory().getId(),
                transaction.getType(),
                transaction.getAmount(),
                transaction.getDescription(),
                transaction.getOccurredAt(),
                transaction.getCreatedAt(),
                transaction.getUpdatedAt()
        );
    }
}
