package br.com.User.walletcore.dtos;

import java.math.BigDecimal;

public record MonthSummary(
        int month,
        BigDecimal totalIncome,
        BigDecimal totalExpenses,
        BigDecimal savings
) {
}
