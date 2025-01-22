package kr.hhplus.be.server.interfaces.coupon.dto;

import java.time.LocalDateTime;

public record CouponResponseDto (
        long couponId,
        String message,
        LocalDateTime issuance
){
}
