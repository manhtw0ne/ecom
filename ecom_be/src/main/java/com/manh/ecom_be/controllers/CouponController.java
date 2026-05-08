package com.manh.ecom_be.controllers;


import com.manh.ecom_be.responses.ResponseObject;
import com.manh.ecom_be.responses.coupon.CouponCalculationResponse;
import com.manh.ecom_be.services.coupon.CouponService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("${api.prefix}/coupons")
@RequiredArgsConstructor
public class CouponController {
    private final CouponService couponService;

    @GetMapping("/calculate")
    public ResponseEntity<ResponseObject> calculateCouponValue(
            @RequestParam("couponCode") String couponCode,
            @RequestParam("totalAmount") double totalAmount
    ) {
        try {

        double finalAmount = couponService.calculateCouponValue(couponCode, totalAmount);
        return ResponseEntity.ok(ResponseObject.builder()
                .message("Calculate coupon successfully")
                .status(httpStatus.OK)
                .data(CouponCalculationResponse.builder().result(finalAmount).build())
                .build());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ResponseObject.builder()
                    .message(e.getMessage())
                    .status(HttpStatus.BAD_REQUEST).build());
        }
    }
}
