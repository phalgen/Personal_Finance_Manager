package com.finance.personalfinancemanager.repository;

import com.finance.personalfinancemanager.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    Optional<Category> findByNameAndUserId(String name, Long userId);

    boolean existsByNameAndUserId(String name, Long userId);

    @Query("SELECT c FROM Category c WHERE c.user.id = :userId OR c.isCustom = false")
    List<Category> findByUserIdOrIsCustomFalse(Long userId);

    List<Category> findByUserId(Long userId);

    void deleteByNameAndUserId(String name, Long userId);
}
