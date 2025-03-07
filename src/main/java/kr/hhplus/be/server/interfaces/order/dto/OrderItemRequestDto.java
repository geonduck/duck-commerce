package kr.hhplus.be.server.interfaces.order.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record OrderItemRequestDto(
        @NotNull(message = "상품 ID는 필수입니다.")
        @JsonProperty("product_id") Long productId,

        @Min(value = 1, message = "상품 수량은 최소 1개 이상이어야 합니다.")
        @JsonProperty("amount") int amount
) {
}
