package com.manh.ecom_be.services.coupon;

public interface InterfaceCouponService {
    double calculateCouponValue(String couponCode, double totalAmount);
}
