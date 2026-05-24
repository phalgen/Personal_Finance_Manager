package com.finance.personalfinancemanager.service;

import com.finance.personalfinancemanager.dto.category.CategoryRequest;
import com.finance.personalfinancemanager.dto.category.CategoryResponse;
import com.finance.personalfinancemanager.entity.Category;
import com.finance.personalfinancemanager.entity.Category.CategoryType;
import com.finance.personalfinancemanager.entity.User;
import com.finance.personalfinancemanager.exception.ConflictException;
import com.finance.personalfinancemanager.exception.ForbiddenException;
import com.finance.personalfinancemanager.exception.ResourceNotFoundException;
import com.finance.personalfinancemanager.repository.CategoryRepository;
import com.finance.personalfinancemanager.repository.TransactionRepository;
import com.finance.personalfinancemanager.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    private static final String[] DEFAULT_INCOME_CATEGORIES = {"Salary"};
    private static final String[] DEFAULT_EXPENSE_CATEGORIES = {
        "Food", "Rent", "Transportation", "Entertainment", "Healthcare", "Utilities"
    };

    public CategoryService(CategoryRepository categoryRepository,
                          TransactionRepository transactionRepository,
                          UserRepository userRepository) {
        this.categoryRepository = categoryRepository;
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public void initializeDefaultCategories(User user) {
        List<Category> defaultCategories = new ArrayList<>();

        for (String name : DEFAULT_INCOME_CATEGORIES) {
            Category category = Category.createDefault(name, CategoryType.INCOME, user);
            defaultCategories.add(category);
        }

        for (String name : DEFAULT_EXPENSE_CATEGORIES) {
            Category category = Category.createDefault(name, CategoryType.EXPENSE, user);
            defaultCategories.add(category);
        }

        categoryRepository.saveAll(defaultCategories);
    }

    public List<CategoryResponse> getAllCategoriesForUser(Long userId) {
        List<Category> categories = categoryRepository.findByUserIdOrIsCustomFalse(userId);
        return categories.stream()
                .map(this::toCategoryResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public CategoryResponse createCustomCategory(CategoryRequest request, Long userId) {
        if (categoryRepository.existsByNameAndUserId(request.getName(), userId)) {
            throw new ConflictException("Category with name '" + request.getName() + "' already exists");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        CategoryType type;
        try {
            type = CategoryType.valueOf(request.getType().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid category type. Must be INCOME or EXPENSE");
        }

        Category category = Category.createCustom(request.getName(), type, user);
        category = categoryRepository.save(category);
        
        return toCategoryResponse(category);
    }

    @Transactional
    public void deleteCustomCategory(String categoryName, Long userId) {
        Category category = categoryRepository.findByNameAndUserId(categoryName, userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Category '" + categoryName + "' not found"));

        if (!category.getIsCustom()) {
            throw new ForbiddenException("Cannot delete default category");
        }

        long transactionCount = transactionRepository.countByCategoryIdAndDeletedFalse(
                category.getId());
        if (transactionCount > 0) {
            throw new ConflictException(
                    "Cannot delete category that is referenced by " + transactionCount + " transaction(s)");
        }

        categoryRepository.delete(category);
    }

    public Category getCategoryByName(String name, Long userId) {
        return categoryRepository.findByNameAndUserId(name, userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Category '" + name + "' not found or not accessible"));
    }

    private CategoryResponse toCategoryResponse(Category category) {
        CategoryResponse response = new CategoryResponse();
        response.setName(category.getName());
        response.setType(category.getType().name());
        response.setIsCustom(category.getIsCustom());
        return response;
    }
}
