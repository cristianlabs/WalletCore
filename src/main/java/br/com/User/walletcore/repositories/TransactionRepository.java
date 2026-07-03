package br.com.User.walletcore.repositories;

import br.com.User.walletcore.entities.Transaction;
import br.com.User.walletcore.entities.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    Optional<Transaction> findByIdAndOwnerId(UUID id, UUID ownerId);

    Page<Transaction> findAllByOwnerId(UUID ownerId, Pageable pageable);

    // Aggregated in the DB rather than summed over findAll(...) in Java, so this
    // doesn't degrade as a user's transaction history grows.
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.owner.id = :ownerId AND t.type = :type")
    BigDecimal sumAmountByOwnerIdAndType(@Param("ownerId") UUID ownerId, @Param("type") TransactionType type);

    @Query("""
            SELECT t.category.id AS categoryId, t.category.name AS categoryName, SUM(t.amount) AS total
            FROM Transaction t
            WHERE t.owner.id = :ownerId AND t.type = :type
            GROUP BY t.category.id, t.category.name
            ORDER BY SUM(t.amount) DESC
            """)
    List<CategoryAmountSummary> sumByCategoryAndType(@Param("ownerId") UUID ownerId, @Param("type") TransactionType type);

    @Query("""
            SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t
            WHERE t.owner.id = :ownerId AND t.type = :type AND t.occurredAt >= :from AND t.occurredAt < :to
            """)
    BigDecimal sumAmountByOwnerIdAndTypeAndPeriod(
            @Param("ownerId") UUID ownerId,
            @Param("type") TransactionType type,
            @Param("from") Instant from,
            @Param("to") Instant to
    );

    @Query("""
            SELECT t.category.id AS categoryId, t.category.name AS categoryName, SUM(t.amount) AS total
            FROM Transaction t
            WHERE t.owner.id = :ownerId AND t.type = :type AND t.occurredAt >= :from AND t.occurredAt < :to
            GROUP BY t.category.id, t.category.name
            ORDER BY SUM(t.amount) DESC
            """)
    List<CategoryAmountSummary> sumByCategoryAndTypeAndPeriod(
            @Param("ownerId") UUID ownerId,
            @Param("type") TransactionType type,
            @Param("from") Instant from,
            @Param("to") Instant to
    );

    @Query("""
            SELECT t.category.id AS categoryId, t.category.name AS categoryName, t.type AS type, SUM(t.amount) AS total
            FROM Transaction t
            WHERE t.owner.id = :ownerId AND t.occurredAt >= :from AND t.occurredAt < :to
            GROUP BY t.category.id, t.category.name, t.type
            ORDER BY SUM(t.amount) DESC
            """)
    List<CategoryTypeAmountSummary> sumByCategoryAndPeriod(
            @Param("ownerId") UUID ownerId,
            @Param("from") Instant from,
            @Param("to") Instant to
    );

    interface CategoryAmountSummary {
        UUID getCategoryId();

        String getCategoryName();

        BigDecimal getTotal();
    }

    interface CategoryTypeAmountSummary {
        UUID getCategoryId();

        String getCategoryName();

        TransactionType getType();

        BigDecimal getTotal();
    }
}
