package com.Payment.Shop.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class BaseProductResponse {
    protected long id;

    protected String name;

    protected String description;

    protected String imageUrl;

    protected Double price;
    protected Integer stockQuantity;

    protected Instant createdAt;

    protected Instant updatedAt;

    protected CategoryResponse category;

    protected List<ProductVariantResponse> variants;
}
