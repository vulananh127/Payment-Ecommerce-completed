package com.Payment.Shop.constant;

public enum PaymentStatus {
    UNPAID,              // chưa thanh toán (COD)
    PAYMENT_PROCESSING,  // đang xử lý (VNPay redirect)
    PAYMENT_COMPLETED,   // đã thanh toán
    PAYMENT_FAILED       // thất bại
}