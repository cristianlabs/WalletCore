package br.com.User.walletcore.controllers;

import br.com.User.walletcore.dtos.CategoryResponse;
import br.com.User.walletcore.dtos.CreateCategoryRequest;
import br.com.User.walletcore.dtos.UpdateCategoryRequest;
import br.com.User.walletcore.entities.Category;
import br.com.User.walletcore.entities.User;
import br.com.User.walletcore.services.CategoryService;
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
@RequestMapping("/categories")
public class CategoryController {

    private final CategoryService categoryService;
    private final UserService userService;

    public CategoryController(CategoryService categoryService, UserService userService) {
        this.categoryService = categoryService;
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<CategoryResponse> create(Authentication authentication, @Valid @RequestBody CreateCategoryRequest request) {
        User owner = currentUser(authentication);
        Category category = categoryService.create(owner, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(CategoryResponse.fromEntity(category));
    }

    @GetMapping
    public ResponseEntity<List<CategoryResponse>> findAll(Authentication authentication) {
        User owner = currentUser(authentication);
        List<CategoryResponse> categories = categoryService.findAll(owner).stream()
                .map(CategoryResponse::fromEntity)
                .toList();
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponse> findById(Authentication authentication, @PathVariable UUID id) {
        User owner = currentUser(authentication);
        Category category = categoryService.findById(owner, id);
        return ResponseEntity.ok(CategoryResponse.fromEntity(category));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponse> update(
            Authentication authentication,
            @PathVariable UUID id,
            @Valid @RequestBody UpdateCategoryRequest request
    ) {
        User owner = currentUser(authentication);
        Category category = categoryService.update(owner, id, request);
        return ResponseEntity.ok(CategoryResponse.fromEntity(category));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(Authentication authentication, @PathVariable UUID id) {
        User owner = currentUser(authentication);
        categoryService.delete(owner, id);
        return ResponseEntity.noContent().build();
    }

    private User currentUser(Authentication authentication) {
        return userService.findByEmail(authentication.getName());
    }
}
