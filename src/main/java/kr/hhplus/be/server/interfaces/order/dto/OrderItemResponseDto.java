package kr.hhplus.be.server.interfaces.order.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record OrderItemResponseDto(
        @JsonProperty("product_id") Long productId,
        @JsonProperty("product_name") String productName,
        @JsonProperty("amount") int amount
) {
}