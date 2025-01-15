package kr.hhplus.be.server.interfaces.product.dto;

public record ProductResponseDto(
        String name,
        double price,
        int stock
) {
}
