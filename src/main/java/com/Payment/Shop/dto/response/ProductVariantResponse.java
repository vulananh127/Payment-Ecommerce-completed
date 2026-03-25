package com.Payment.Shop.dto.response;

import lombok.Getter;
import lombok.Setter;
import java.util.Map;

@Getter
@Setter
public class ProductVariantResponse {

    private Long id; 
    private String sku;

    private double price;

    private int stockQuantity;

    private Map<String, String> attributes;

}

