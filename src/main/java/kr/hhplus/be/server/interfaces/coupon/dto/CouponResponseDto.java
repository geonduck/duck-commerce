package kr.hhplus.be.server.interfaces.coupon.dto;

public record CouponResponseDto (
        long couponId,
        String message,
        java.time.LocalDateTime issuance
){
}
