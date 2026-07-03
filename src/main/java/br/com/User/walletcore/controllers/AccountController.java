package br.com.User.walletcore.controllers;

import br.com.User.walletcore.dtos.AccountResponse;
import br.com.User.walletcore.dtos.CreateAccountRequest;
import br.com.User.walletcore.dtos.UpdateAccountRequest;
import br.com.User.walletcore.entities.Account;
import br.com.User.walletcore.security.AuthenticatedUser;
import br.com.User.walletcore.services.AccountService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping
    public ResponseEntity<AccountResponse> create(@AuthenticationPrincipal AuthenticatedUser principal, @Valid @RequestBody CreateAccountRequest request) {
        Account account = accountService.create(principal.getUser(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(AccountResponse.fromEntity(account));
    }

    @GetMapping
    public ResponseEntity<List<AccountResponse>> findAll(@AuthenticationPrincipal AuthenticatedUser principal) {
        List<AccountResponse> accounts = accountService.findAll(principal.getUser()).stream()
                .map(AccountResponse::fromEntity)
                .toList();
        return ResponseEntity.ok(accounts);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AccountResponse> findById(@AuthenticationPrincipal AuthenticatedUser principal, @PathVariable UUID id) {
        Account account = accountService.findById(principal.getUser(), id);
        return ResponseEntity.ok(AccountResponse.fromEntity(account));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AccountResponse> update(
            @AuthenticationPrincipal AuthenticatedUser principal,
            @PathVariable UUID id,
            @Valid @RequestBody UpdateAccountRequest request
    ) {
        Account account = accountService.update(principal.getUser(), id, request);
        return ResponseEntity.ok(AccountResponse.fromEntity(account));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal AuthenticatedUser principal, @PathVariable UUID id) {
        accountService.delete(principal.getUser(), id);
        return ResponseEntity.noContent().build();
    }
}
