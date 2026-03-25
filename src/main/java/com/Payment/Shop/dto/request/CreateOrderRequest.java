package com.Payment.Shop.dto.request;

import com.Payment.Shop.constant.PaymentMethod;
import com.Payment.Shop.entity.Address;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class CreateOrderRequest {

    private List<OrderItemRequest> productVariants;
    private Address address;
    private String note;
    private String receiverName;
    private String email; // thêm email
    private String phone;
    private PaymentMethod paymentMethod;
    private BigDecimal  totalAmount;
    private List<Long> voucherIds;
    private BigDecimal  shippingFee;
}
