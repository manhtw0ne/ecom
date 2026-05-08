package com.manh.ecom_be.services.coupon;


import com.manh.ecom_be.models.Coupon;
import com.manh.ecom_be.models.CouponCondition;
import com.manh.ecom_be.models.User;
import com.manh.ecom_be.repositories.CouponConditionRepository;
import com.manh.ecom_be.repositories.CouponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CouponService implements InterfaceCouponService {
    private final CouponRepository couponRepository;
    private final CouponConditionRepository couponConditionRepository;

    @Override
    public double calculateCouponValue(String couponCode, double totalAmount) {
        Coupon coupon = couponRepository.findByCode(couponCode)
        .orElseThrow(() -> new IllegalArgumentException("Coupon not found: " + couponCode));

        if (!coupon.isActive()) {
            throw new IllegalArgumentException("Coupon is not active");
        }

        double discount = calculateDiscount(coupon, totalAmount);

        return Math.max(totalAmount - discount, 0);
    }

    private double calculateDiscount(Coupon coupon, double totalAmount) {
        List<CouponCondition> conditions
                = couponConditionRepository.findByCouponId(coupon.getId());

        double discount = 0.0;
        double updatedTotal = totalAmount;

        for (CouponCondition condition : conditions) {
            String attribute = condition.getAttribute();
            String operator = condition.getOperator();
            String value = condition.getValue();
            double percentDiscount = condition.getDiscountAmount().doubleValue();

            switch (attribute) {
                case "minimum_amount" -> {
                    if (">".equals(operator) && updateTotal > Double.parseDouble(value)) {
                        discount += updatedTotal * percentDiscount/100;
                    }
                }

                case "appicable_date" -> {
                    LocalDate applicableDate = LocalDate.parse(value);
                    if ("BETWEEN".equalsIgnoreCase(operator)
                    && LocalDate.now().isEqual(applicableDate)) {
                        discount += updatedTotal * percentDiscount/100;
                    }
                }

                case "user_type" -> {
                    User currentUser = securityUtils.getLoggedInUser();
                    if ("=".equals(operator) && value.equals(currentUser.getUserType())) {
                        discount += updatedTotal * percentDiscount/100;
                    }
                }
            }
            updatedTotal -= discount;
        }
        return discount;
    }
}
