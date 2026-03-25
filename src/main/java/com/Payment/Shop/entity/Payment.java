package com.Payment.Shop.entity;

import java.math.BigDecimal;
import java.time.Instant;

import com.Payment.Shop.constant.PaymentMethod;
import com.Payment.Shop.constant.PaymentStatus;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "payments")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private PaymentMethod paymentMethod;
    private BigDecimal  amount;
    // // Trạng thái của giao dịch
    // @Enumerated(EnumType.STRING)
    // private PaymentStatus status;
    private String transactionId;
    private String sessionId;
    private Instant createdAt;
    // không cần truyền createAt từ request
    @PrePersist
    public void prePersist() {
        createdAt = Instant.now();
    }
    @ManyToOne
    @JoinColumn(name="order_id")
    private Order order;
}
