package com.manh.ecom_be.services.vnpay;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.manh.ecom_be.components.VNPayConfig;
import com.manh.ecom_be.components.VNPayUtils;
import com.manh.ecom_be.dtos.payment.PaymentDTO;
import com.manh.ecom_be.dtos.payment.PaymentQueryDTO;
import com.manh.ecom_be.dtos.payment.PaymentRefundDTO;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.*;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
@RequiredArgsConstructor
public class VNPayService implements InterfaceVNPayService {

    private final VNPayConfig vnPayConfig;
    private final VNPayUtils vnPayUtils;

    @Override
    public String createPaymentUrl(PaymentDTO paymentDto, HttpServletRequest request) {
        long amount = paymentDto.getAmount() * 100;
        String transactionRef = vnPayUtils.getRandomNumber(8);
        String clientIp = vnPayUtils.getIpAddress(request);

        Map<String, String> params = new HashMap<>();
        params.put("vnp_Version", "2.1.0");
        params.put("vnp_Command", "pay");
        params.put("vnp_TmnCode", vnPayConfig.getVnpTmnCode());
        params.put("vnp_Amount", String.valueOf(amount));
        params.put("vnp_CurrCode", "VND");
        params.put("vnp_TxnRef", transactionRef);
        params.put("vnp_OrderInfo", "Thanh toan don hang:" + transactionRef);
        params.put("vnp_OrderType", "other");

        String locale = paymentDto.getLanguage();
        if (locale != null && !locale.isEmpty()) {
            params.put("vnp_Locale", locale);
        } else {
            params.put("vnp_Locale", "vn");
        }

        params.put("vnp_ReturnUrl", vnPayConfig.getVnpReturnUrl());
        params.put("vnp_IpAddr", clientIp);

        SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMddHHmmss");
        fmt.setTimeZone(TimeZone.getTimeZone("Etc/GMT+7"));
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        params.put("vnp_CreateDate", fmt.format(cal.getTime()));
        cal.add(Calendar.MINUTE, 15);
        params.put("vnp_ExpireDate", fmt.format(cal.getTime()));

        List<String> fieldNames = new ArrayList<>(params.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        StringBuilder queryData = new StringBuilder();

        for (Iterator<String> it = fieldNames.iterator(); it.hasNext();) {
            String name = it.next();
            String value = params.get(name);
            if (value != null && !value.isEmpty()) {
                hashData.append(name).append('=')
                        .append(URLEncoder.encode(value, StandardCharsets.US_ASCII));
                queryData.append(URLEncoder.encode(name, StandardCharsets.US_ASCII))
                        .append('=')
                        .append(URLEncoder.encode(value, StandardCharsets.US_ASCII));
                if (it.hasNext()) {
                    hashData.append('&');
                    queryData.append('&');
                }
            }
        }

        String secureHash = vnPayUtils.hmacSHA512(vnPayConfig.getSecretKey(), hashData.toString());
        return vnPayConfig.getVnpPayUrl() + "?" + queryData + "&vnp_SecureHash=" + secureHash;
    }

    @Override
    public String queryTransaction(PaymentQueryDTO queryDto, HttpServletRequest httpRequest) throws IOException {
        String requestId = vnPayUtils.getRandomNumber(8);
        String version = "2.1.0";
        String command = "querydr";
        String terminalCode = vnPayConfig.getVnpTmnCode();
        String transactionReference = queryDto.getOrderId();
        String transactionDate = queryDto.getTransDate();
        String createDate = vnPayUtils.getCurrentDateTime();
        String clientIpAddress = vnPayUtils.getIpAddress(httpRequest);

        Map<String, String> params = new HashMap<>();
        params.put("vnp_RequestId", requestId);
        params.put("vnp_Version", version);
        params.put("vnp_Command", command);
        params.put("vnp_TmnCode", terminalCode);
        params.put("vnp_TxnRef", transactionReference);
        params.put("vnp_OrderInfo", "Check transaction result for OrderId:" + transactionReference);
        params.put("vnp_TransactionDate", transactionDate);
        params.put("vnp_CreateDate", createDate);
        params.put("vnp_IpAddr", clientIpAddress);

        String hashData = String.join("|", requestId, version, command,
                terminalCode, transactionReference, transactionDate, createDate, clientIpAddress, "Check transaction");
        String secureHash = vnPayUtils.hmacSHA512(vnPayConfig.getSecretKey(), hashData);
        params.put("vnp_SecureHash", secureHash);

        URL apiUrl = new URL(vnPayConfig.getVnpApiUrl());
        HttpURLConnection connection = (HttpURLConnection) apiUrl.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);

        try (DataOutputStream writer = new DataOutputStream(connection.getOutputStream())) {
            writer.writeBytes(new Gson().toJson(params));
        }

        int responseCode = connection.getResponseCode();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            if (responseCode == 200) {
                return response.toString();
            } else {
                throw new RuntimeException("VNPay API Error: " + response.toString());
            }
        }
    }

    @Override
    public String refundTransaction(PaymentRefundDTO refundRequest) throws IOException {
        String requestId = vnPayUtils.getRandomNumber(8);
        String version = "2.1.0";
        String command = "refund";
        String terminalCode = vnPayConfig.getVnpTmnCode();

        Map<String, String> params = new LinkedHashMap<>();
        params.put("vnp_RequestId", requestId);
        params.put("vnp_Version", version);
        params.put("vnp_Command", command);
        params.put("vnp_TmnCode", terminalCode);
        params.put("vnp_TransactionType", refundRequest.getTransactionType());
        params.put("vnp_TxnRef", refundRequest.getOrderId());
        params.put("vnp_Amount", String.valueOf(refundRequest.getAmount() * 100));
        params.put("vnp_OrderInfo", "Refund for OrderId: " + refundRequest.getOrderId());
        params.put("vnp_TransactionDate", refundRequest.getTransactionDate());
        params.put("vnp_CreateBy", refundRequest.getCreatedBy());
        params.put("vnp_IpAddr", refundRequest.getIpAddress());

        String hashData = String.join("|",
                requestId, version, command, terminalCode,
                refundRequest.getTransactionType(),
                refundRequest.getOrderId(),
                String.valueOf(refundRequest.getAmount() * 100),
                refundRequest.getTransactionDate(),
                refundRequest.getCreatedBy(),
                refundRequest.getIpAddress(),
                "Refund for OrderId: " + refundRequest.getOrderId());
        String secureHash = vnPayUtils.hmacSHA512(vnPayConfig.getSecretKey(), hashData);
        params.put("vnp_SecureHash", secureHash);

        URL apiUrl = new URL(vnPayConfig.getVnpApiUrl());
        HttpURLConnection connection = (HttpURLConnection) apiUrl.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);

        try (OutputStream outputStream = connection.getOutputStream()) {
            byte[] jsonPayload = new ObjectMapper().writeValueAsBytes(params);
            outputStream.write(jsonPayload, 0, jsonPayload.length);
        }

        int responseCode = connection.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK) {
            throw new RuntimeException("Failed to process refund. Response code: " + responseCode);
        }

        try (BufferedReader bufferedReader = new BufferedReader(
                new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
            StringBuilder responseBuilder = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                responseBuilder.append(line.trim());
            }
            return responseBuilder.toString();
        }
    }
}
