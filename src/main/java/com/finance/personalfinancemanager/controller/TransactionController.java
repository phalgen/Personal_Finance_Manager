package com.finance.personalfinancemanager.controller;

import com.finance.personalfinancemanager.dto.transaction.TransactionRequest;
import com.finance.personalfinancemanager.dto.transaction.TransactionResponse;
import com.finance.personalfinancemanager.dto.transaction.UpdateTransactionRequest;
import com.finance.personalfinancemanager.service.TransactionService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping
    public ResponseEntity<TransactionResponse> createTransaction(
            @Valid @RequestBody TransactionRequest request,
            HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        TransactionResponse response = transactionService.createTransaction(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<Map<String, List<TransactionResponse>>> getTransactions(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String category,
            HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        List<TransactionResponse> transactions = transactionService.getAllTransactions(
                userId, startDate, endDate, category);
        return ResponseEntity.ok(Map.of("transactions", transactions));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TransactionResponse> updateTransaction(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTransactionRequest request,
            HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        TransactionResponse response = transactionService.updateTransaction(id, request, userId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteTransaction(
            @PathVariable Long id,
            HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        transactionService.deleteTransaction(id, userId);
        return ResponseEntity.ok(Map.of("message", "Transaction deleted successfully"));
    }
}
