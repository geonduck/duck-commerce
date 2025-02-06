package kr.hhplus.be.server.interfaces.order.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record OrderResponseDto (
        @JsonProperty("user_id") String userId,
        @JsonProperty("order_id") Long orderId,
        @JsonProperty("order_items") List<OrderItemResponseDto> orderItems,
        @JsonProperty("status") String status,
        @JsonProperty("total_price") double totalPrice,
        @JsonProperty("discount_price") double discountPrice

){
}
