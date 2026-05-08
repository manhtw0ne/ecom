package com.manh.ecom_be.services.vnpay;

import com.manh.ecom_be.dtos.payment.PaymentDTO;
import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;

public interface InterfaceVNPayService {
    String createPaymentUrl(PaymentDTO paymentDto, HttpServletRequest request);
    String queryTransaction(PaymentDTO queryDto, HttpServletRequest request) throws IOException;
}
