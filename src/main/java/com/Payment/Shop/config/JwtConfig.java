package com.Payment.Shop.config;


import com.Payment.Shop.security.JwtUtil;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.nimbusds.jose.util.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import java.util.List;

@Configuration
public class JwtConfig {


    @Value("${app.jwt-secret}")
    private String jwtSecret;

    // Create Secret Key
    private SecretKey getSecretKey(){
        byte[] keyBytes = Base64.from(jwtSecret).decode();
        return new SecretKeySpec(keyBytes, 0, keyBytes.length , JwtUtil.JWT_ALGORITHM.getName());
    }

    @Bean
    public JwtDecoder jwtDecoder(){
        NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder.withSecretKey(
                getSecretKey()).macAlgorithm(JwtUtil.JWT_ALGORITHM).build();

        return token -> {
            try{
                return jwtDecoder.decode(token);
            } catch (Exception e) {
                System.out.println("JWT error: " + e.getMessage());
                throw new RuntimeException(e);
            }
        };
    }

    @Bean
    public JwtEncoder jwtEncoder(){
        return new NimbusJwtEncoder(new ImmutableSecret<>(getSecretKey()));
    }

    @Bean
public JwtAuthenticationConverter jwtAuthenticationConverter() {
    JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
    converter.setJwtGrantedAuthoritiesConverter(jwt -> {
        // Log xem JWT có những claim gì
        System.out.println("=== JWT Claims: " + jwt.getClaims());
        System.out.println("=== authorities claim: " + jwt.getClaim("authorities"));

        Object authClaim = jwt.getClaim("authorities");
        if (authClaim == null) return List.of();

        if (authClaim instanceof List<?> list) {
            return list.stream()
                .map(a -> new SimpleGrantedAuthority(a.toString()))
                .collect(java.util.stream.Collectors.toList());
        }

        if (authClaim instanceof String str) {
            return List.of(new SimpleGrantedAuthority(str));
        }

        return List.of();
    });
    return converter;
}

}
