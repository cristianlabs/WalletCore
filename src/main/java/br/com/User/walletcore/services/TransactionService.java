package br.com.User.walletcore.services;

import br.com.User.walletcore.dtos.CreateTransactionRequest;
import br.com.User.walletcore.dtos.UpdateTransactionRequest;
import br.com.User.walletcore.entities.Account;
import br.com.User.walletcore.entities.Category;
import br.com.User.walletcore.entities.Transaction;
import br.com.User.walletcore.entities.User;
import br.com.User.walletcore.repositories.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
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

        Transaction transaction = new Transaction();
        transaction.setOwner(owner);
        transaction.setAccount(account);
        transaction.setCategory(category);
        transaction.setType(request.type());
        transaction.setAmount(request.amount());
        transaction.setDescription(request.description());
        transaction.setOccurredAt(request.occurredAt() != null ? request.occurredAt() : Instant.now());
        return transactionRepository.save(transaction);
    }

    @Transactional(readOnly = true)
    public List<Transaction> findAll(User owner) {
        return transactionRepository.findAllByOwnerId(owner.getId());
    }

    @Transactional(readOnly = true)
    public Transaction findById(User owner, UUID id) {
        return transactionRepository.findByIdAndOwnerId(id, owner.getId())
                .orElseThrow(() -> new NoSuchElementException("Transaction not found: " + id));
    }

    @Transactional
    public Transaction update(User owner, UUID id, UpdateTransactionRequest request) {
        Transaction transaction = findById(owner, id);
        Account account = accountService.findById(owner, request.accountId());
        Category category = categoryService.findById(owner, request.categoryId());

        transaction.setAccount(account);
        transaction.setCategory(category);
        transaction.setType(request.type());
        transaction.setAmount(request.amount());
        transaction.setDescription(request.description());
        transaction.setOccurredAt(request.occurredAt() != null ? request.occurredAt() : transaction.getOccurredAt());
        return transactionRepository.save(transaction);
    }

    @Transactional
    public void delete(User owner, UUID id) {
        Transaction transaction = findById(owner, id);
        transactionRepository.delete(transaction);
    }
}
