package com.manh.ecom_be.dtos.payment;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaymentRefundDTO {
    @JsonProperty("order_id")
    private String orderId;

    @JsonProperty("amount")
    private long amount;

    @JsonProperty("transaction_type")
    private String transactionType;

    @JsonProperty("transaction_date")
    private String transactionDate;

    @JsonProperty("created_by")
    private String createdBy;

    @JsonProperty("ip_address")
    private String ipAddress;
}
