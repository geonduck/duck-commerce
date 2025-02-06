package kr.hhplus.be.server.interfaces.coupon.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CouponRequestDto (
        @NotBlank(message = "사용자 ID는 필수입니다.")
        @JsonProperty("coupon_id") String userId,

        @NotNull(message = "쿠폰 ID는 필수입니다.")
        @Min(value = 1, message = "쿠폰 ID는 반드시 1 이상의 값이어야 합니다.")
        @JsonProperty("coupon_id") long couponId
) {
}
