package com.finance.personalfinancemanager.controller;

import com.finance.personalfinancemanager.dto.report.MonthlyReportResponse;
import com.finance.personalfinancemanager.dto.report.YearlyReportResponse;
import com.finance.personalfinancemanager.service.ReportService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/monthly/{year}/{month}")
    public ResponseEntity<?> getMonthlyReport(
            @PathVariable int year,
            @PathVariable int month,
            HttpSession session) {

        // ====== VALIDATE MONTH (1-12) ======
        if (month < 1 || month > 12) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Month must be between 1 and 12"));
        }

        Long userId = (Long) session.getAttribute("userId");
        MonthlyReportResponse report = reportService.getMonthlyReport(userId, year, month);
        return ResponseEntity.ok(report);
    }

    @GetMapping("/yearly/{year}")
    public ResponseEntity<YearlyReportResponse> getYearlyReport(
            @PathVariable int year,
            HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        YearlyReportResponse report = reportService.getYearlyReport(userId, year);
        return ResponseEntity.ok(report);
    }
}