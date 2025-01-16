package kr.hhplus.be.server.domain.coupon.dto;

import kr.hhplus.be.server.domain.coupon.entity.Coupon;

import java.time.LocalDateTime;

public record CouponDto(Long id, String name, double discountAmount, LocalDateTime expiredAt) {
    public static CouponDto of(Coupon coupon) {
        return new CouponDto(coupon.getId(), coupon.getName(), coupon.getDiscountAmount(), coupon.getExpiredAt());
    }
}