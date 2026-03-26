package com.Payment.Shop.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.Payment.Shop.constant.OrderStatus;
import com.Payment.Shop.constant.PaymentMethod;
import com.Payment.Shop.constant.PaymentStatus;
import com.Payment.Shop.entity.Address;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BaseOrderResponse {

    @JsonProperty("orderId")
    protected Long id;

    protected Address address;

    protected String note;

    protected String receiverName;

    protected String phone;

    protected String email;           // ← thêm

    protected PaymentMethod paymentMethod;

    protected PaymentStatus paymentStatus; // ← thêm

    protected OrderStatus orderStatus;

    protected BigDecimal totalAmount;

    protected BigDecimal shippingFee;  // ← thêm
    
    protected LocalDateTime createdAt; // ← thêm

    protected LocalDateTime updatedAt; // ← thêm (optional)

    protected List<OrderItemResponse> orderItems; // ← thêm — quan trọng nhất
}