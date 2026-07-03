package br.com.User.walletcore.dtos;

import br.com.User.walletcore.entities.TransactionType;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record UpdateTransactionRequest(

        @NotNull
        UUID accountId,

        @NotNull
        UUID categoryId,

        @NotNull
        TransactionType type,

        @NotNull
        @Positive
        @Digits(integer = 17, fraction = 2)
        BigDecimal amount,

        @Size(max = 255)
        String description,

        Instant occurredAt
) {
}
