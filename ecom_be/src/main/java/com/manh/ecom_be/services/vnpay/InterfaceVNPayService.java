package com.manh.ecom_be.services.vnpay;

import com.manh.ecom_be.dtos.payment.PaymentDTO;
import com.manh.ecom_be.dtos.payment.PaymentQueryDTO;
import com.manh.ecom_be.dtos.payment.PaymentRefundDTO;
import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;

public interface InterfaceVNPayService {
    String createPaymentUrl(PaymentDTO paymentRequest, HttpServletRequest request);
    String queryTransaction(PaymentQueryDTO paymentQueryDTO, HttpServletRequest request) throws IOException;
    String refundTransaction(PaymentRefundDTO refundDTO) throws IOException;
}
