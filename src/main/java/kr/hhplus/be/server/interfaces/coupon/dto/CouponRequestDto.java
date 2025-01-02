package kr.hhplus.be.server.interfaces.coupon.dto;

public record CouponRequestDto (
        String userId,
        long couponId
) {
}
