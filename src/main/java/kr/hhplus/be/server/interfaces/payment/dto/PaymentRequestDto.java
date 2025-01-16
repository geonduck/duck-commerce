package kr.hhplus.be.server.interfaces.payment.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record PaymentRequestDto (
        @NotBlank(message = "사용자 ID는 필수입니다.")
        String userId,

        @Min(value = 1, message = "주문 ID는 반드시 1 이상의 값이어야 합니다.")
        Long orderId
) {
}
