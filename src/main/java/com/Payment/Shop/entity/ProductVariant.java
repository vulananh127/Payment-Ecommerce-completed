package com.Payment.Shop.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "product_variant")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductVariant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String sku;

    private int stock;

    private double price;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @Builder.Default
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "productVariant", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ProductVariantOption> productVariantOptions = new HashSet<>();

    public ProductVariant(Long id){
        this.id = id;
    }
}
