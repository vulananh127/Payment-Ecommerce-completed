package com.Payment.Shop.constant;

public enum OrderStatus {
    PENDING,        // mới tạo đơn
    CONFIRMED,      // shop xác nhận
    ON_DELIVERY,    // đang giao
    DELIVERED,      // đã giao
    CANCELED        // đã hủy
}
