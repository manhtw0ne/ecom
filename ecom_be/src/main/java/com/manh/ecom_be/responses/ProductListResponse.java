package com.manh.ecom_be.responses;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class ProductListResponse {
    private List<ProductResponse> products;

    @JsonProperty("total_pages")
    private long totalPages;
}
