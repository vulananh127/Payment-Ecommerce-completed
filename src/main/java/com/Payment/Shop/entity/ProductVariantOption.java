package com.Payment.Shop.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductVariantOption {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String attribute;
    private String value;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_variant_id")
    private ProductVariant productVariant;
    public ProductVariantOption(String attribute, String value){
        this.attribute = attribute;
        this.value = value;
    }
}
