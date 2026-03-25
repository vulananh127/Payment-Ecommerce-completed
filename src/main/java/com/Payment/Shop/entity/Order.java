package com.Payment.Shop.entity;

import com.Payment.Shop.constant.OrderStatus;
import com.Payment.Shop.constant.PaymentMethod;
import com.Payment.Shop.constant.PaymentStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Builder
@Getter
@Setter
@AllArgsConstructor
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String email;
    private String phone;
    private String receiverName;
    private BigDecimal  totalAmount;
    @Enumerated(EnumType.STRING)
    private OrderStatus orderStatus;
    // Trạng thái tổng giao dịch của đơn
    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus;
    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;
    private String note;
    private BigDecimal  shippingFee;
    private Instant createdAt;
    private Instant updatedAt;
    @Embedded
    private Address address;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
    @OneToMany(mappedBy = "order", orphanRemoval = true)
    private List<OrderItem> orderItems;
    public Order() {}
    public Order(Long orderId){
        this.id = orderId;
    }
    public void addOrderItem(OrderItem orderItem) {
        if(this.orderItems == null){
            this.orderItems = new ArrayList<>();
        }
        orderItems.add(orderItem);
    }
    @PrePersist
    public void prePersist() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }
    @PreUpdate
    public void preUpdate() {
        updatedAt = Instant.now();
    }
}
