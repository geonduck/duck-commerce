package kr.hhplus.be.server.interfaces.cart.dto;

public record CartRequestDto(
        String userId,
        long productId,
        long amount
) {
}
