package br.com.User.walletcore.dtos;

import java.math.BigDecimal;
import java.util.UUID;

public record CategoryExpenseResponse(
        UUID categoryId,
        String categoryName,
        BigDecimal total
) {
}
