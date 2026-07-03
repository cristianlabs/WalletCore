package br.com.User.walletcore.dtos;

import java.math.BigDecimal;
import java.util.List;

public record MonthlyReportResponse(
        int year,
        int month,
        BigDecimal totalIncome,
        BigDecimal totalExpenses,
        BigDecimal savings,
        List<CategoryExpenseResponse> expensesByCategory
) {
}
