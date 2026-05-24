package com.finance.personalfinancemanager.service;

import com.finance.personalfinancemanager.dto.report.MonthlyReportResponse;
import com.finance.personalfinancemanager.dto.report.YearlyReportResponse;
import com.finance.personalfinancemanager.entity.Category;
import com.finance.personalfinancemanager.entity.CategoryType;
import com.finance.personalfinancemanager.entity.Transaction;
import com.finance.personalfinancemanager.entity.User;
import com.finance.personalfinancemanager.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private ReportService reportService;

    private User user;
    private Category salaryCategory;
    private Category foodCategory;
    private Transaction salaryTransaction;
    private Transaction foodTransaction;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("test@example.com");

        salaryCategory = new Category();
        salaryCategory.setName("Salary");
        salaryCategory.setType(CategoryType.INCOME);

        foodCategory = new Category();
        foodCategory.setName("Food");
        foodCategory.setType(CategoryType.EXPENSE);

        salaryTransaction = new Transaction();
        salaryTransaction.setId(1L);
        salaryTransaction.setAmount(new BigDecimal("50000.00"));
        salaryTransaction.setCategory(salaryCategory);
        salaryTransaction.setDate(LocalDate.of(2024, 5, 15));
        salaryTransaction.setUser(user);
        salaryTransaction.setDeleted(false);

        foodTransaction = new Transaction();
        foodTransaction.setId(2L);
        foodTransaction.setAmount(new BigDecimal("5000.00"));
        foodTransaction.setCategory(foodCategory);
        foodTransaction.setDate(LocalDate.of(2024, 5, 20));
        foodTransaction.setUser(user);
        foodTransaction.setDeleted(false);
    }

    @Test
    void getMonthlyReport_ReturnsCorrectData() {
        // Arrange
        when(transactionRepository.findByUserIdAndDateBetweenAndDeletedFalse(
            anyLong(), any(LocalDate.class), any(LocalDate.class)))
            .thenReturn(Arrays.asList(salaryTransaction, foodTransaction));

        // Act
        MonthlyReportResponse report = reportService.getMonthlyReport(1L, 2024, 5);

        // Assert
        assertNotNull(report);
        assertEquals(5, report.getMonth());
        assertEquals(2024, report.getYear());
        
        Map<String, BigDecimal> totalIncome = report.getTotalIncome();
        Map<String, BigDecimal> totalExpenses = report.getTotalExpenses();
        
        assertEquals(new BigDecimal("50000.00"), totalIncome.get("Salary"));
        assertEquals(new BigDecimal("5000.00"), totalExpenses.get("Food"));
        assertEquals(new BigDecimal("45000.00"), report.getNetSavings());
        
        verify(transactionRepository).findByUserIdAndDateBetweenAndDeletedFalse(
            eq(1L), any(LocalDate.class), any(LocalDate.class));
    }

    @Test
    void getYearlyReport_ReturnsCorrectData() {
        // Arrange
        when(transactionRepository.findByUserIdAndDateBetweenAndDeletedFalse(
            anyLong(), any(LocalDate.class), any(LocalDate.class)))
            .thenReturn(Arrays.asList(salaryTransaction, foodTransaction));

        // Act
        YearlyReportResponse report = reportService.getYearlyReport(1L, 2024);

        // Assert
        assertNotNull(report);
        assertEquals(2024, report.getYear());
        
        Map<String, BigDecimal> totalIncome = report.getTotalIncome();
        Map<String, BigDecimal> totalExpenses = report.getTotalExpenses();
        
        assertEquals(new BigDecimal("50000.00"), totalIncome.get("Salary"));
        assertEquals(new BigDecimal("5000.00"), totalExpenses.get("Food"));
        assertEquals(new BigDecimal("45000.00"), report.getNetSavings());
        
        verify(transactionRepository).findByUserIdAndDateBetweenAndDeletedFalse(
            eq(1L), any(LocalDate.class), any(LocalDate.class));
    }

    @Test
    void getMonthlyReport_NoTransactions_ReturnsEmptyReport() {
        // Arrange
        when(transactionRepository.findByUserIdAndDateBetweenAndDeletedFalse(
            anyLong(), any(LocalDate.class), any(LocalDate.class)))
            .thenReturn(Arrays.asList());

        // Act
        MonthlyReportResponse report = reportService.getMonthlyReport(1L, 2024, 5);

        // Assert
        assertNotNull(report);
        assertTrue(report.getTotalIncome().isEmpty());
        assertTrue(report.getTotalExpenses().isEmpty());
        assertEquals(BigDecimal.ZERO, report.getNetSavings());
    }

    @Test
    void getMonthlyReport_MultipleTransactionsSameCategory_Aggregates() {
        // Arrange
        Transaction salary2 = new Transaction();
        salary2.setAmount(new BigDecimal("25000.00"));
        salary2.setCategory(salaryCategory);
        salary2.setDate(LocalDate.of(2024, 5, 25));
        salary2.setUser(user);
        salary2.setDeleted(false);

        when(transactionRepository.findByUserIdAndDateBetweenAndDeletedFalse(
            anyLong(), any(LocalDate.class), any(LocalDate.class)))
            .thenReturn(Arrays.asList(salaryTransaction, salary2, foodTransaction));

        // Act
        MonthlyReportResponse report = reportService.getMonthlyReport(1L, 2024, 5);

        // Assert
        Map<String, BigDecimal> totalIncome = report.getTotalIncome();
        assertEquals(new BigDecimal("75000.00"), totalIncome.get("Salary")); // 50000 + 25000
        assertEquals(new BigDecimal("70000.00"), report.getNetSavings()); // 75000 - 5000
    }

    @Test
    void getYearlyReport_InvalidYear_HandlesGracefully() {
        // Arrange
        when(transactionRepository.findByUserIdAndDateBetweenAndDeletedFalse(
            anyLong(), any(LocalDate.class), any(LocalDate.class)))
            .thenReturn(Arrays.asList());

        // Act
        YearlyReportResponse report = reportService.getYearlyReport(1L, 2030);

        // Assert
        assertNotNull(report);
        assertEquals(2030, report.getYear());
        assertTrue(report.getTotalIncome().isEmpty());
        assertTrue(report.getTotalExpenses().isEmpty());
    }
}
