package com.manh.ecom_be.components;

import org.springframework.beans.factory.annotation.Value;

public class VNPayConfig {
    @Value("${vnpay.pay-url}")
    private String vnpPayUrl;

    @Value("${vnpay.return-url}")
    private String vnpReturnUrl;

    @Value("${vnpay.tmn-code}")
    private String vnpTmnCode;

    @Value("${vnpay.secret-key}")
    private String secretKey;

    @Value("${vnpay.api-url")
    private String vnpApiUrl;
}
