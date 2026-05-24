package com.finance.personalfinancemanager.service;

import com.finance.personalfinancemanager.dto.goal.GoalRequest;
import com.finance.personalfinancemanager.dto.goal.GoalResponse;
import com.finance.personalfinancemanager.entity.SavingsGoal;
import com.finance.personalfinancemanager.entity.User;
import com.finance.personalfinancemanager.exception.BadRequestException;
import com.finance.personalfinancemanager.exception.ForbiddenException;
import com.finance.personalfinancemanager.exception.NotFoundException;
import com.finance.personalfinancemanager.repository.SavingsGoalRepository;
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
class SavingsGoalServiceTest {

    @Mock
    private SavingsGoalRepository savingsGoalRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TransactionService transactionService;

    @InjectMocks
    private SavingsGoalService savingsGoalService;

    private User user;
    private SavingsGoal savingsGoal;
    private GoalRequest goalRequest;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("test@example.com");

        savingsGoal = new SavingsGoal();
        savingsGoal.setId(1L);
        savingsGoal.setGoalName("Emergency Fund");
        savingsGoal.setTargetAmount(new BigDecimal("100000.00"));
        savingsGoal.setTargetDate(LocalDate.now().plusMonths(6));
        savingsGoal.setStartDate(LocalDate.now());
        savingsGoal.setUser(user);

        goalRequest = new GoalRequest();
        goalRequest.setGoalName("Emergency Fund");
        goalRequest.setTargetAmount(new BigDecimal("100000.00"));
        goalRequest.setTargetDate(LocalDate.now().plusMonths(6));
    }

    @Test
    void createGoal_Success() {
        // Arrange
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(savingsGoalRepository.save(any(SavingsGoal.class))).thenReturn(savingsGoal);
        when(transactionService.calculateNetSavingsSince(anyLong(), any(LocalDate.class)))
            .thenReturn(new BigDecimal("50000.00"));

        // Act
        GoalResponse response = savingsGoalService.createGoal(1L, goalRequest);

        // Assert
        assertNotNull(response);
        assertEquals("Emergency Fund", response.getGoalName());
        assertEquals(new BigDecimal("100000.00"), response.getTargetAmount());
        verify(savingsGoalRepository).save(any(SavingsGoal.class));
    }

    @Test
    void createGoal_PastDate_ThrowsBadRequestException() {
        // Arrange
        goalRequest.setTargetDate(LocalDate.now().minusDays(1));

        // Act & Assert
        BadRequestException exception = assertThrows(
            BadRequestException.class,
            () -> savingsGoalService.createGoal(1L, goalRequest)
        );

        assertEquals("Target date must be in the future", exception.getMessage());
        verify(savingsGoalRepository, never()).save(any(SavingsGoal.class));
    }

    @Test
    void getAllGoals_ReturnsUserGoals() {
        // Arrange
        when(savingsGoalRepository.findByUserId(anyLong()))
            .thenReturn(Arrays.asList(savingsGoal));
        when(transactionService.calculateNetSavingsSince(anyLong(), any(LocalDate.class)))
            .thenReturn(new BigDecimal("50000.00"));

        // Act
        List<GoalResponse> goals = savingsGoalService.getAllGoals(1L);

        // Assert
        assertEquals(1, goals.size());
        assertEquals("Emergency Fund", goals.get(0).getGoalName());
        assertEquals(50.0, goals.get(0).getProgressPercentage());
    }

    @Test
    void getGoalById_Success() {
        // Arrange
        when(savingsGoalRepository.findById(anyLong())).thenReturn(Optional.of(savingsGoal));
        when(transactionService.calculateNetSavingsSince(anyLong(), any(LocalDate.class)))
            .thenReturn(new BigDecimal("50000.00"));

        // Act
        GoalResponse response = savingsGoalService.getGoalById(1L, 1L);

        // Assert
        assertNotNull(response);
        assertEquals("Emergency Fund", response.getGoalName());
        assertEquals(new BigDecimal("50000.00"), response.getCurrentProgress());
    }

    @Test
    void getGoalById_NotFound_ThrowsNotFoundException() {
        // Arrange
        when(savingsGoalRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException exception = assertThrows(
            NotFoundException.class,
            () -> savingsGoalService.getGoalById(1L, 1L)
        );

        assertEquals("Savings goal not found", exception.getMessage());
    }

    @Test
    void getGoalById_NotOwner_ThrowsForbiddenException() {
        // Arrange
        when(savingsGoalRepository.findById(anyLong())).thenReturn(Optional.of(savingsGoal));

        // Act & Assert
        ForbiddenException exception = assertThrows(
            ForbiddenException.class,
            () -> savingsGoalService.getGoalById(2L, 1L) // Different userId
        );

        assertEquals("Access denied", exception.getMessage());
    }

    @Test
    void updateGoal_Success() {
        // Arrange
        when(savingsGoalRepository.findById(anyLong())).thenReturn(Optional.of(savingsGoal));
        when(savingsGoalRepository.save(any(SavingsGoal.class))).thenReturn(savingsGoal);
        when(transactionService.calculateNetSavingsSince(anyLong(), any(LocalDate.class)))
            .thenReturn(new BigDecimal("50000.00"));

        GoalRequest updateRequest = new GoalRequest();
        updateRequest.setTargetAmount(new BigDecimal("150000.00"));
        updateRequest.setTargetDate(LocalDate.now().plusMonths(12));

        // Act
        GoalResponse response = savingsGoalService.updateGoal(1L, 1L, updateRequest);

        // Assert
        assertNotNull(response);
        verify(savingsGoalRepository).save(savingsGoal);
    }

    @Test
    void deleteGoal_Success() {
        // Arrange
        when(savingsGoalRepository.findById(anyLong())).thenReturn(Optional.of(savingsGoal));

        // Act
        savingsGoalService.deleteGoal(1L, 1L);

        // Assert
        verify(savingsGoalRepository).delete(savingsGoal);
    }

    @Test
    void calculateProgress_ReturnsCorrectPercentage() {
        // Arrange
        when(transactionService.calculateNetSavingsSince(anyLong(), any(LocalDate.class)))
            .thenReturn(new BigDecimal("50000.00"));

        // Act
        double percentage = savingsGoalService.calculateProgressPercentage(
            1L,
            new BigDecimal("50000.00"),
            new BigDecimal("100000.00")
        );

        // Assert
        assertEquals(50.0, percentage, 0.01);
    }

    @Test
    void calculateProgress_ZeroTarget_ReturnsZero() {
        // Act
        double percentage = savingsGoalService.calculateProgressPercentage(
            1L,
            new BigDecimal("50000.00"),
            BigDecimal.ZERO
        );

        // Assert
        assertEquals(0.0, percentage);
    }
}
