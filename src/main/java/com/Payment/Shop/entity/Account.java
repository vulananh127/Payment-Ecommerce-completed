package com.Payment.Shop.entity;


import com.Payment.Shop.constant.Role;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "accounts")
@Inheritance(strategy = InheritanceType.JOINED)
//@DiscriminatorColumn(name = "role", discriminatorType = DiscriminatorType.STRING)
@Getter
@Setter
@AllArgsConstructor
public abstract class Account {

    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY, generator = "account_seq")
//    @SequenceGenerator(name = "account_seq", sequenceName = "account_sequence", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected long id;
    @Column(name = "email", nullable = false, unique = true)
    protected String email;
    @Column(name = "password", nullable = false)
    protected String password;
    @Column(name = "name", nullable = true)
    protected String name;
    @Enumerated(EnumType.STRING)
    @Column(name = "role", updatable = false) //  Prevent duplicate mapping
    protected Role role;
    @Column(name = "provider")
    protected String provider; // "google", "facebook", or null for regular auth
    @Column(name = "provider_id")
    protected String providerId; // User ID from OAuth provider
    protected Account() {}
    protected Account(String email, String password, Role role) {
        this.email = email;
        this.password = password;
        this.role = role;
    }
}
