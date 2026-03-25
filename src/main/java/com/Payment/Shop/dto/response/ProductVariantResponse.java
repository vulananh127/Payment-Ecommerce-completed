package com.Payment.Shop.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Map;

@Getter
@Setter
public class ProductVariantResponse {
    private Long id; 
    private String sku;
    private BigDecimal price;
    private int stockQuantity;
    private Map<String, String> attributes;
}

