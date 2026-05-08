package com.manh.ecom_be.dtos;


import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor

public class OrderDTO {
    @JsonProperty("user_id")
    private Long userId;

    @JsonProperty("fullname")
    private String fullName;

    private String phoneNumber;

    private String address;
    private String note;

    @JsonProperty("total_money")
    @Min(value = 0)
    private Float totalMoney;

    @JsonProperty("shipping_method")
    private String shippingMethod;

    @JsonProperty("shipping_address")
    private String shippingAddress;

    @JsonProperty("shipping_date")
    private LocalDate shippingDate;

    @JsonProperty("payment_method")
    private String paymentMethod;

    @JsonProperty("coupon_code")
    private String couponCode;

    @JsonProperty("vnp_txn_ref")
    private String vnpTxnRef;

    @JsonProperty("cart_items")
    private CartItemDTO[] cartItems;

}
