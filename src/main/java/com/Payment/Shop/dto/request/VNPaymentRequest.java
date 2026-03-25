package com.Payment.Shop.dto.request;

import java.math.BigDecimal;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
public class VNPaymentRequest extends PaymentRequest{
    protected BigDecimal totalAmount;
}
