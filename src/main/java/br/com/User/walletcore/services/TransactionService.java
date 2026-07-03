package br.com.User.walletcore.services;

import br.com.User.walletcore.dtos.CreateTransactionRequest;
import br.com.User.walletcore.dtos.UpdateTransactionRequest;
import br.com.User.walletcore.entities.Account;
import br.com.User.walletcore.entities.Category;
import br.com.User.walletcore.entities.Transaction;
import br.com.User.walletcore.entities.TransactionType;
import br.com.User.walletcore.entities.User;
import br.com.User.walletcore.exceptions.CategoryTypeMismatchException;
import br.com.User.walletcore.repositories.TransactionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountService accountService;
    private final CategoryService categoryService;

    public TransactionService(TransactionRepository transactionRepository, AccountService accountService, CategoryService categoryService) {
        this.transactionRepository = transactionRepository;
        this.accountService = accountService;
        this.categoryService = categoryService;
    }

    @Transactional
    public Transaction create(User owner, CreateTransactionRequest request) {
        Account account = accountService.findById(owner, request.accountId());
        Category category = categoryService.findById(owner, request.categoryId());
        validateCategoryMatchesType(category, request.type());

        Transaction transaction = new Transaction();
        transaction.setOwner(owner);
        transaction.setAccount(account);
        transaction.setCategory(category);
        transaction.setType(request.type());
        transaction.setAmount(request.amount());
        transaction.setDescription(request.description());
        transaction.setOccurredAt(request.occurredAt() != null ? request.occurredAt() : Instant.now());
        Transaction saved = transactionRepository.save(transaction);

        accountService.adjustBalance(account.getId(), signedAmount(request.type(), request.amount()));
        return saved;
    }

    @Transactional(readOnly = true)
    public Page<Transaction> findAll(User owner, Pageable pageable) {
        return transactionRepository.findAllByOwnerId(owner.getId(), pageable);
    }

    @Transactional(readOnly = true)
    public Transaction findById(User owner, UUID id) {
        return transactionRepository.findByIdAndOwnerId(id, owner.getId())
                .orElseThrow(() -> new NoSuchElementException("Transaction not found: " + id));
    }

    @Transactional
    public Transaction update(User owner, UUID id, UpdateTransactionRequest request) {
        Transaction transaction = findById(owner, id);
        UUID previousAccountId = transaction.getAccount().getId();
        BigDecimal previousSignedAmount = signedAmount(transaction.getType(), transaction.getAmount());

        Account account = accountService.findById(owner, request.accountId());
        Category category = categoryService.findById(owner, request.categoryId());
        validateCategoryMatchesType(category, request.type());

        transaction.setAccount(account);
        transaction.setCategory(category);
        transaction.setType(request.type());
        transaction.setAmount(request.amount());
        transaction.setDescription(request.description());
        transaction.setOccurredAt(request.occurredAt() != null ? request.occurredAt() : transaction.getOccurredAt());
        Transaction saved = transactionRepository.save(transaction);

        applyBalanceChanges(
                previousAccountId, previousSignedAmount.negate(),
                account.getId(), signedAmount(request.type(), request.amount())
        );
        return saved;
    }

    // Applies both balance deltas in a consistent account-id order (rather than
    // "old account, then new account") so two concurrent updates moving transactions
    // between the same two accounts in opposite directions can't deadlock on row locks.
    private void applyBalanceChanges(UUID accountIdA, BigDecimal deltaA, UUID accountIdB, BigDecimal deltaB) {
        if (accountIdA.compareTo(accountIdB) <= 0) {
            accountService.adjustBalance(accountIdA, deltaA);
            accountService.adjustBalance(accountIdB, deltaB);
        } else {
            accountService.adjustBalance(accountIdB, deltaB);
            accountService.adjustBalance(accountIdA, deltaA);
        }
    }

    @Transactional
    public void delete(User owner, UUID id) {
        Transaction transaction = findById(owner, id);
        UUID accountId = transaction.getAccount().getId();
        BigDecimal signedAmount = signedAmount(transaction.getType(), transaction.getAmount());

        transactionRepository.delete(transaction);
        accountService.adjustBalance(accountId, signedAmount.negate());
    }

    private BigDecimal signedAmount(TransactionType type, BigDecimal amount) {
        return type == TransactionType.EXPENSE ? amount.negate() : amount;
    }

    private void validateCategoryMatchesType(Category category, TransactionType type) {
        if (!category.getType().name().equals(type.name())) {
            throw new CategoryTypeMismatchException(category.getType(), type);
        }
    }
}
