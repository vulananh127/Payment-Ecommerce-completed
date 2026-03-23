package com.Payment.Shop.controller;

import com.Payment.Shop.dto.ResultObject;
import com.Payment.Shop.dto.request.CreateCategoryRequest;
import com.Payment.Shop.dto.request.UpdateCategoryRequest;
import com.Payment.Shop.dto.response.BaseCategoryResponse;
import com.Payment.Shop.service.category.ICategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class CategoryController {

    private final ICategoryService categoryService;

    // PUBLIC
    @GetMapping("/api/v1/categories")
    public ResponseEntity<ResultObject> getAllCategories() {
        List<BaseCategoryResponse> categories = categoryService.getAllCategories();
        ResultObject<List<BaseCategoryResponse>> result = new ResultObject<>(
                true, "Get categories successfully", HttpStatus.OK, categories);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @GetMapping("/api/v1/categories/{id}")
    public ResponseEntity<ResultObject> getCategoryById(@PathVariable Long id) {
        BaseCategoryResponse category = categoryService.getCategoryById(id);
        ResultObject<BaseCategoryResponse> result = new ResultObject<>(
                true, "Get category successfully", HttpStatus.OK, category);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    // ADMIN
    @PostMapping("/api/v1/admin/categories")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SHOP')")
    public ResponseEntity<ResultObject> createCategory(
            @Valid @RequestBody CreateCategoryRequest request) throws Exception {
        BaseCategoryResponse category = categoryService.createCategory(request);
        ResultObject<BaseCategoryResponse> result = new ResultObject<>(
                true, "Create category successfully", HttpStatus.CREATED, category);
        return new ResponseEntity<>(result, HttpStatus.CREATED);
    }

    @PutMapping("/api/v1/admin/categories/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SHOP')")
    public ResponseEntity<ResultObject> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCategoryRequest request) {
        BaseCategoryResponse category = categoryService.updateCategory(id, request);
        ResultObject<BaseCategoryResponse> result = new ResultObject<>(
                true, "Update category successfully", HttpStatus.OK, category);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @DeleteMapping("/api/v1/admin/categories/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SHOP')")
    public ResponseEntity<ResultObject> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        ResultObject<Void> result = new ResultObject<>(
                true, "Delete category successfully", HttpStatus.OK, null);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }
}