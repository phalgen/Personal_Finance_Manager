package com.finance.personalfinancemanager.service;

import com.finance.personalfinancemanager.dto.transaction.TransactionRequest;
import com.finance.personalfinancemanager.dto.transaction.TransactionResponse;
import com.finance.personalfinancemanager.dto.transaction.UpdateTransactionRequest;
import com.finance.personalfinancemanager.entity.Category;
import com.finance.personalfinancemanager.entity.Transaction;
import com.finance.personalfinancemanager.entity.User;
import com.finance.personalfinancemanager.exception.ForbiddenException;
import com.finance.personalfinancemanager.exception.ResourceNotFoundException;
import com.finance.personalfinancemanager.repository.TransactionRepository;
import com.finance.personalfinancemanager.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final CategoryService categoryService;

    public TransactionService(TransactionRepository transactionRepository,
                             UserRepository userRepository,
                             CategoryService categoryService) {
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
        this.categoryService = categoryService;
    }

    @Transactional
    public TransactionResponse createTransaction(TransactionRequest request, Long userId) {
        if (request.getDate().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Transaction date cannot be in the future");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Category category = categoryService.getCategoryByName(request.getCategory(), userId);

        Transaction transaction = new Transaction();
        transaction.setAmount(request.getAmount());
        transaction.setDate(request.getDate());
        transaction.setCategory(category);
        transaction.setDescription(request.getDescription());
        transaction.setUser(user);
        transaction.setDeleted(false);

        transaction = transactionRepository.save(transaction);
        return toTransactionResponse(transaction);
    }

    public List<TransactionResponse> getAllTransactions(Long userId, LocalDate startDate, 
                                                        LocalDate endDate, String category) {
        List<Transaction> transactions;

        if (startDate != null && endDate != null && category != null) {
            Category cat = categoryService.getCategoryByName(category, userId);
            transactions = transactionRepository.findByUserIdAndDateBetweenAndCategoryIdAndDeletedFalse(
                    userId, startDate, endDate, cat.getId());
        } else if (startDate != null && endDate != null) {
            transactions = transactionRepository.findByUserIdAndDateBetweenAndDeletedFalse(
                    userId, startDate, endDate);
        } else if (category != null) {
            Category cat = categoryService.getCategoryByName(category, userId);
            transactions = transactionRepository.findByUserIdAndCategoryIdAndDeletedFalse(
                    userId, cat.getId());
        } else {
            transactions = transactionRepository.findByUserIdAndDeletedFalseOrderByDateDesc(userId);
        }

        return transactions.stream()
                .map(this::toTransactionResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public TransactionResponse updateTransaction(Long id, UpdateTransactionRequest request, Long userId) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));

        if (!transaction.getUser().getId().equals(userId)) {
            throw new ForbiddenException("You don't have permission to update this transaction");
        }

        if (transaction.getDeleted()) {
            throw new ResourceNotFoundException("Transaction not found");
        }

        if (request.getAmount() != null) {
            transaction.setAmount(request.getAmount());
        }
        if (request.getDescription() != null) {
            transaction.setDescription(request.getDescription());
        }

        transaction = transactionRepository.save(transaction);
        return toTransactionResponse(transaction);
    }

    @Transactional
    public void deleteTransaction(Long id, Long userId) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));

        if (!transaction.getUser().getId().equals(userId)) {
            throw new ForbiddenException("You don't have permission to delete this transaction");
        }

        transaction.markAsDeleted();
        transactionRepository.save(transaction);
    }

    public BigDecimal calculateNetSavings(Long userId, LocalDate startDate, LocalDate endDate) {
        List<Transaction> transactions = transactionRepository.findTransactionsForPeriod(
                userId, startDate, endDate);

        BigDecimal totalIncome = BigDecimal.ZERO;
        BigDecimal totalExpenses = BigDecimal.ZERO;

        for (Transaction t : transactions) {
            if (t.getCategory().getType() == Category.CategoryType.INCOME) {
                totalIncome = totalIncome.add(t.getAmount());
            } else {
                totalExpenses = totalExpenses.add(t.getAmount());
            }
        }

        return totalIncome.subtract(totalExpenses);
    }

    private TransactionResponse toTransactionResponse(Transaction transaction) {
        TransactionResponse response = new TransactionResponse();
        response.setId(transaction.getId());
        response.setAmount(transaction.getAmount());
        response.setDate(transaction.getDate());
        response.setCategory(transaction.getCategory().getName());
        response.setDescription(transaction.getDescription());
        response.setType(transaction.getType());
        return response;
    }
}
