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


import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.*;
import java.text.SimpleDateFormat;
import java.util.*;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
@RequiredArgsConstructor
public class VNPayService implements InterfaceVNPayService {
    private final VNPayConfig vnPayConfig;

    long amount = paymentDto.getAmount() * 100;
    String transactionRef = vnPayUtils.getRandomNumber(8);
    String clientIp = vnPayUtils.getIpAddress(request);


    Map<String, String> params = new HashMap<>();
    params.put("vnp_Version","2.1.0");
    params.put("vnp_Command","pay");
    params.put("vnp_TmnCode",vnPayConfig.getVnpTmnCode());
    params.put("vnp_Amount",String.valueOf(amount));
    params.put("vnp_CurrCode","VND");
    params.put("vnp_TxnRef",transactionRef);
    params.put("vnp_OrderInfo","Thanh toan don hang:"+transactionRef);
    params.put("vnp_OrderType","other");
    params.put("vnp_Locale",
            (paymentDto.getLanguage()!=null&&!paymentDto.getLanguage().

    isEmpty())
            ?paymentDto.getLanguage():"vn");
    params.put("vnp_ReturnUrl",vnPayConfig.getVnpReturnUrl());
    params.put("vnp_IpAddr",clientIp);

    SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMddHHmmss");
    fmt.setTimeZone(TimeZone.getTimeZone("Etc/GMT+7"));
    Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
    params.put("vnp_CreateDate",fmt.format(cal.getTime()));
    cal.add(Calendar.MINUTE,15);
    params.put("vnp_ExpireDate",fmt.format(cal.getTime()));

    List<String> fieldNames = new ArrayList<>(params.keySet());
    Collections.sort(fieldNames);
    StringBuilder hashData = new StringBuilder();
    StringBuilder queryData = new StringBuilder();

    List<String> fieldNames = new ArrayList<>(params.keySet());
    Collections.sort(fieldNames);
    StringBuilder hashData = new StringBuilder();
    StringBuilder queryData = new StringBuilder();

    for (Iterator<String> it = fieldNames.iterator(); it.hasNext();)

    {
        String name = it.next();
        String value = params.get(name);
        if (value != null && !value.isEmpty()) {
            hashData.append(name).append('=')
                    .append(URLEncoder.encode(value, StandardCharsets.US_ASCII));
            queryData.append(URLEncoder.encode(name, StandardCharsets.US_ASCII))
                    .append('=')
                    .append(URLEncoder.encode(value, StandardCharsets.US_ASCII));
            if (it.hasNext()) { hashData.append('&'); queryData.append('&'); }
        }
    }

    String secureHash = vnPayUtils.hmacSHA512(vnPayConfig.getSecretKey(), hashData.toString());
    return vnPayConfig.getVnpPayUrl()+ "?" +queryData + "&vnp_SecureHash=" +secureHash;
}
