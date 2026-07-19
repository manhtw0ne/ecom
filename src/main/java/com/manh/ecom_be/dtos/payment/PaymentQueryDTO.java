package com.manh.ecom_be.dtos.payment;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentQueryDTO {
    @JsonProperty("order_id")
    private String orderId;

    @JsonProperty("trans_date")
    private String transDate;

    @JsonProperty("ip_address")
    private String ipAddress;

}
