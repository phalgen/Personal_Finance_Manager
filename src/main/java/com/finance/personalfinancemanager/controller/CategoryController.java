package com.finance.personalfinancemanager.controller;

import com.finance.personalfinancemanager.dto.category.CategoryRequest;
import com.finance.personalfinancemanager.dto.category.CategoryResponse;
import com.finance.personalfinancemanager.service.CategoryService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public ResponseEntity<Map<String, List<CategoryResponse>>> getAllCategories(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        List<CategoryResponse> categories = categoryService.getAllCategoriesForUser(userId);
        return ResponseEntity.ok(Map.of("categories", categories));
    }

    @PostMapping
    public ResponseEntity<CategoryResponse> createCategory(
            @Valid @RequestBody CategoryRequest request,
            HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        CategoryResponse response = categoryService.createCustomCategory(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/{name}")
    public ResponseEntity<Map<String, String>> deleteCategory(
            @PathVariable String name,
            HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        categoryService.deleteCustomCategory(name, userId);
        return ResponseEntity.ok(Map.of("message", "Category deleted successfully"));
    }
}
