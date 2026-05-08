package com.manh.ecom_be.components;


import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.TimeZone;

@Component
public class VNPayUtils {
    public String getRandomNumber(int len) {
        String chars = "0123456789";
        StringBuilder sb = new StringBuilder();
        Random rnd = new Random();
        for (int i = 0; i < len; i++) sb.append(chars.charAt(rnd.nextInt(chars.length())));
        return sb.toString();
    }

    public String getIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        return (ip == null) ? request.getRemoteAddr() : ip;
    }

    public String getCurrentDateTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        sdf.setTimeZone(TimeZone.getTimeZone("Etc/GMT+7"));
        return sdf.format(new Date());
    }

    public String hmacSHA512(String key, String data) {
        try {
            Mac hmac = Mac.getInstance("HmacSHA512");
            hmac.init(new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512"));
            byte[] result = hmac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(2 * result.length);
            for (byte b : result) sb.append(String.format("%02x", b & 0xff));
            return sb.toString();
        } catch (Exception ex) {
            return "";
        }
    }
}
