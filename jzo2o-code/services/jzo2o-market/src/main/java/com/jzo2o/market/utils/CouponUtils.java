package com.jzo2o.market.utils;

import com.jzo2o.market.enums.ActivityTypeEnum;
import com.jzo2o.market.model.domain.Coupon;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 优惠券相关工具
 */
public class CouponUtils {

    private CouponUtils() {
    }

    /**
     * 计算优惠金额
     * 满减：直接取优惠券优惠金额
     * 折扣：订单金额 * (1 - 折扣率/100)，例如 7 折(discountRate=70) → 优惠 30%
     *
     * @param coupon      优惠券
     * @param totalAmount 订单金额
     * @return 优惠金额
     */
    public static BigDecimal calDiscountAmount(Coupon coupon, BigDecimal totalAmount) {
        if (coupon == null || totalAmount == null) {
            return BigDecimal.ZERO;
        }
        if (ActivityTypeEnum.AMOUNT_DISCOUNT.equals(coupon.getType())) {
            return coupon.getDiscountAmount() == null ? BigDecimal.ZERO : coupon.getDiscountAmount();
        }
        if (ActivityTypeEnum.RATE_DISCOUNT.equals(coupon.getType())) {
            if (coupon.getDiscountRate() == null) {
                return BigDecimal.ZERO;
            }
            // 优惠比例 = (100 - discountRate) / 100
            return totalAmount.multiply(BigDecimal.valueOf(100 - coupon.getDiscountRate()))
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        }
        return BigDecimal.ZERO;
    }
}
