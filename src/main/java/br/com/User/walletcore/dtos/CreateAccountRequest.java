package br.com.User.walletcore.dtos;

import br.com.User.walletcore.entities.AccountType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CreateAccountRequest(

        @NotBlank
        @Size(max = 255)
        String name,

        @NotNull
        @DecimalMin(value = "0.0", inclusive = true)
        @Digits(integer = 17, fraction = 2)
        BigDecimal balance,

        @NotNull
        AccountType type
) {
}
