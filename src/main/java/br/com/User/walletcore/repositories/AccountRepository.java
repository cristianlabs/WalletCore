package br.com.User.walletcore.repositories;

import br.com.User.walletcore.entities.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AccountRepository extends JpaRepository<Account, UUID> {

    Optional<Account> findByIdAndOwnerId(UUID id, UUID ownerId);

    List<Account> findAllByOwnerId(UUID ownerId);
}
