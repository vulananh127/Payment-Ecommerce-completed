package com.Payment.Shop.entity;

import com.Payment.Shop.entity.Id.StoreProductVariantId;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class StoreProductVariant {
    @EmbeddedId
    private StoreProductVariantId id;
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("storeId")
    @JoinColumn(name = "store_id")
    private Store store;
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("variantId")
    @JoinColumn(name = "product_variant_id")
    private ProductVariant productVariant;
    private Long stock;
}
