package br.com.User.walletcore.dtos;

import br.com.User.walletcore.entities.Category;
import br.com.User.walletcore.entities.CategoryType;

import java.time.Instant;
import java.util.UUID;

public record CategoryResponse(
        UUID id,
        String name,
        CategoryType type,
        Instant createdAt,
        Instant updatedAt
) {

    public static CategoryResponse fromEntity(Category category) {
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getType(),
                category.getCreatedAt(),
                category.getUpdatedAt()
        );
    }
}
