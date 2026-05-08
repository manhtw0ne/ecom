package com.manh.ecom_be.controllers;


import com.manh.ecom_be.dtos.payment.PaymentDTO;
import com.manh.ecom_be.responses.ResponseObject;
import com.manh.ecom_be.services.vnpay.VNPayService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("${api.prefix}/payments")
@RequiredArgsConstructor
public class PaymentController {
    private final VNPayService vnPayService;

    @PostMapping("/create_payment_url")
    public ResponseEntity<ResponseObject> createPaymentUrl(
            @RequestBody PaymentDTO paymentRequest,
            HttpServletRequest request
    ) {
        try {
            String url = vnPayService.createPaymentUrl(paymentRequest, request);
            return ResponseEntity.ok(ResponseObject.builder()
                            .status(HttpStatus.OK)
                            .message("Payment URL generated successfully")
                            .data(url).build()
                    );
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(ResponseObject.builder()
                    .message("Error: " + e.getMessage()).build());
        }
    }

    @GetMapping("/vn-pay-callback")
    public ResponseEntity<ResponseObject> payCallbackHandler(HttpServletRequest request) {
        String responseCode = request.getParameter("vnp_ResponseCode");
        String txnRef = request.getParameter("vnp_TxnRef");

        if ("00".equals(responseCode)) {
            return ResponseEntity.ok(ResponseObject.builder()
                    .status(HttpStatus.OK)
                    .message("Payment successful")
                    .data(Map.of("vnp_TxnRef", txnRef, "vnp_ResponseCode", responseCode))
                    .build());
        } else {
            return ResponseEntity.badRequest().body(ResponseObject.builder()
                    .status(HttpStatus.BAD_REQUEST)
                    .message("Payment failed. Code: " + responseCode).build());
        }
    }
}
