package kr.hhplus.be.server.interfaces.cart.dto;

public record CartResponseDto(
        String name,
        long productId,
        long amount
){
}
