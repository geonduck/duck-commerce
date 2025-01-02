package kr.hhplus.be.server.interfaces.coupon.dto;

public record CouponResponseDto (
        long couponId,
        String message,
        int issuance
){
}
