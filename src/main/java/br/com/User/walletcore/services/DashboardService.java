package br.com.User.walletcore.services;

import br.com.User.walletcore.dtos.CategoryExpenseResponse;
import br.com.User.walletcore.dtos.DashboardResponse;
import br.com.User.walletcore.entities.TransactionType;
import br.com.User.walletcore.entities.User;
import br.com.User.walletcore.repositories.AccountRepository;
import br.com.User.walletcore.repositories.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DashboardService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    public DashboardService(AccountRepository accountRepository, TransactionRepository transactionRepository) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    @Transactional(readOnly = true)
    public DashboardResponse getDashboard(User owner) {
        var totalBalance = accountRepository.sumBalanceByOwnerId(owner.getId());
        var totalIncome = transactionRepository.sumAmountByOwnerIdAndType(owner.getId(), TransactionType.INCOME);
        var totalExpenses = transactionRepository.sumAmountByOwnerIdAndType(owner.getId(), TransactionType.EXPENSE);
        var savings = totalIncome.subtract(totalExpenses);

        var expensesByCategory = transactionRepository.sumByCategoryAndType(owner.getId(), TransactionType.EXPENSE).stream()
                .map(row -> new CategoryExpenseResponse(row.getCategoryId(), row.getCategoryName(), row.getTotal()))
                .toList();

        return new DashboardResponse(totalBalance, totalIncome, totalExpenses, savings, expensesByCategory);
    }
}
