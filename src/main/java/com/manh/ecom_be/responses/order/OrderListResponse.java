package com.manh.ecom_be.responses.order;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.checkerframework.checker.units.qual.A;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class OrderListResponse {
    private List<OrderResponse> orders;
    private int totalPages;
    private int currentPage;
}
