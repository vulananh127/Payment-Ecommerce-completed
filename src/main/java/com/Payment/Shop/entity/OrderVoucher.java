package com.Payment.Shop.entity;

import com.Payment.Shop.entity.Id.OrderVoucherId;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Getter
@Setter
public class OrderVoucher {
    @EmbeddedId
    private OrderVoucherId id;
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("orderId")
    @JoinColumn(name = "order_id")
    private Order order;
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("voucherId")
    @JoinColumn(name = "voucher_id")
    private Voucher voucher;
    private Instant usedAt;
    @PrePersist
    public void prePersist() {
        usedAt = Instant.now();
    }
}
