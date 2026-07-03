package br.com.User.walletcore.dtos;

import br.com.User.walletcore.entities.CategoryType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateCategoryRequest(

        @NotBlank
        String name,

        @NotNull
        CategoryType type
) {
}
