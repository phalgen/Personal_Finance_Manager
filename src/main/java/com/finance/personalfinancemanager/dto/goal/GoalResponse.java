package com.finance.personalfinancemanager.dto.goal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GoalResponse {

    private Long id;
    private String goalName;
    private BigDecimal targetAmount;
    private LocalDate targetDate;
    private LocalDate startDate;
    private BigDecimal currentProgress;

    // CHANGED: Use Double instead of BigDecimal to avoid trailing zeros (65.5 instead of 65.50)
    private Double progressPercentage;

    private BigDecimal remainingAmount;
}