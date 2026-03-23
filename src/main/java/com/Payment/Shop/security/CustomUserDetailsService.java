package com.Payment.Shop.security;



import com.Payment.Shop.entity.Account;
import com.Payment.Shop.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Set;


@Service
public class CustomUserDetailsService implements UserDetailsService {

    private AccountRepository accountRepository;

    @Autowired
    public CustomUserDetailsService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

//     @Override
//     public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
//         Account user = accountRepository.findByEmail(usernameOrEmail)
//                 .orElseThrow(() -> new UsernameNotFoundException("User not found with username or email: " + usernameOrEmail));

// //        Set<GrantedAuthority> authorities = user.getRoles().stream().map(role -> new SimpleGrantedAuthority(role.getName())).collect(Collectors.toSet());
//         Set<GrantedAuthority> authorities = Collections.singleton(new SimpleGrantedAuthority("ROLE_" + user.getRole()));

//         return new org.springframework.security.core.userdetails.User(user.getEmail(), user.getPassword(), authorities);
//     }

    @Override
public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
    System.out.println("=== loadUserByUsername called with: " + usernameOrEmail);
    
    Account user = accountRepository.findByEmail(usernameOrEmail)
            .orElseThrow(() -> {
                System.out.println("=== User NOT FOUND: " + usernameOrEmail);
                return new UsernameNotFoundException("User not found: " + usernameOrEmail);
            });

    System.out.println("=== User FOUND: " + user.getEmail() + ", password: " + user.getPassword());

    Set<GrantedAuthority> authorities = Collections.singleton(
        new SimpleGrantedAuthority("ROLE_" + user.getRole())
    );

    return new org.springframework.security.core.userdetails.User(
        user.getEmail(), user.getPassword(), authorities
    );
}

}
