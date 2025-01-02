package kr.hhplus.be.server.interfaces.product.dto;

public record ProductResponseDto(
        String name,
        int price,
        int stock
) {
}
