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

    // Atomic DB-level increment (balance = balance + delta) so concurrent transactions
    // on the same account can't lose an update by racing on a read-modify-write in Java.
    // clearAutomatically/flushAutomatically keep the persistence context consistent when
    // this runs alongside managed-entity saves in the same transaction (see TransactionService).
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Account a SET a.balance = a.balance + :delta, a.updatedAt = :updatedAt WHERE a.id = :accountId")
    void adjustBalance(@Param("accountId") UUID accountId, @Param("delta") BigDecimal delta, @Param("updatedAt") Instant updatedAt);
}
