package com.manh.ecom_be.responses.order;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.manh.ecom_be.models.Order;
import com.manh.ecom_be.models.OrderDetail;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderResponse {
    private Long id;
    @JsonProperty("user_id")
    private String fullName;
    private String email;
    private String phoneNumber;
    private String address;
    private String note;
    private LocalDateTime orderDate;
    private String status;
    private Float totalMoney;
    private String shippingMethod;
    private String shippingAddress;
    private LocalDate shippingDate;
    private String paymentMethod;
    private Boolean active;
    private List<OrderDetail> orderDetails;

    public static OrderResponse fromOrder(Order order) {
        return OrderResponse.builder()
                .id(order.getId())
                .userId(order.getUser().getId())
                .fullName(order.getFullName())
                .email(order.getEmail())
                .phoneNumber(order.getPhoneNumber())
                .address(order.getAddress())
                .note(order.getNote())
                .orderDate(order.getOrderDate())
                .status(order.getStatus())
                .totalMoney(order.getTotalMoney())
                .shippingMethod(order.getShippingMethod())
                .shippingAddress(order.getShippingAddress())
                .shippingDate(order.getShippingDate())
                .paymentMethod(order.getPaymentMethod())
                .active(order.getActive())
                .orderDetails(order.getOrderDetails())
                .build();
    }
}
