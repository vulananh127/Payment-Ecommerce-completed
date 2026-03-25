package com.Payment.Shop.entity.Id;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Embeddable
@AllArgsConstructor
@Getter
@Setter
public class OrderVoucherId {
    private Long orderId;
    private Long voucherId;
    public OrderVoucherId() {
    }
}
