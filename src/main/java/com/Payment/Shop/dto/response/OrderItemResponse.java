package com.Payment.Shop.dto.response;

import java.math.BigDecimal;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemResponse {

    protected Long id;

    protected String productName;

    protected String variantName;

    protected Integer quantity;

    protected BigDecimal unitPrice;

    protected BigDecimal totalPrice;

    protected Long productVariantId;   // ← để frontend dùng nếu cần

     protected String imageUrl;

}