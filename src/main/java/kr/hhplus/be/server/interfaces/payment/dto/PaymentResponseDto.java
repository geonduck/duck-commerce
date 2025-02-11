package kr.hhplus.be.server.interfaces.payment.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record PaymentResponseDto (
        @JsonProperty("user_id") String userId,
        @JsonProperty("order_id") Long orderId,
        @JsonProperty("status") String status

){
}
