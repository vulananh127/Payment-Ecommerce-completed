package com.Payment.Shop.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "voucher")
@Getter
@Setter
public class Voucher {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String code;
    private double discountPercent;
    private String description;
    private boolean isActive;
    private Instant createdAt;
    private Instant updatedAt;
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
