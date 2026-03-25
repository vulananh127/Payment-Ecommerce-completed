package com.Payment.Shop.entity.Id;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@AllArgsConstructor
public class StoreProductVariantId {
    private Long storeId;
    private Long variantId;
    public StoreProductVariantId() {
    }
}
