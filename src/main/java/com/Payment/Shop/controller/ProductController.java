package com.Payment.Shop.controller;

import com.Payment.Shop.dto.ResultObject;
import com.Payment.Shop.dto.request.CreateProductRequest;
import com.Payment.Shop.dto.request.UpdateProductRequest;
import com.Payment.Shop.dto.response.BaseProductResponse;
import com.Payment.Shop.service.product.IProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ProductController {

    private final IProductService productService;

        // PUBLIC
    @GetMapping("/api/v1/products")
    public ResponseEntity<ResultObject> getAllProducts(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Long categoryId) {
        List<BaseProductResponse> products = productService.getAllProducts(name, categoryId);
        ResultObject<List<BaseProductResponse>> result = new ResultObject<>(
                true, "Get products successfully", HttpStatus.OK, products);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @GetMapping("/api/v1/products/{id}")
    public ResponseEntity<ResultObject> getProductById(@PathVariable Long id) {
        BaseProductResponse product = productService.getProductById(id);
        ResultObject<BaseProductResponse> result = new ResultObject<>(
                true, "Get product successfully", HttpStatus.OK, product);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    // ADMIN
    @PostMapping("/api/v1/admin/products")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SHOP')")
    public ResponseEntity<ResultObject> createProduct(
            @Valid @RequestBody CreateProductRequest request) {
        BaseProductResponse product = productService.createProduct(request);
        ResultObject<BaseProductResponse> result = new ResultObject<>(
                true, "Create product successfully", HttpStatus.CREATED, product);
        return new ResponseEntity<>(result, HttpStatus.CREATED);
    }

    @PutMapping("/api/v1/admin/products/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SHOP')")
    public ResponseEntity<ResultObject> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody UpdateProductRequest request) {
        BaseProductResponse product = productService.updateProduct(id, request);
        ResultObject<BaseProductResponse> result = new ResultObject<>(
                true, "Update product successfully", HttpStatus.OK, product);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @DeleteMapping("/api/v1/admin/products/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SHOP')")
    public ResponseEntity<ResultObject> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        ResultObject<Void> result = new ResultObject<>(
                true, "Delete product successfully", HttpStatus.OK, null);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }
}