package com.finance.personalfinancemanager.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

@Entity
@Table(name = "savings_goals")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SavingsGoal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Goal name is required")
    @Column(nullable = false)
    private String goalName;

    @NotNull(message = "Target amount is required")
    @Positive(message = "Target amount must be positive")
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal targetAmount;

    @NotNull(message = "Target date is required")
    @Column(nullable = false)
    private LocalDate targetDate;

    @Column(nullable = false)
    private LocalDate startDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @NotNull
    @JsonIgnore
    private User user;

    @PrePersist
    protected void onCreate() {
        if (startDate == null) {
            startDate = LocalDate.now();
        }
    }

    // CHANGED: Return Double instead of BigDecimal to avoid trailing zeros
    public Double calculateProgressPercentage(BigDecimal currentProgress) {
        if (targetAmount.compareTo(BigDecimal.ZERO) == 0) {
            return 0.0;
        }
        BigDecimal percentage = currentProgress
                .divide(targetAmount, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP);

        // Convert to double - this automatically strips trailing zeros (65.50 → 65.5)
        return percentage.doubleValue();
    }

    public BigDecimal calculateRemainingAmount(BigDecimal currentProgress) {
        BigDecimal remaining = targetAmount.subtract(currentProgress);
        return remaining.max(BigDecimal.ZERO);
    }
}