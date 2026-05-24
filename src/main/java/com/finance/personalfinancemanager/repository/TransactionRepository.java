package com.finance.personalfinancemanager.repository;

import com.finance.personalfinancemanager.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    
    List<Transaction> findByUserIdAndDeletedFalseOrderByDateDesc(Long userId);
    
    List<Transaction> findByUserIdAndDeletedFalse(Long userId);
    
    List<Transaction> findByUserIdAndDateBetweenAndDeletedFalse(Long userId, LocalDate startDate, LocalDate endDate);
    
    List<Transaction> findByUserIdAndCategoryIdAndDeletedFalse(Long userId, Long categoryId);
    
    List<Transaction> findByUserIdAndDateBetweenAndCategoryIdAndDeletedFalse(
        Long userId, LocalDate startDate, LocalDate endDate, Long categoryId);
    
    long countByCategoryIdAndDeletedFalse(Long categoryId);
    
    @Query("SELECT t FROM Transaction t WHERE t.user.id = :userId AND t.deleted = false " +
           "AND t.date >= :startDate AND t.date < :endDate")
    List<Transaction> findTransactionsForPeriod(Long userId, LocalDate startDate, LocalDate endDate);
}
