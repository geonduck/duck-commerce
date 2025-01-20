package kr.hhplus.be.server.interfaces.order.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record OrderRequestDto (
        @NotBlank(message = "사용자 ID는 필수입니다.")
        String userId,

        @NotEmpty(message = "주문 항목(Order Items)은 최소 1개 이상이어야 합니다.")
        @Size(max = 10, message = "주문 항목(Order Items)은 최대 10개를 초과할 수 없습니다.")
        List<@Valid OrderItemRequestDto> orderItems,

        @Min(value = 1, message = "쿠폰 ID는 반드시 1 이상의 값이어야 합니다.")
        Long couponId
){
}
