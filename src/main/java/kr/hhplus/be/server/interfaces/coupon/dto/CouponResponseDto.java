package kr.hhplus.be.server.interfaces.coupon.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CouponResponseDto {

    @JsonProperty("coupon_id")
    private Long couponId;

    @JsonProperty("message")
    private String message;

    @JsonProperty("issuance")
    private LocalDateTime issuance;
}