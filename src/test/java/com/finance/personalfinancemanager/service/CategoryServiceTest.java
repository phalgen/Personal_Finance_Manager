package com.finance.personalfinancemanager.service;

import com.finance.personalfinancemanager.dto.category.CategoryRequest;
import com.finance.personalfinancemanager.dto.category.CategoryResponse;
import com.finance.personalfinancemanager.entity.Category;
import com.finance.personalfinancemanager.entity.CategoryType;
import com.finance.personalfinancemanager.entity.User;
import com.finance.personalfinancemanager.exception.BadRequestException;
import com.finance.personalfinancemanager.exception.ConflictException;
import com.finance.personalfinancemanager.exception.NotFoundException;
import com.finance.personalfinancemanager.repository.CategoryRepository;
import com.finance.personalfinancemanager.repository.TransactionRepository;
import com.finance.personalfinancemanager.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private CategoryService categoryService;

    private User user;
    private Category defaultCategory;
    private Category customCategory;
    private CategoryRequest categoryRequest;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("test@example.com");

        defaultCategory = new Category();
        defaultCategory.setName("Salary");
        defaultCategory.setType(CategoryType.INCOME);
        defaultCategory.setCustom(false);
        defaultCategory.setUser(null);

        customCategory = new Category();
        customCategory.setName("Freelance");
        customCategory.setType(CategoryType.INCOME);
        customCategory.setCustom(true);
        customCategory.setUser(user);

        categoryRequest = new CategoryRequest();
        categoryRequest.setName("SideBusiness");
        categoryRequest.setType(CategoryType.INCOME);
    }

    @Test
    void getAllCategories_ReturnsDefaultAndCustomCategories() {
        // Arrange
        when(categoryRepository.findByUserIdOrIsCustomFalse(anyLong()))
            .thenReturn(Arrays.asList(defaultCategory, customCategory));

        // Act
        List<CategoryResponse> categories = categoryService.getAllCategories(1L);

        // Assert
        assertEquals(2, categories.size());
        verify(categoryRepository).findByUserIdOrIsCustomFalse(1L);
    }

    @Test
    void createCategory_Success() {
        // Arrange
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(categoryRepository.existsByNameAndUserId(anyString(), anyLong())).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenReturn(customCategory);

        // Act
        CategoryResponse response = categoryService.createCategory(1L, categoryRequest);

        // Assert
        assertNotNull(response);
        assertEquals("SideBusiness", categoryRequest.getName());
        verify(userRepository).findById(1L);
        verify(categoryRepository).existsByNameAndUserId("SideBusiness", 1L);
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    void createCategory_DuplicateName_ThrowsConflictException() {
        // Arrange
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(categoryRepository.existsByNameAndUserId(anyString(), anyLong())).thenReturn(true);

        // Act & Assert
        ConflictException exception = assertThrows(
            ConflictException.class,
            () -> categoryService.createCategory(1L, categoryRequest)
        );

        assertEquals("Category with this name already exists", exception.getMessage());
        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    void deleteCategory_Success() {
        // Arrange
        when(categoryRepository.findByNameAndUserId(anyString(), anyLong()))
            .thenReturn(Optional.of(customCategory));
        when(transactionRepository.countByCategoryId(anyLong())).thenReturn(0L);

        // Act
        categoryService.deleteCategory(1L, "Freelance");

        // Assert
        verify(categoryRepository).findByNameAndUserId("Freelance", 1L);
        verify(transactionRepository).countByCategoryId(customCategory.getId());
        verify(categoryRepository).delete(customCategory);
    }

    @Test
    void deleteCategory_NotCustom_ThrowsBadRequestException() {
        // Arrange
        when(categoryRepository.findByNameAndUserId(anyString(), anyLong()))
            .thenReturn(Optional.of(defaultCategory));

        // Act & Assert
        BadRequestException exception = assertThrows(
            BadRequestException.class,
            () -> categoryService.deleteCategory(1L, "Salary")
        );

        assertEquals("Cannot delete default categories", exception.getMessage());
        verify(categoryRepository, never()).delete(any(Category.class));
    }

    @Test
    void deleteCategory_InUse_ThrowsBadRequestException() {
        // Arrange
        when(categoryRepository.findByNameAndUserId(anyString(), anyLong()))
            .thenReturn(Optional.of(customCategory));
        when(transactionRepository.countByCategoryId(anyLong())).thenReturn(5L);

        // Act & Assert
        BadRequestException exception = assertThrows(
            BadRequestException.class,
            () -> categoryService.deleteCategory(1L, "Freelance")
        );

        assertEquals("Cannot delete category that is in use by transactions", exception.getMessage());
        verify(categoryRepository, never()).delete(any(Category.class));
    }

    @Test
    void deleteCategory_NotFound_ThrowsNotFoundException() {
        // Arrange
        when(categoryRepository.findByNameAndUserId(anyString(), anyLong()))
            .thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException exception = assertThrows(
            NotFoundException.class,
            () -> categoryService.deleteCategory(1L, "NonExistent")
        );

        assertEquals("Category not found", exception.getMessage());
    }
}
