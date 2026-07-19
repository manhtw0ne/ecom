package com.manh.ecom_be.dtos.payment;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentDTO {
    private long amount;

    @JsonProperty("bank_code")
    private String bankCode;

    private String language;
}
