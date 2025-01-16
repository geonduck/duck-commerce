package kr.hhplus.be.server.interfaces.order.dto;

public record OrderItemResponseDto(
        Long productId,
        String productName,
        int amount
) {
}