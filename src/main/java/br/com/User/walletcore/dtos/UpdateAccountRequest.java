package br.com.User.walletcore.dtos;

import br.com.User.walletcore.entities.AccountType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record UpdateAccountRequest(

        @NotBlank
        String name,

        @NotNull
        BigDecimal balance,

        @NotNull
        AccountType type
) {
}
