package com.Payment.Shop.entity;

import java.math.BigDecimal;

import com.Payment.Shop.constant.PaymentMethod;
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

    private String transactionId;

    private String sessionId;

    private String createdAt;

    @Column(nullable = false)
    private Long orderId;

}
