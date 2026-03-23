package com.Payment.Shop.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class UpdateProductRequest {

    @NotBlank(message = "Product name must not be blank")
    private String name;

    private String description;

    @NotNull(message = "Base price must not be null")
    @Min(value = 0, message = "Base price must be >= 0")
    private Double basePrice;

    @Min(value = 0, message = "Discount must be >= 0")
    private Double discountPercent = 0.0;

    private Long categoryId;

    private String imageUrl;

    // Optional - nếu null thì giữ nguyên variants cũ
    // nếu có thì xóa cũ và thêm mới
    private List<CreateProductVariantRequest> variants;
}