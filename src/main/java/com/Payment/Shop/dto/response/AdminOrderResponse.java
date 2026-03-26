package com.Payment.Shop.dto.response;

import com.Payment.Shop.constant.OrderStatus;
import com.Payment.Shop.constant.PaymentStatus;
import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class AdminOrderResponse extends BaseOrderResponse {

    private PaymentStatus paymentStatus;

    // private OrderStatus orderStatus;
    
    private LocalDateTime createdAt;

    private String email;

}