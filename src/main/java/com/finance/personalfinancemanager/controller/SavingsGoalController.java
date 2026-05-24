package com.finance.personalfinancemanager.controller;

import com.finance.personalfinancemanager.dto.goal.GoalRequest;
import com.finance.personalfinancemanager.dto.goal.GoalResponse;
import com.finance.personalfinancemanager.dto.goal.UpdateGoalRequest;
import com.finance.personalfinancemanager.service.SavingsGoalService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/goals")
public class SavingsGoalController {

    private final SavingsGoalService savingsGoalService;

    public SavingsGoalController(SavingsGoalService savingsGoalService) {
        this.savingsGoalService = savingsGoalService;
    }

    @PostMapping
    public ResponseEntity<GoalResponse> createGoal(
            @Valid @RequestBody GoalRequest request,
            HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        GoalResponse response = savingsGoalService.createGoal(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<Map<String, List<GoalResponse>>> getAllGoals(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        List<GoalResponse> goals = savingsGoalService.getAllGoals(userId);
        return ResponseEntity.ok(Map.of("goals", goals));
    }

    @GetMapping("/{id}")
    public ResponseEntity<GoalResponse> getGoal(
            @PathVariable Long id,
            HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        GoalResponse response = savingsGoalService.getGoal(id, userId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<GoalResponse> updateGoal(
            @PathVariable Long id,
            @Valid @RequestBody UpdateGoalRequest request,
            HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        GoalResponse response = savingsGoalService.updateGoal(id, request, userId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteGoal(
            @PathVariable Long id,
            HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        savingsGoalService.deleteGoal(id, userId);
        return ResponseEntity.ok(Map.of("message", "Goal deleted successfully"));
    }
}
