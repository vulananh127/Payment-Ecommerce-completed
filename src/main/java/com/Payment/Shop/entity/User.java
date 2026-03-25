package com.Payment.Shop.entity;

import com.Payment.Shop.constant.Role;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Entity
@Table(name = "users")
@Getter
@Setter
public class User extends Account{

    @Column(name = "birthday")
    private Date birthday;

    public User() {
        super();
        this.role = Role.USER;
    }

    // Constructor with fields
    public User(String email, String password, String name, Date birthday) {
        super(email, password, Role.USER);
        this.name = name;
        this.birthday = birthday;

    }
}
