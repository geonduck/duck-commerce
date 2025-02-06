package kr.hhplus.be.server.interfaces.coupon.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

public record CouponResponseDto (
        @JsonProperty("coupon_id") Long couponId,
        @JsonProperty("message") String message,
        @JsonProperty("issuance") LocalDateTime issuance
){
}
