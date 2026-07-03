package br.com.User.walletcore.dtos;

import br.com.User.walletcore.entities.TransactionType;

import java.math.BigDecimal;
import java.util.UUID;

public record CategoryReportItem(
        UUID categoryId,
        String categoryName,
        TransactionType type,
        BigDecimal total
) {
}
