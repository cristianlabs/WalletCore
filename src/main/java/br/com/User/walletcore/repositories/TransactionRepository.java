package br.com.User.walletcore.repositories;

import br.com.User.walletcore.entities.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    Optional<Transaction> findByIdAndOwnerId(UUID id, UUID ownerId);

    Page<Transaction> findAllByOwnerId(UUID ownerId, Pageable pageable);
}
