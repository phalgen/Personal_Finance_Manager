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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SavingsGoalService {

    private final SavingsGoalRepository savingsGoalRepository;
    private final UserRepository userRepository;
    private final TransactionService transactionService;

    public SavingsGoalService(SavingsGoalRepository savingsGoalRepository,
                             UserRepository userRepository,
                             TransactionService transactionService) {
        this.savingsGoalRepository = savingsGoalRepository;
        this.userRepository = userRepository;
        this.transactionService = transactionService;
    }

    @Transactional
    public GoalResponse createGoal(GoalRequest request, Long userId) {
        if (request.getTargetDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Target date must be in the future");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        SavingsGoal goal = new SavingsGoal();
        goal.setGoalName(request.getGoalName());
        goal.setTargetAmount(request.getTargetAmount());
        goal.setTargetDate(request.getTargetDate());
        goal.setStartDate(request.getStartDate() != null ? request.getStartDate() : LocalDate.now());
        goal.setUser(user);

        goal = savingsGoalRepository.save(goal);
        return toGoalResponse(goal);
    }

    public List<GoalResponse> getAllGoals(Long userId) {
        List<SavingsGoal> goals = savingsGoalRepository.findByUserId(userId);
        return goals.stream()
                .map(this::toGoalResponse)
                .collect(Collectors.toList());
    }

    public GoalResponse getGoal(Long id, Long userId) {
        SavingsGoal goal = savingsGoalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Goal not found"));

        if (!goal.getUser().getId().equals(userId)) {
            throw new ForbiddenException("You don't have permission to view this goal");
        }

        return toGoalResponse(goal);
    }

    @Transactional
    public GoalResponse updateGoal(Long id, UpdateGoalRequest request, Long userId) {
        SavingsGoal goal = savingsGoalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Goal not found"));

        if (!goal.getUser().getId().equals(userId)) {
            throw new ForbiddenException("You don't have permission to update this goal");
        }

        if (request.getTargetAmount() != null) {
            goal.setTargetAmount(request.getTargetAmount());
        }
        if (request.getTargetDate() != null) {
            goal.setTargetDate(request.getTargetDate());
        }

        goal = savingsGoalRepository.save(goal);
        return toGoalResponse(goal);
    }

    @Transactional
    public void deleteGoal(Long id, Long userId) {
        SavingsGoal goal = savingsGoalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Goal not found"));

        if (!goal.getUser().getId().equals(userId)) {
            throw new ForbiddenException("You don't have permission to delete this goal");
        }

        savingsGoalRepository.delete(goal);
    }

    private GoalResponse toGoalResponse(SavingsGoal goal) {
        BigDecimal currentProgress = transactionService.calculateNetSavings(
                goal.getUser().getId(), goal.getStartDate(), LocalDate.now());

        GoalResponse response = new GoalResponse();
        response.setId(goal.getId());
        response.setGoalName(goal.getGoalName());
        response.setTargetAmount(goal.getTargetAmount());
        response.setTargetDate(goal.getTargetDate());
        response.setStartDate(goal.getStartDate());
        response.setCurrentProgress(currentProgress);
        response.setProgressPercentage(goal.calculateProgressPercentage(currentProgress));
        response.setRemainingAmount(goal.calculateRemainingAmount(currentProgress));

        return response;
    }
}
