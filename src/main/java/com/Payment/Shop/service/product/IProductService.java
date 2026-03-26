package com.Payment.Shop.service.product;

import com.Payment.Shop.dto.request.CreateProductRequest;
import com.Payment.Shop.dto.request.UpdateProductRequest;
import com.Payment.Shop.dto.response.BaseProductResponse;
import com.Payment.Shop.entity.ProductVariant;

import java.util.List;

public interface IProductService {

    // Existing methods
    BaseProductResponse createProduct(CreateProductRequest createProductRequest);
    List<ProductVariant> findAllProductVariantByVariantId(List<Long> variantIds);
    List<ProductVariant> findAllProductVariantByVariantIdWithLock(List<Long> variantIds);
    void saveAllProductVariant(List<ProductVariant> productVariants);
    int updateStockOptimistic(Long variantId, Integer requestQuantity);

    // New methods
    List<BaseProductResponse> getAllProducts(String name, Long categoryId);
    BaseProductResponse getProductById(Long id);
    BaseProductResponse updateProduct(Long id, UpdateProductRequest request);
    void deleteProduct(Long id);

    void increaseStock(Long variantId, Integer qty);
    List<ProductVariant> findAllProductVariantByVariantIdWithProduct(List<Long> variantIds);
}