package br.com.User.walletcore.services;

import br.com.User.walletcore.dtos.CategoryExpenseResponse;
import br.com.User.walletcore.dtos.CategoryReportItem;
import br.com.User.walletcore.dtos.MonthSummary;
import br.com.User.walletcore.dtos.MonthlyReportResponse;
import br.com.User.walletcore.dtos.YearlyReportResponse;
import br.com.User.walletcore.entities.TransactionType;
import br.com.User.walletcore.entities.User;
import br.com.User.walletcore.repositories.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.IntStream;

@Service
public class ReportService {

    private static final Instant DISTANT_PAST = Instant.EPOCH;
    private static final Instant DISTANT_FUTURE = Instant.parse("9999-12-31T23:59:59Z");

    private final TransactionRepository transactionRepository;

    public ReportService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @Transactional(readOnly = true)
    public MonthlyReportResponse getMonthlyReport(User owner, int year, int month) {
        Instant from = startOfMonth(year, month);
        Instant to = startOf(YearMonth.of(year, month).plusMonths(1));

        BigDecimal income = transactionRepository.sumAmountByOwnerIdAndTypeAndPeriod(owner.getId(), TransactionType.INCOME, from, to);
        BigDecimal expenses = transactionRepository.sumAmountByOwnerIdAndTypeAndPeriod(owner.getId(), TransactionType.EXPENSE, from, to);
        List<CategoryExpenseResponse> byCategory = transactionRepository
                .sumByCategoryAndTypeAndPeriod(owner.getId(), TransactionType.EXPENSE, from, to).stream()
                .map(row -> new CategoryExpenseResponse(row.getCategoryId(), row.getCategoryName(), row.getTotal()))
                .toList();

        return new MonthlyReportResponse(year, month, income, expenses, income.subtract(expenses), byCategory);
    }

    @Transactional(readOnly = true)
    public YearlyReportResponse getYearlyReport(User owner, int year) {
        Instant yearStart = startOf(YearMonth.of(year, 1));
        Instant yearEnd = startOf(YearMonth.of(year + 1, 1));

        BigDecimal totalIncome = transactionRepository.sumAmountByOwnerIdAndTypeAndPeriod(owner.getId(), TransactionType.INCOME, yearStart, yearEnd);
        BigDecimal totalExpenses = transactionRepository.sumAmountByOwnerIdAndTypeAndPeriod(owner.getId(), TransactionType.EXPENSE, yearStart, yearEnd);

        List<MonthSummary> months = IntStream.rangeClosed(1, 12)
                .mapToObj(month -> {
                    Instant from = startOf(YearMonth.of(year, month));
                    Instant to = startOf(YearMonth.of(year, month).plusMonths(1));
                    BigDecimal income = transactionRepository.sumAmountByOwnerIdAndTypeAndPeriod(owner.getId(), TransactionType.INCOME, from, to);
                    BigDecimal expenses = transactionRepository.sumAmountByOwnerIdAndTypeAndPeriod(owner.getId(), TransactionType.EXPENSE, from, to);
                    return new MonthSummary(month, income, expenses, income.subtract(expenses));
                })
                .toList();

        return new YearlyReportResponse(year, totalIncome, totalExpenses, totalIncome.subtract(totalExpenses), months);
    }

    @Transactional(readOnly = true)
    public List<CategoryReportItem> getCategoryReport(User owner, LocalDate from, LocalDate to) {
        Instant fromInstant = from != null ? from.atStartOfDay(ZoneOffset.UTC).toInstant() : DISTANT_PAST;
        Instant toInstant = to != null ? to.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant() : DISTANT_FUTURE;

        return transactionRepository.sumByCategoryAndPeriod(owner.getId(), fromInstant, toInstant).stream()
                .map(row -> new CategoryReportItem(row.getCategoryId(), row.getCategoryName(), row.getType(), row.getTotal()))
                .toList();
    }

    private Instant startOfMonth(int year, int month) {
        return startOf(YearMonth.of(year, month));
    }

    private Instant startOf(YearMonth yearMonth) {
        return yearMonth.atDay(1).atStartOfDay(ZoneOffset.UTC).toInstant();
    }
}
