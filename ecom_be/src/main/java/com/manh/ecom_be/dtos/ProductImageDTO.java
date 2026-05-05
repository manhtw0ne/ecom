package com.manh.ecom_be.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Size;

public class ProductImageDTO {
    @JsonProperty("product_id")
    private Long productId;

    @JsonProperty("image_url")
    @Size(min = 5, max = 300, message = "Image URL must be between 5 and 300 characters")
    private String imageUrl;
}


