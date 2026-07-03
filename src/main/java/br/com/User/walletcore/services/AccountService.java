package br.com.User.walletcore.services;

import br.com.User.walletcore.dtos.CreateAccountRequest;
import br.com.User.walletcore.dtos.UpdateAccountRequest;
import br.com.User.walletcore.entities.Account;
import br.com.User.walletcore.entities.User;
import br.com.User.walletcore.exceptions.InsufficientBalanceException;
import br.com.User.walletcore.repositories.AccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
public class AccountService {

    private final AccountRepository accountRepository;

    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Transactional
    public Account create(User owner, CreateAccountRequest request) {
        Account account = new Account();
        account.setOwner(owner);
        account.setName(request.name());
        account.setBalance(request.balance());
        account.setType(request.type());
        return accountRepository.save(account);
    }

    @Transactional(readOnly = true)
    public List<Account> findAll(User owner) {
        return accountRepository.findAllByOwnerId(owner.getId());
    }

    @Transactional(readOnly = true)
    public Account findById(User owner, UUID id) {
        return accountRepository.findByIdAndOwnerId(id, owner.getId())
                .orElseThrow(() -> new NoSuchElementException("Account not found: " + id));
    }

    @Transactional
    public Account update(User owner, UUID id, UpdateAccountRequest request) {
        Account account = findById(owner, id);
        account.setName(request.name());
        account.setBalance(request.balance());
        account.setType(request.type());
        return accountRepository.save(account);
    }

    @Transactional
    public void delete(User owner, UUID id) {
        Account account = findById(owner, id);
        accountRepository.delete(account);
    }

    @Transactional
    public void adjustBalance(UUID accountId, BigDecimal delta) {
        int updated = accountRepository.adjustBalanceIfSufficient(accountId, delta, Instant.now());
        if (updated == 0) {
            throw new InsufficientBalanceException(accountId);
        }
    }
}
