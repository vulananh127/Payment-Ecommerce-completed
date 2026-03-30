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

    private Long categoryId;

    private String imageUrl;

    private List<CreateProductVariantRequest> variants;
}