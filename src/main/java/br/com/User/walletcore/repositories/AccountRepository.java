package br.com.User.walletcore.repositories;

import br.com.User.walletcore.entities.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AccountRepository extends JpaRepository<Account, UUID> {

    Optional<Account> findByIdAndOwnerId(UUID id, UUID ownerId);

    List<Account> findAllByOwnerId(UUID ownerId);

    // Atomic DB-level increment (balance = balance + delta), guarded by the same WHERE clause
    // so the negative-balance check can't be bypassed by two concurrent updates racing past a
    // separate read-then-check in Java. Returns 0 rows affected if the account doesn't exist
    // or the update would take the balance below zero.
    // clearAutomatically/flushAutomatically keep the persistence context consistent when this
    // runs alongside managed-entity saves in the same transaction (see TransactionService).
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Account a SET a.balance = a.balance + :delta, a.updatedAt = :updatedAt WHERE a.id = :accountId AND a.balance + :delta >= 0")
    int adjustBalanceIfSufficient(@Param("accountId") UUID accountId, @Param("delta") BigDecimal delta, @Param("updatedAt") Instant updatedAt);
}
