package br.com.User.walletcore.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateUserRequest(

        @NotBlank
        @Size(max = 255)
        String name,

        @NotBlank
        @Email
        @Size(max = 255)
        String email,

        // BCrypt only considers the first 72 bytes of the raw password
        @NotBlank
        @Size(min = 8, max = 72)
        String password
) {
    public CreateUserRequest {
        email = email == null ? null : email.trim().toLowerCase();
    }
}
