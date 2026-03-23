// ===== UpdateCategoryRequest.java =====
package com.Payment.Shop.dto.request;
 
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
 
@Getter
@Setter
@NoArgsConstructor
public class UpdateCategoryRequest {
 
    @NotBlank(message = "Category name must not be blank")
    private String categoryName;
}