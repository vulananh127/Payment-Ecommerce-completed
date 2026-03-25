package com.Payment.Shop.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Getter
@Setter
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @Column(unique = true)
    private String categoryName;
    private Instant createdAt;
    @PrePersist
    public void prePersist() {
        createdAt = Instant.now();
    }
}
