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

        // One round trip for the whole year (rather than one query per month): fetch just
        // (occurredAt, type, amount) and group by UTC month in Java, so the boundary is
        // always UTC regardless of the DB session's timezone setting.
        BigDecimal[] income = new BigDecimal[13];
        BigDecimal[] expenses = new BigDecimal[13];
        for (int i = 1; i <= 12; i++) {
            income[i] = BigDecimal.ZERO;
            expenses[i] = BigDecimal.ZERO;
        }

        for (var row : transactionRepository.findAmountsByOwnerIdAndPeriod(owner.getId(), yearStart, yearEnd)) {
            int month = row.getOccurredAt().atZone(ZoneOffset.UTC).getMonthValue();
            if (row.getType() == TransactionType.INCOME) {
                income[month] = income[month].add(row.getAmount());
            } else {
                expenses[month] = expenses[month].add(row.getAmount());
            }
        }

        List<MonthSummary> months = IntStream.rangeClosed(1, 12)
                .mapToObj(month -> new MonthSummary(month, income[month], expenses[month], income[month].subtract(expenses[month])))
                .toList();

        BigDecimal totalIncome = months.stream().map(MonthSummary::totalIncome).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalExpenses = months.stream().map(MonthSummary::totalExpenses).reduce(BigDecimal.ZERO, BigDecimal::add);

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
