package com.finance.personalfinancemanager.service;

import com.finance.personalfinancemanager.dto.report.MonthlyReportResponse;
import com.finance.personalfinancemanager.dto.report.YearlyReportResponse;
import com.finance.personalfinancemanager.entity.Category;
import com.finance.personalfinancemanager.entity.Transaction;
import com.finance.personalfinancemanager.repository.TransactionRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReportService {

    private final TransactionRepository transactionRepository;

    public ReportService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public MonthlyReportResponse getMonthlyReport(Long userId, int year, int month) {
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.plusMonths(1);

        List<Transaction> transactions = transactionRepository.findTransactionsForPeriod(
                userId, startDate, endDate);

        Map<String, BigDecimal> totalIncome = new HashMap<>();
        Map<String, BigDecimal> totalExpenses = new HashMap<>();
        BigDecimal netSavings = BigDecimal.ZERO;

        for (Transaction t : transactions) {
            String categoryName = t.getCategory().getName();
            BigDecimal amount = t.getAmount();

            if (t.getCategory().getType() == Category.CategoryType.INCOME) {
                totalIncome.merge(categoryName, amount, BigDecimal::add);
                netSavings = netSavings.add(amount);
            } else {
                totalExpenses.merge(categoryName, amount, BigDecimal::add);
                netSavings = netSavings.subtract(amount);
            }
        }

        MonthlyReportResponse response = new MonthlyReportResponse();
        response.setMonth(month);
        response.setYear(year);
        response.setTotalIncome(totalIncome);
        response.setTotalExpenses(totalExpenses);
        response.setNetSavings(netSavings);

        return response;
    }

    public YearlyReportResponse getYearlyReport(Long userId, int year) {
        LocalDate startDate = LocalDate.of(year, 1, 1);
        LocalDate endDate = LocalDate.of(year + 1, 1, 1);

        List<Transaction> transactions = transactionRepository.findTransactionsForPeriod(
                userId, startDate, endDate);

        Map<String, BigDecimal> totalIncome = new HashMap<>();
        Map<String, BigDecimal> totalExpenses = new HashMap<>();
        BigDecimal netSavings = BigDecimal.ZERO;

        for (Transaction t : transactions) {
            String categoryName = t.getCategory().getName();
            BigDecimal amount = t.getAmount();

            if (t.getCategory().getType() == Category.CategoryType.INCOME) {
                totalIncome.merge(categoryName, amount, BigDecimal::add);
                netSavings = netSavings.add(amount);
            } else {
                totalExpenses.merge(categoryName, amount, BigDecimal::add);
                netSavings = netSavings.subtract(amount);
            }
        }

        YearlyReportResponse response = new YearlyReportResponse();
        response.setYear(year);
        response.setTotalIncome(totalIncome);
        response.setTotalExpenses(totalExpenses);
        response.setNetSavings(netSavings);

        return response;
    }
}
