package com.finance.personalfinancemanager.service;

import com.finance.personalfinancemanager.dto.goal.GoalRequest;
import com.finance.personalfinancemanager.dto.goal.GoalResponse;
import com.finance.personalfinancemanager.dto.goal.UpdateGoalRequest;
import com.finance.personalfinancemanager.entity.SavingsGoal;
import com.finance.personalfinancemanager.entity.User;
import com.finance.personalfinancemanager.exception.ForbiddenException;
import com.finance.personalfinancemanager.exception.ResourceNotFoundException;
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
        savingsGoal.setTargetAmount(new BigDecimal("10000.00"));
        savingsGoal.setTargetDate(LocalDate.now().plusMonths(6));
        savingsGoal.setStartDate(LocalDate.now());
        savingsGoal.setUser(user);

        goalRequest = new GoalRequest();
        goalRequest.setGoalName("Emergency Fund");
        goalRequest.setTargetAmount(new BigDecimal("10000.00"));
        goalRequest.setTargetDate(LocalDate.now().plusMonths(6));
    }

    @Test
    void createGoal_Success() {
        // Arrange
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(savingsGoalRepository.save(any(SavingsGoal.class))).thenReturn(savingsGoal);
        when(transactionService.calculateNetSavings(anyLong(), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(new BigDecimal("5000.00"));

        // Act
        GoalResponse response = savingsGoalService.createGoal(goalRequest, 1L);

        // Assert
        assertNotNull(response);
        assertEquals("Emergency Fund", response.getGoalName());
        assertEquals(new BigDecimal("10000.00"), response.getTargetAmount());
        verify(savingsGoalRepository).save(any(SavingsGoal.class));
    }

    @Test
    void createGoal_PastDate_ThrowsIllegalArgumentException() {
        // Arrange
        goalRequest.setTargetDate(LocalDate.now().minusDays(1));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> savingsGoalService.createGoal(goalRequest, 1L)
        );

        assertEquals("Target date must be in the future", exception.getMessage());
        verify(savingsGoalRepository, never()).save(any(SavingsGoal.class));
    }

    @Test
    void getAllGoals_ReturnsUserGoals() {
        // Arrange
        when(savingsGoalRepository.findByUserId(anyLong()))
                .thenReturn(Arrays.asList(savingsGoal));
        when(transactionService.calculateNetSavings(anyLong(), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(new BigDecimal("5000.00"));

        // Act
        List<GoalResponse> goals = savingsGoalService.getAllGoals(1L);

        // Assert
        assertEquals(1, goals.size());
        assertEquals("Emergency Fund", goals.get(0).getGoalName());
        assertEquals(50.0, goals.get(0).getProgressPercentage());
    }

    @Test
    void getGoal_Success() {
        // Arrange
        when(savingsGoalRepository.findById(anyLong())).thenReturn(Optional.of(savingsGoal));
        when(transactionService.calculateNetSavings(anyLong(), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(new BigDecimal("5000.00"));

        // Act
        GoalResponse response = savingsGoalService.getGoal(1L, 1L);

        // Assert
        assertNotNull(response);
        assertEquals("Emergency Fund", response.getGoalName());
        assertEquals(new BigDecimal("5000.00"), response.getCurrentProgress());
    }

    @Test
    void getGoal_NotFound_ThrowsResourceNotFoundException() {
        // Arrange
        when(savingsGoalRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> savingsGoalService.getGoal(1L, 1L)
        );

        assertEquals("Goal not found", exception.getMessage());
    }

    @Test
    void getGoal_NotOwner_ThrowsForbiddenException() {
        // Arrange
        User otherUser = new User();
        otherUser.setId(2L);
        savingsGoal.setUser(otherUser);

        when(savingsGoalRepository.findById(anyLong())).thenReturn(Optional.of(savingsGoal));

        // Act & Assert
        ForbiddenException exception = assertThrows(
                ForbiddenException.class,
                () -> savingsGoalService.getGoal(1L, 1L) // Different userId
        );

        assertEquals("You don't have permission to view this goal", exception.getMessage());
    }

    @Test
    void updateGoal_Success() {
        // Arrange
        when(savingsGoalRepository.findById(anyLong())).thenReturn(Optional.of(savingsGoal));
        when(savingsGoalRepository.save(any(SavingsGoal.class))).thenReturn(savingsGoal);
        when(transactionService.calculateNetSavings(anyLong(), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(new BigDecimal("5000.00"));

        UpdateGoalRequest updateRequest = new UpdateGoalRequest();
        updateRequest.setTargetAmount(new BigDecimal("15000.00"));
        updateRequest.setTargetDate(LocalDate.now().plusMonths(12));

        // Act
        GoalResponse response = savingsGoalService.updateGoal(1L, updateRequest, 1L);

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
}