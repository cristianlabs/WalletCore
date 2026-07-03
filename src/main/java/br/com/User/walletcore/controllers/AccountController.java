package br.com.User.walletcore.controllers;

import br.com.User.walletcore.dtos.AccountResponse;
import br.com.User.walletcore.dtos.CreateAccountRequest;
import br.com.User.walletcore.dtos.UpdateAccountRequest;
import br.com.User.walletcore.entities.Account;
import br.com.User.walletcore.entities.User;
import br.com.User.walletcore.services.AccountService;
import br.com.User.walletcore.services.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/accounts")
public class AccountController {

    private final AccountService accountService;
    private final UserService userService;

    public AccountController(AccountService accountService, UserService userService) {
        this.accountService = accountService;
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<AccountResponse> create(Authentication authentication, @Valid @RequestBody CreateAccountRequest request) {
        User owner = currentUser(authentication);
        Account account = accountService.create(owner, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(AccountResponse.fromEntity(account));
    }

    @GetMapping
    public ResponseEntity<List<AccountResponse>> findAll(Authentication authentication) {
        User owner = currentUser(authentication);
        List<AccountResponse> accounts = accountService.findAll(owner).stream()
                .map(AccountResponse::fromEntity)
                .toList();
        return ResponseEntity.ok(accounts);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AccountResponse> findById(Authentication authentication, @PathVariable UUID id) {
        User owner = currentUser(authentication);
        Account account = accountService.findById(owner, id);
        return ResponseEntity.ok(AccountResponse.fromEntity(account));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AccountResponse> update(
            Authentication authentication,
            @PathVariable UUID id,
            @Valid @RequestBody UpdateAccountRequest request
    ) {
        User owner = currentUser(authentication);
        Account account = accountService.update(owner, id, request);
        return ResponseEntity.ok(AccountResponse.fromEntity(account));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(Authentication authentication, @PathVariable UUID id) {
        User owner = currentUser(authentication);
        accountService.delete(owner, id);
        return ResponseEntity.noContent().build();
    }

    private User currentUser(Authentication authentication) {
        return userService.findByEmail(authentication.getName());
    }
}
