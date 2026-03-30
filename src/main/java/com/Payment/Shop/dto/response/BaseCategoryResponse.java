// ===== BaseCategoryResponse.java =====
package com.Payment.Shop.dto.response;
 
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
 
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BaseCategoryResponse {
 
    private Long id;
    private String categoryName;
    private Instant createdAt;
    private Long productCount;
}