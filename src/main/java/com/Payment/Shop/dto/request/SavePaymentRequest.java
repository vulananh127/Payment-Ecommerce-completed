package com.Payment.Shop.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SavePaymentRequest extends PaymentRequest{

    // transactionId is used for VNPay payment, crucial to query the transaction
    private String transactionId;

    // sessionId is used for Stripe payment, crucial to retrieve checkout session
    // private String sessionId;

    private String createdAt;
}
