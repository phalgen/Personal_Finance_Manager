package com.finance.personalfinancemanager.service;

import com.finance.personalfinancemanager.dto.transaction.TransactionRequest;
import com.finance.personalfinancemanager.dto.transaction.TransactionResponse;
import com.finance.personalfinancemanager.entity.Category;
import com.finance.personalfinancemanager.entity.CategoryType;
import com.finance.personalfinancemanager.entity.Transaction;
import com.finance.personalfinancemanager.entity.User;
import com.finance.personalfinancemanager.exception.BadRequestException;
import com.finance.personalfinancemanager.exception.ForbiddenException;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private TransactionService transactionService;

    private User user;
    private Category category;
    private Transaction transaction;
    private TransactionRequest transactionRequest;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("test@example.com");

        category = new Category();
        category.setId(1L);
        category.setName("Salary");
        category.setType(CategoryType.INCOME);
        category.setCustom(false);

        transaction = new Transaction();
        transaction.setId(1L);
        transaction.setAmount(new BigDecimal("50000.00"));
        transaction.setDate(LocalDate.now());
        transaction.setCategory(category);
        transaction.setDescription("Test transaction");
        transaction.setUser(user);
        transaction.setDeleted(false);

        transactionRequest = new TransactionRequest();
        transactionRequest.setAmount(new BigDecimal("50000.00"));
        transactionRequest.setDate(LocalDate.now());
        transactionRequest.setCategory("Salary");
        transactionRequest.setDescription("Test transaction");
    }

    @Test
    void createTransaction_Success() {
        // Arrange
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(categoryRepository.findByName(anyString())).thenReturn(Optional.of(category));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);

        // Act
        TransactionResponse response = transactionService.createTransaction(1L, transactionRequest);

        // Assert
        assertNotNull(response);
        assertEquals(new BigDecimal("50000.00"), response.getAmount());
        assertEquals("Salary", response.getCategory());
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void createTransaction_FutureDate_ThrowsBadRequestException() {
        // Arrange
        transactionRequest.setDate(LocalDate.now().plusDays(1));

        // Act & Assert
        BadRequestException exception = assertThrows(
            BadRequestException.class,
            () -> transactionService.createTransaction(1L, transactionRequest)
        );

        assertEquals("Transaction date cannot be in the future", exception.getMessage());
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    void createTransaction_CategoryNotFound_ThrowsNotFoundException() {
        // Arrange
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(categoryRepository.findByName(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException exception = assertThrows(
            NotFoundException.class,
            () -> transactionService.createTransaction(1L, transactionRequest)
        );

        assertEquals("Category not found", exception.getMessage());
    }

    @Test
    void getAllTransactions_ReturnsUserTransactions() {
        // Arrange
        when(transactionRepository.findByUserIdAndDeletedFalseOrderByDateDesc(anyLong()))
            .thenReturn(Arrays.asList(transaction));

        // Act
        List<TransactionResponse> transactions = transactionService.getAllTransactions(1L, null, null, null);

        // Assert
        assertEquals(1, transactions.size());
        assertEquals("Salary", transactions.get(0).getCategory());
        verify(transactionRepository).findByUserIdAndDeletedFalseOrderByDateDesc(1L);
    }

    @Test
    void updateTransaction_Success() {
        // Arrange
        when(transactionRepository.findById(anyLong())).thenReturn(Optional.of(transaction));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);

        TransactionRequest updateRequest = new TransactionRequest();
        updateRequest.setAmount(new BigDecimal("55000.00"));
        updateRequest.setDescription("Updated description");

        // Act
        TransactionResponse response = transactionService.updateTransaction(1L, 1L, updateRequest);

        // Assert
        assertNotNull(response);
        verify(transactionRepository).findById(1L);
        verify(transactionRepository).save(transaction);
    }

    @Test
    void updateTransaction_NotOwner_ThrowsForbiddenException() {
        // Arrange
        when(transactionRepository.findById(anyLong())).thenReturn(Optional.of(transaction));

        TransactionRequest updateRequest = new TransactionRequest();
        updateRequest.setAmount(new BigDecimal("55000.00"));

        // Act & Assert
        ForbiddenException exception = assertThrows(
            ForbiddenException.class,
            () -> transactionService.updateTransaction(2L, 1L, updateRequest) // Different userId
        );

        assertEquals("Access denied", exception.getMessage());
    }

    @Test
    void deleteTransaction_Success() {
        // Arrange
        when(transactionRepository.findById(anyLong())).thenReturn(Optional.of(transaction));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);

        // Act
        transactionService.deleteTransaction(1L, 1L);

        // Assert
        assertTrue(transaction.isDeleted());
        verify(transactionRepository).save(transaction);
    }

    @Test
    void deleteTransaction_NotFound_ThrowsNotFoundException() {
        // Arrange
        when(transactionRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException exception = assertThrows(
            NotFoundException.class,
            () -> transactionService.deleteTransaction(1L, 1L)
        );

        assertEquals("Transaction not found", exception.getMessage());
    }

    @Test
    void calculateNetSavings_ReturnsCorrectValue() {
        // Arrange
        Transaction income = new Transaction();
        income.setAmount(new BigDecimal("50000.00"));
        income.setCategory(category);

        Category expenseCategory = new Category();
        expenseCategory.setType(CategoryType.EXPENSE);

        Transaction expense = new Transaction();
        expense.setAmount(new BigDecimal("20000.00"));
        expense.setCategory(expenseCategory);

        when(transactionRepository.findByUserIdAndDeletedFalseOrderByDateDesc(anyLong()))
            .thenReturn(Arrays.asList(income, expense));

        // Act
        BigDecimal netSavings = transactionService.calculateNetSavings(1L);

        // Assert
        assertEquals(new BigDecimal("30000.00"), netSavings);
    }
}
