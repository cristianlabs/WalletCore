package br.com.User.walletcore.controllers;

import br.com.User.walletcore.dtos.CreateTransactionRequest;
import br.com.User.walletcore.dtos.TransactionResponse;
import br.com.User.walletcore.dtos.UpdateTransactionRequest;
import br.com.User.walletcore.entities.Transaction;
import br.com.User.walletcore.security.AuthenticatedUser;
import br.com.User.walletcore.services.TransactionService;
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
@RequestMapping("/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping
    public ResponseEntity<TransactionResponse> create(@AuthenticationPrincipal AuthenticatedUser principal, @Valid @RequestBody CreateTransactionRequest request) {
        Transaction transaction = transactionService.create(principal.getUser(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(TransactionResponse.fromEntity(transaction));
    }

    @GetMapping
    public ResponseEntity<List<TransactionResponse>> findAll(@AuthenticationPrincipal AuthenticatedUser principal) {
        List<TransactionResponse> transactions = transactionService.findAll(principal.getUser()).stream()
                .map(TransactionResponse::fromEntity)
                .toList();
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransactionResponse> findById(@AuthenticationPrincipal AuthenticatedUser principal, @PathVariable UUID id) {
        Transaction transaction = transactionService.findById(principal.getUser(), id);
        return ResponseEntity.ok(TransactionResponse.fromEntity(transaction));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TransactionResponse> update(
            @AuthenticationPrincipal AuthenticatedUser principal,
            @PathVariable UUID id,
            @Valid @RequestBody UpdateTransactionRequest request
    ) {
        Transaction transaction = transactionService.update(principal.getUser(), id, request);
        return ResponseEntity.ok(TransactionResponse.fromEntity(transaction));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal AuthenticatedUser principal, @PathVariable UUID id) {
        transactionService.delete(principal.getUser(), id);
        return ResponseEntity.noContent().build();
    }
}
