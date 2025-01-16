package kr.hhplus.be.server.domain.order.dto;

public record OrderItemResponse(
        Long productId,
        String productName,
        int amount
) {
}
