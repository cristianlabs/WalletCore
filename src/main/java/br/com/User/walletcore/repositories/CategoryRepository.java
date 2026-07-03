package br.com.User.walletcore.repositories;

import br.com.User.walletcore.entities.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CategoryRepository extends JpaRepository<Category, UUID> {

    Optional<Category> findByIdAndOwnerId(UUID id, UUID ownerId);

    List<Category> findAllByOwnerId(UUID ownerId);

    boolean existsByOwnerIdAndNameIgnoreCase(UUID ownerId, String name);
}
