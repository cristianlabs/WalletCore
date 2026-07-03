package br.com.User.walletcore.services;

import br.com.User.walletcore.dtos.CreateCategoryRequest;
import br.com.User.walletcore.dtos.UpdateCategoryRequest;
import br.com.User.walletcore.entities.Category;
import br.com.User.walletcore.entities.User;
import br.com.User.walletcore.exceptions.CategoryAlreadyExistsException;
import br.com.User.walletcore.repositories.CategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Transactional
    public Category create(User owner, CreateCategoryRequest request) {
        if (categoryRepository.existsByOwnerIdAndNameIgnoreCase(owner.getId(), request.name())) {
            throw new CategoryAlreadyExistsException(request.name());
        }

        Category category = new Category();
        category.setOwner(owner);
        category.setName(request.name());
        category.setType(request.type());
        return categoryRepository.save(category);
    }

    @Transactional(readOnly = true)
    public List<Category> findAll(User owner) {
        return categoryRepository.findAllByOwnerId(owner.getId());
    }

    @Transactional(readOnly = true)
    public Category findById(User owner, UUID id) {
        return categoryRepository.findByIdAndOwnerId(id, owner.getId())
                .orElseThrow(() -> new NoSuchElementException("Category not found: " + id));
    }

    @Transactional
    public Category update(User owner, UUID id, UpdateCategoryRequest request) {
        Category category = findById(owner, id);

        boolean nameChanged = !category.getName().equalsIgnoreCase(request.name());
        if (nameChanged && categoryRepository.existsByOwnerIdAndNameIgnoreCase(owner.getId(), request.name())) {
            throw new CategoryAlreadyExistsException(request.name());
        }

        category.setName(request.name());
        category.setType(request.type());
        return categoryRepository.save(category);
    }

    @Transactional
    public void delete(User owner, UUID id) {
        Category category = findById(owner, id);
        categoryRepository.delete(category);
    }
}
