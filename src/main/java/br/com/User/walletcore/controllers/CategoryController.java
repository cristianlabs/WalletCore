package br.com.User.walletcore.controllers;

import br.com.User.walletcore.dtos.CategoryResponse;
import br.com.User.walletcore.dtos.CreateCategoryRequest;
import br.com.User.walletcore.dtos.UpdateCategoryRequest;
import br.com.User.walletcore.entities.Category;
import br.com.User.walletcore.security.AuthenticatedUser;
import br.com.User.walletcore.services.CategoryService;
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
@RequestMapping("/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PostMapping
    public ResponseEntity<CategoryResponse> create(@AuthenticationPrincipal AuthenticatedUser principal, @Valid @RequestBody CreateCategoryRequest request) {
        Category category = categoryService.create(principal.getUser(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(CategoryResponse.fromEntity(category));
    }

    @GetMapping
    public ResponseEntity<List<CategoryResponse>> findAll(@AuthenticationPrincipal AuthenticatedUser principal) {
        List<CategoryResponse> categories = categoryService.findAll(principal.getUser()).stream()
                .map(CategoryResponse::fromEntity)
                .toList();
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponse> findById(@AuthenticationPrincipal AuthenticatedUser principal, @PathVariable UUID id) {
        Category category = categoryService.findById(principal.getUser(), id);
        return ResponseEntity.ok(CategoryResponse.fromEntity(category));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponse> update(
            @AuthenticationPrincipal AuthenticatedUser principal,
            @PathVariable UUID id,
            @Valid @RequestBody UpdateCategoryRequest request
    ) {
        Category category = categoryService.update(principal.getUser(), id, request);
        return ResponseEntity.ok(CategoryResponse.fromEntity(category));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal AuthenticatedUser principal, @PathVariable UUID id) {
        categoryService.delete(principal.getUser(), id);
        return ResponseEntity.noContent().build();
    }
}
