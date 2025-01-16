package kr.hhplus.be.server.interfaces.order.dto;

public record OrderItemRequestDto(
        Long productId,
        int amount
) {
}
